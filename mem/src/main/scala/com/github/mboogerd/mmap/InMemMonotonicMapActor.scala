/*
 * Copyright 2017 Merlijn Boogerd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.mboogerd.mmap

import akka.actor.{ Actor, ActorLogging, Props }
import algebra.lattice.JoinSemilattice
import cats.Semigroup
import cats.instances.option._
import cats.instances.vector._
import cats.syntax.semigroup._
import com.github.mboogerd.mmap.InMemMonotonicMapMessages._
import org.log4s._
import org.reactivestreams.{ Subscriber, Subscription }

/**
 * InMemMonotonicMapActor allows reads and writes. Writes are propagated to active readers. For every
 * Reader notified, the Writer is notified. A flag is set to signal whether the last update completes
 * the reader-notifications, up to that point.
 *
 * This implementation is not optimized in any way. In particular, it simply merges all unconsumed deltas. The initial
 * element is likely to be the entire state for the key, no effort is made to cut it into bite-sized chunks.
 */
object InMemMonotonicMapActor {
  def props[K](initialState: Map[K, Any]): Props = Props(new InMemMonotonicMapActor[K](initialState))
}

class InMemMonotonicMapActor[K](initialState: Map[K, Any]) extends Actor with ActorLogging {

  private[this] val log = getLogger

  var readers: SubscribedReaders[K] = SubscribedReaders()
  var writers: SubscribedWriters[K] = SubscribedWriters()

  // The 'persistent' state for this map
  var state: Map[K, (Set[Long], Any)] = initialState.mapValues(any ⇒ (Set.empty, any))

  // logical timestamps for Reader/Writer subscribers
  var subscriberIndex: Long = 0

  /*
    * Message handling logic
    */
  override val receive: Receive = {
    case UpdateDemand(key: K @unchecked, index, demand, false) if readers.isDefined(key, index) ⇒
      handleUpdateReader(key, index, readers.addDemand(demand))
    case UpdateDemand(key: K @unchecked, index, demand, true) if writers.isDefined(key, index) ⇒
      handleUpdateWriter(key, index, writers.addDemand(demand))
    case Read(key: K @unchecked, subscriber) ⇒
      readers = subscribeReader(key, subscriber, state.get(key))._2
    case Write(key: K @unchecked, value, lat, sub) ⇒
      handleWrite(key, value, sub)(lat)
    case Unsubscribe(key: K @unchecked, index, false) if readers.isDefined(key, index) ⇒
      readers = readers.unsubscribe(key, index)
    case Unsubscribe(key: K @unchecked, index, true) if writers.isDefined(key, index) ⇒
      unsubscribeWriter(key, index)
  }

  /*
   * Implementation
   */
  def nextSubscriberIndex[T](f: Long => T): (Long, T) = {
    subscriberIndex += 1
    val t = f(subscriberIndex)
    (subscriberIndex, t)
  }

  def createSubscription(key: K, index: Long, writer: Boolean): Subscription = new Subscription {
    override def cancel(): Unit = self ! Unsubscribe(key, index, writer)
    override def request(n: Long): Unit = self ! UpdateDemand(key, index, n, writer)
  }

  def subscribeReader(key: K, subscriber: Subscriber[Any], queue: Option[(Set[Long], Any)]): (Long, SubscribedReaders[K]) = {
    nextSubscriberIndex { index ⇒
      val subscription = createSubscription(key, index, writer = false)
      subscriber.onSubscribe(subscription)
      readers.subscribe(key, subscriber, queue, index)
    }
  }

  def subscribeWriter(key: K, subscriber: Subscriber[WriteNotification], queue: Vector[WriteNotification]): (Long, SubscribedWriters[K]) = {
    nextSubscriberIndex { index ⇒
      val subscription = createSubscription(key, index, writer = true)
      subscriber.onSubscribe(subscription)
      writers.subscribe(key, subscriber, queue, index)
    }
  }

  /**
   * Unsubscribes the writer and removes its index from all readers/state
   */
  def unsubscribeWriter(key: K, index: Long): Unit = {
    writers = writers.unsubscribe(key, index)
    val (indexes, keyState) = state(key)
    state = state.updated(key, (indexes - index, keyState))
    readers = readers.unsubscribeWriter(key, index)
  }

  def handleUpdateReader(key: K, index: Long, mutate: SubscribedReaders[K]#SubscriberState ⇒ SubscribedReaders[K]#SubscriberState): Unit = {
    val (sub, queue, newState) = readers.updateSubscriber(key, index, mutate)
    queue.map(_._2).foreach(sub.onNext)
    queue.toTraversable.flatMap(q ⇒ q._1).foreach(notifyPropagation(key, index)(_))
    readers = newState
  }

  private def notifyPropagation(key: K, readerIndex: Long)(writerIndex: Long): Unit =
    handleUpdateWriter(key, writerIndex, writers.enqueue(Vector(Propagated(readerIndex))))

  def handleUpdateWriter(key: K, index: Long, update: SubscribedWriters[K]#SubscriberState ⇒ SubscribedWriters[K]#SubscriberState): Unit = {
    val (sub, toSend, subscriberState) = writers.updateSubscriber(key, index, update)
    toSend.foreach(sub.onNext)
    writers = subscriberState
  }

  def handleWrite(key: K, value: Any, tracker: Subscriber[WriteNotification])(implicit lattice: JoinSemilattice[Any]): Unit = {
    val (writerIndex, subState) = subscribeWriter(key, tracker, Vector(Persisted()))
    val trackedWrite = (Set(writerIndex), value)
    // need to combine new value with possibly existing value (product lattice)
    state = state.updated(key, state.get(key).map(_ |+| trackedWrite).getOrElse(trackedWrite))
    writers = subState

    val readerIndices = readers.state.getOrElse(key, Map.empty).keySet
    readerIndices.foreach(index => handleUpdateReader(key, index, readers.enqueue(Option(trackedWrite))))
  }

  private implicit def trackedWriteSemigroup(implicit joinSemilattice: JoinSemilattice[Any]): Semigroup[(Set[Long], Any)] =
    (x: (Set[Long], Any), y: (Set[Long], Any)) => (x._1 ++ y._1, joinSemilattice.join(x._2, y._2))
}