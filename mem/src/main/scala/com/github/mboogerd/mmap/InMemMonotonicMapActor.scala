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
import akka.event.LoggingReceive
import algebra.lattice.JoinSemilattice
import com.github.mboogerd.mmap.InMemMonotonicMapActor._
import org.log4s._
import org.reactivestreams.{ Subscriber, Subscription }

import scala.collection.immutable.Seq

/**
 * InMemMonotonicMapActor allows reads and writes. Writes are propagated to active readers. For every
 * Reader notified, the Writer is notified. A flag is set to signal whether the last update completes
 * the reader-notifications, up to that point.
 *
 * This implementation is not optimized in any way. In particular, it simply merges new data at (developer)
 * convenience, which is whenever all active readers have processed it. New readers may therefore see chunks
 * instead of a complete initial state because an existing reader had no demand. Conversely, the initial element
 * may be the entire state for the key, no effort is made to cut it into bite-sized chunks.
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

  var state: Map[K, Vector[(Seq[Long], AnyRef)]] = initialState.mapValues(any ⇒ Vector((Seq.empty, any)))

  // logical timestamps for queries and writes
  var subscriberIndex: Long = 0

  // Abstracts over the very similar types that we have for maintaining subscriber-state of writes and queries
  private type SubscriberState[S, Q] = Map[K, Map[Long, (Subscriber[S], Long, Vector[Q])]]
  var queries: SubscriberState[AnyRef, (Seq[Long], AnyRef)] = Map.empty
  var writes: SubscriberState[WriteNotification, WriteNotification] = Map.empty

  override def receive: Receive = LoggingReceive {
//    logPrePostState {
      case Read(key: K @unchecked, subscriber) ⇒ queries = subscribe(key, subscriber, queries, writer = false, state.getOrElse(key, Vector.empty))._2
      case Unsubscribe(key: K @unchecked, index, false) ⇒ queries = unsubscribe(key, index, queries)
      case Unsubscribe(key: K @unchecked, index, true) ⇒ writes = unsubscribe(key, index, writes)
      case UpdateDemand(key: K @unchecked, index, demand, false) ⇒ handleUpdateReader(key, index, demand)
      case UpdateDemand(key: K @unchecked, index, demand, true) ⇒ handleUpdateWriter(key, index, demand)
      case Write(key: K @unchecked, value, lat, sub) ⇒ handleWrite(key, value, lat, sub)
//    }
  }

  def subscribe[S, Q](key: K, subscriber: Subscriber[S], from: SubscriberState[S, Q], writer: Boolean, queue: Vector[Q]): (Long, SubscriberState[S, Q]) = {
    subscriberIndex += 1

    val subscription = new Subscription {
      val subscriptionIndex: Long = subscriberIndex
      override def cancel(): Unit = self ! Unsubscribe(key, subscriptionIndex, writer)
      override def request(n: Long): Unit = self ! UpdateDemand(key, subscriptionIndex, n, writer)
    }

    // pass the subscription to the subscriber
    subscriber.onSubscribe(subscription)

    // add the subscription to active `queries` with demand set to 0
    val subscriptionState = (subscriber, InMemMonotonicMapActor.initialDemand, queue)
    (subscriberIndex, from.updated(key, from.getOrElse(key, Map.empty) + (subscriberIndex → subscriptionState)))
  }

  def unsubscribe[S, Q](key: K, index: Long, from: SubscriberState[S, Q]): SubscriberState[S, Q] = {
    val subscribed = from(key) - index
    if (subscribed.isEmpty) from - key
    else from + (key → subscribed)
  }

  def handleUpdateReader(key: K, index: Long, newDemand: Long, enqueue: Vector[(Seq[Long], AnyRef)] = Vector.empty): Unit = {
    val (subscriber, _, _) = queries(key)(index)
    val (toSend, subState) = eventsToDispatch(key, index, queries, newDemand, enqueue)

    // send updates to subscribers
    toSend.foreach(s ⇒ subscriber.onNext(s._2))

    // send write notifications
    val updates: Seq[(Long, Propagated)] = toSend.flatMap {
      case (trackers, _) ⇒
        trackers.map(_ → Propagated(index))
    }
    updates.foreach { case (writerIndex, msg) ⇒ handleUpdateWriter(key, writerIndex, 0, Vector(msg)) }

    queries = subState
  }

  def handleUpdateWriter(key: K, index: Long, newDemand: Long, enqueue: Vector[WriteNotification] = Vector.empty): Unit = {
    val (subscriber, _, _) = writes(key)(index)
    val (toSend, subState) = eventsToDispatch(key, index, writes, newDemand, enqueue)
    // send notifications to writer
    toSend.foreach(subscriber.onNext)

    writes = subState
  }

  def eventsToDispatch[S, Q](key: K, index: Long, from: SubscriberState[S, Q], addDemand: Long = 0,
    enqueue: Vector[Q] = Vector.empty): (Vector[Q], SubscriberState[S, Q]) = {
    val (subscriber, demand, queue) = from(key)(index)
    val newDemand = demand + addDemand
    val (toSend, toRetain) = (queue ++ enqueue).splitAt(newDemand.toInt)
    val newSubscriberState = (subscriber, newDemand - toSend.size + demand, toRetain)
    (toSend, from.updated(key, from(key) + (index → newSubscriberState)))
  }

  def handleWrite(key: K, value: AnyRef, lattice: JoinSemilattice[AnyRef], tracker: Subscriber[WriteNotification]): Unit = {
    val (writerIndex, subState) = subscribe(key, tracker, writes, writer = true, Vector(Persisted()))
    val trackedWrite = (Seq(writerIndex), value)
    state = state.updated(key, state.get(key).map(trackedWrite +: _).getOrElse(Vector(trackedWrite)))
    writes = subState
    queries.getOrElse(key, Map.empty).keySet.foreach(index => handleUpdateReader(key, index, 0, Vector(trackedWrite)))
  }

  /**
    * @deprecated Temporary logging of total state
    */
  private def showState: String =
    s"""
        STATE =   $state
        QUERIES = $queries
        WRITES =  $writes
     """.stripMargin

  /**
    * @deprecated Temporary logging of total state
    */
  private def logPrePostState[S, T]: PartialFunction[S, T] ⇒ PartialFunction[S, T] = pf ⇒ {
    case s if pf.isDefinedAt(s) ⇒
      log.info(s"[PRE $s] $showState")
      val result = pf(s)
      log.info(s"[POST $s] $showState")
      result
  }
}
