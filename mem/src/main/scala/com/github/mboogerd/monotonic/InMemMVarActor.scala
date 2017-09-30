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

package com.github.mboogerd.monotonic

import akka.actor.{ Actor, ActorLogging, Props }
import algebra.Semilattice
import algebra.lattice.{ BoundedJoinSemilattice, JoinSemilattice }
import cats.instances.set.catsKernelStdSemilatticeForSet
import cats.kernel.instances.tuple.catsKernelStdSemigroupForTuple2
import cats.syntax.semigroup.catsSyntaxSemigroup
import com.github.mboogerd.monotonic.InMemMVarActor._
import com.github.mboogerd.monotonic.InMemMonotonicMapMessages.Persisted
import com.github.mboogerd.monotonic.queue.{ LWWQueue, Queue }
import com.github.mboogerd.monotonic.util.CancellingSubscriber
import org.reactivestreams.{ Subscriber, Subscription }

/**
 * InMemMVarActor allows reads and writes. Writes are propagated to active readers. For every
 * Reader notified, the Writer is notified. A flag is set to signal whether the last update completes
 * the reader-notifications, up to that point.
 *
 * This implementation is not optimized in any way. In particular, it simply merges all unconsumed deltas. The initial
 * element is likely to be the entire state for the no effort is made to cut it into bite-sized chunks.
 */
object InMemMVarActor {
  def props[V: JoinSemilattice](initialState: V): Props = Props(new InMemMVarActor[V](initialState))
  def props[V: BoundedJoinSemilattice]: Props = props(BoundedJoinSemilattice[V].zero)

  case class Subscribe[V](subscriber: Subscriber[V], queue: Queue[(Set[Long], V)])
  case class Unsubscribe(index: Long, writer: Boolean)
  case class UpdateDemand(index: Long, demand: Long, writer: Boolean)
  object UpdateValue {
    def apply[V](value: V): UpdateValue[V, Queue[WriteNotification]] =
      new UpdateValue[V, Queue[WriteNotification]](value, new CancellingSubscriber[WriteNotification], LWWQueue.empty)
  }
  case class UpdateValue[V, Q <: Queue[WriteNotification]](delta: V, subscriber: Subscriber[WriteNotification], queue: Q)

  case class Registered() extends WriteNotification
  case class Propagated(readerIndex: Long) extends WriteNotification
}

class InMemMVarActor[V: JoinSemilattice](initialState: V) extends Actor with ActorLogging {

  type SubscriberState[S, Q] = (Subscriber[S], Queue[Q], Long)
  type ReaderState = SubscriberState[V, (Set[Long], V)]
  type WriterState = SubscriberState[WriteNotification, WriteNotification]

  var readers: Map[Long, ReaderState] = Map.empty
  var writers: Map[Long, WriterState] = Map.empty

  // The 'persistent' state for this map
  var state: (Set[Long], V) = (Set.empty, initialState)

  // logical timestamps for Reader/Writer subscribers
  var subscriberIndex: Long = 0

  /*
    * Message handling logic
    */
  override val receive: Receive = {
    case UpdateDemand(index, demand, false) if readers.contains(index) && demand >= 0 ⇒
      handleUpdateReader(index, addDemand(demand))
    case UpdateDemand(index, demand, true) if writers.contains(index) && demand >= 0 ⇒
      handleUpdateWriter(index, addDemand(demand))
    case sub: Subscribe[V @unchecked] if sub.queue.isEmpty ⇒
      readers = readers + subscribe(sub.subscriber, sub.queue.enqueue(state), writer = false)
    case UpdateValue(value: V @unchecked, sub, queue) ⇒
      handleWrite(value, sub, queue)
    case Unsubscribe(index, false) if readers.contains(index) ⇒
      readers = readers - index
    case Unsubscribe(index, true) if writers.contains(index) ⇒
      unsubscribeWriter(index)
  }

  /*
   * Implementation
   */
  private def createSubscription(index: Long, writer: Boolean): Subscription = new Subscription {
    override def cancel(): Unit = self ! Unsubscribe(index, writer)
    override def request(n: Long): Unit = self ! UpdateDemand(index, n, writer)
  }

  def subscribe[S, Q](subscriber: Subscriber[S], queue: Queue[Q], writer: Boolean): (Long, SubscriberState[S, Q]) = {
    subscriberIndex += 1
    val subscription = createSubscription(subscriberIndex, writer)
    subscriber.onSubscribe(subscription)
    (subscriberIndex, (subscriber, queue, Subscriptions.initialDemand))
  }

  /**
   * Unsubscribes the writer
   */
  def unsubscribeWriter(index: Long): Unit = {
    writers = writers - index
    val (indexes, keyState) = state
    state = (indexes - index, keyState)
  }

  def handleUpdateReader(index: Long, mutate: ReaderState ⇒ ReaderState): Unit = {
    val (sub, queue, demand) = mutate(readers(index))
    val (newDemand, toSend, newQueue) = queue.dequeue(math.min(demand, Int.MaxValue).toInt)

    // Send the dequeued payload to the reader
    toSend.map(_._2).foreach(sub.onNext)

    // Notify the (still subscribed) writers involved in the construction of that payload
    val writersToNotify = toSend.flatMap(_._1).toSet.intersect(writers.keySet)
    writersToNotify.foreach(notifyPropagation(index)(_))

    readers = readers.updated(index, (sub, newQueue, newDemand))
  }

  private def notifyPropagation(readerIndex: Long)(writerIndex: Long): Unit =
    handleUpdateWriter(writerIndex, enqueue(Propagated(readerIndex)))

  def handleUpdateWriter(index: Long, update: WriterState ⇒ WriterState): Unit = {
    val (sub, queue, demand) = update(writers(index))
    val (newDemand, toSend, newQueue) = queue.dequeue(math.min(demand, Int.MaxValue).toInt)

    toSend.foreach(sub.onNext)
    writers = writers.updated(index, (sub, newQueue, newDemand))
  }

  def handleWrite(value: V, tracker: Subscriber[WriteNotification], queue: Queue[WriteNotification]): Unit = {
    val (writerIndex, subState) = subscribe(tracker, queue.enqueue(Persisted()), writer = true)
    val trackedWrite = (Set(writerIndex), value)
    // need to combine new value with possibly existing value (product lattice)
    state = state |+| trackedWrite
    writers = writers.updated(writerIndex, subState)

    val readerIndices = readers.keySet
    readerIndices.foreach(index => handleUpdateReader(index, enqueue(trackedWrite)))
  }

  def addDemand[S, Q](d: Long): SubscriberState[S, Q] ⇒ SubscriberState[S, Q] = {
    case (sub, queue, demand) ⇒ (sub, queue, demand + d)
  }

  def enqueue[S, Q](msg: Q): SubscriberState[S, Q] ⇒ SubscriberState[S, Q] = {
    case (sub, queue, demand) ⇒ (sub, queue.enqueue(msg), demand)
  }

  private implicit val jslShim: Semilattice[V] = JoinSemilattice[V].joinSemilattice
}