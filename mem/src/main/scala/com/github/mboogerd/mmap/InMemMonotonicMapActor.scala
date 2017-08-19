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
import cats.syntax.semigroup._
import com.github.mboogerd.mmap.InMemMonotonicMapActor._
import org.log4s._
import org.reactivestreams.{ Subscriber, Subscription }

import scala.collection.immutable.Seq

/**
 * InMemMonotonicMapActor allows reads and writes. Writes are propagated to active readers. For every
 * Reader notified, the Writer is notified. A flag is set to signal whether the last update completes
 * the reader-notifications, up to that point.
 *
 * This implementation is not optimized in any way. In particular, it simply merges all unconsumed deltas. The initial
 * element is likely to be the entire state for the key, no effort is made to cut it into bite-sized chunks.
 */
object InMemMonotonicMapActor {
  def props[K <: AnyRef](initialState: Map[K, AnyRef]): Props = Props(new InMemMonotonicMapActor[K](initialState))

  case class Read(key: AnyRef, subscriber: Subscriber[AnyRef])

  case class Unsubscribe(key: AnyRef, index: Long, writer: Boolean)

  case class UpdateDemand(key: AnyRef, index: Long, demand: Long, writer: Boolean)

  case class Write[V <: AnyRef](key: AnyRef, value: V, joinSemilattice: JoinSemilattice[V], subscriber: Subscriber[WriteNotification])

  private final val initialDemand: Long = 0L

  /**
   * Signals that the write was stored in the memory state. This happens max. 1 time and should be the first event
   */
  case class Persisted() extends WriteNotification

  /**
   * Signals that the write was propagated to a Subscriber.
   *
   * @param query The internal index of the query, only there to aid in identification (may be removed in the future)
   */
  case class Propagated(query: Long) extends WriteNotification

}

class InMemMonotonicMapActor[K <: AnyRef](initialState: Map[K, AnyRef]) extends Actor with ActorLogging {

  private[this] val log = getLogger

  var state: Map[K, (Set[Long], AnyRef)] = initialState.mapValues(any ⇒ (Set.empty, any))

  // logical timestamps for queries and writes
  var subscriberIndex: Long = 0

  // Abstracts over the very similar types that we have for maintaining subscriber-state of writes and queries
  private type SubscriberState[S, Q] = Map[K, Map[Long, (Subscriber[S], Long, Q)]]
  var queries: SubscriberState[AnyRef, Option[(Set[Long], AnyRef)]] = Map.empty
  var writes: SubscriberState[WriteNotification, Vector[WriteNotification]] = Map.empty

  override def receive: Receive = {
    case Read(key: K @unchecked, subscriber) ⇒
      queries = subscribe(queries)(key, subscriber, writer = false, state.get(key))._2
    case Unsubscribe(key: K @unchecked, index, false) if queriesContains(key, index) ⇒
      queries = unsubscribe(key, index, queries)
    case Unsubscribe(key: K @unchecked, index, true) if writesContains(key, index) ⇒
      unsubscribeWriter(key, index)
    case UpdateDemand(key: K @unchecked, index, demand, false) if queriesContains(key, index) ⇒
      handleUpdateReader(key, index, demand)()
    case UpdateDemand(key: K @unchecked, index, demand, true) if writesContains(key, index) ⇒
      handleUpdateWriter(key, index, demand)
    case Write(key: K @unchecked, value, lat, sub) ⇒
      handleWrite(key, value, sub)(lat)
  }

  def queriesContains: (K, Long) ⇒ Boolean = subStateContains(queries)
  def writesContains: (K, Long) ⇒ Boolean = subStateContains(writes)
  def subStateContains[S, Q](subState: SubscriberState[S, Q])(key: K, index: Long): Boolean =
    subState.contains(key) && subState(key).contains(index)

  def subscribe[S, Q](from: SubscriberState[S, Q])(key: K, subscriber: Subscriber[S], writer: Boolean, queue: Q): (Long, SubscriberState[S, Q]) = {
    subscriberIndex += 1

    val subscription = new Subscription {
      val subscriptionIndex: Long = subscriberIndex
      override def cancel(): Unit = self ! Unsubscribe(key, subscriptionIndex, writer)
      override def request(n: Long): Unit = self ! UpdateDemand(key, subscriptionIndex, n, writer)
    }

    subscriber.onSubscribe(subscription)

    // add the subscription to active subscribers with demand set to 0
    val subscriptionState = (subscriber, InMemMonotonicMapActor.initialDemand, queue)
    val newSubscribersState = from.updated(key, from.getOrElse(key, Map.empty) + (subscriberIndex → subscriptionState))
    (subscriberIndex, newSubscribersState)
  }

  /**
   * Unsubscribes the given (key, index) pair from the SubscriberState `from`
   * @return the SubscriberState without the subscriber
   */
  def unsubscribe[S, Q](key: K, index: Long, from: SubscriberState[S, Q]): SubscriberState[S, Q] = {
    val subscribed = from(key) - index
    if (subscribed.isEmpty) from - key
    else from + (key → subscribed)
  }

  /**
   * Unsubscribes the writer and removes its index from all queries/state
   */
  def unsubscribeWriter(key: K, index: Long): Unit = {

    // remove any pending messages for the writer
    writes = unsubscribe(key, index, writes)

    // remove the writer from being
    val (writers, keyState) = state(key)
    state = state.updated(key, (writers - index, keyState))

    // remove the writer-index from any running queries
    val keyQueries = queries.get(key).map(_.mapValues {
      case (sub, demand, subState) ⇒ (sub, demand, subState.map { case (set, value) ⇒ (set - index, value) })
    })
    keyQueries.foreach { map ⇒
      queries = queries.updated(key, map)
    }
  }

  def handleUpdateReader(key: K, index: Long, newDemand: Long)(f: Option[(Set[Long], AnyRef)] ⇒ Option[(Set[Long], AnyRef)] = identity): Unit = {
    val (subscriber, demand, queue) = queries(key)(index)
    val totalDemand = demand + newDemand
    val totalQueue = f(queue)
    val shouldSend = totalDemand > 0

    // Should send to the Reader
    if (shouldSend) totalQueue.foreach(s ⇒ subscriber.onNext(s._2))
    val netDemand = if (shouldSend) totalDemand - 1 else totalDemand
    val netQueue = if (!shouldSend) totalQueue else Option.empty

    // send write notifications for a dispatched read
    val updates = if (shouldSend) totalQueue.toSeq.flatMap {
      case (trackers, _) ⇒
        trackers.map(_ → Propagated(index))
    }
    else Seq.empty

    updates.foreach { case (writerIndex, msg) ⇒ handleUpdateWriter(key, writerIndex, 0, Vector(msg)) }

    queries = queries.updated(key, queries(key) + (index → (subscriber, netDemand, netQueue)))
  }

  def handleUpdateWriter(key: K, index: Long, newDemand: Long, enqueue: Vector[WriteNotification] = Vector.empty): Unit = {
    val (subscriber, _, _) = writes(key)(index)
    val (toSend, subState) = eventsToDispatch(writes)(key, index, newDemand, enqueue)
    // send notifications to writer
    toSend.foreach(subscriber.onNext)
    writes = subState
  }

  def eventsToDispatch[S, Q](from: SubscriberState[S, Vector[Q]])(key: K, index: Long, addDemand: Long = 0, enqueue: Vector[Q]): (Vector[Q], SubscriberState[S, Vector[Q]]) = {
    val (subscriber, demand, queue) = from(key)(index)
    val newDemand = demand + addDemand
    val (toSend, toRetain) = (queue ++ enqueue).splitAt(math.min(Int.MaxValue, newDemand).toInt)
    val newSubscriberState = (subscriber, newDemand - toSend.size + demand, toRetain)
    (toSend, from.updated(key, from(key) + (index → newSubscriberState)))
  }

  def handleWrite(key: K, value: AnyRef, tracker: Subscriber[WriteNotification])(implicit lattice: JoinSemilattice[AnyRef]): Unit = {
    val (writerIndex, subState) = subscribe(writes)(key, tracker, writer = true, Vector(Persisted()))
    val trackedWrite = (Set(writerIndex), value)
    // need to combine new value with possibly existing value (product lattice)
    state = state.updated(key, state.get(key).map(_ |+| trackedWrite).getOrElse(trackedWrite))
    writes = subState

    queries.getOrElse(key, Map.empty).keySet.foreach(index => handleUpdateReader(key, index, 0)(_ |+| Option(trackedWrite)))
  }

  implicit def trackedWriteSemigroup(implicit joinSemilattice: JoinSemilattice[AnyRef]): Semigroup[(Set[Long], AnyRef)] =
    (x: (Set[Long], AnyRef), y: (Set[Long], AnyRef)) => (x._1 ++ y._1, joinSemilattice.join(x._2, y._2))
}
