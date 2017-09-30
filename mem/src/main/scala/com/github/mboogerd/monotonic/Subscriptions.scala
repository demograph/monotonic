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

import algebra.Semigroup
import algebra.lattice.JoinSemilattice
import org.reactivestreams.Subscriber
import cats.syntax.semigroup._
/**
 *
 */
object Subscriptions {
  final val initialDemand: Long = 0L
}
trait Subscriptions[K] {
  type Value
  type Queue
  type SubscriberState = (Subscriber[Value], Long, Queue)
  type Inner = Map[K, Map[Long, SubscriberState]]
  type Outer <: Subscriptions[K]
  val state: Inner

  def isDefined(key: K, index: Long): Boolean = state.contains(key) && state(key).contains(index)

  def subscribe(key: K, subscriber: Subscriber[Value], queue: Queue, index: Long): Outer = {
    val subscriptionState = (subscriber, Subscriptions.initialDemand, queue)
    val newSubscribersState = state.updated(key, state.getOrElse(key, Map.empty) + (index → subscriptionState))
    instantiate(newSubscribersState)
  }

  def unsubscribe(key: K, index: Long): Outer = {
    val subscribed = state(key) - index
    val newSubscribersState = if (subscribed.isEmpty) state - key else state + (key → subscribed)
    instantiate(newSubscribersState)
  }

  def updateSubscribers(key: K, update: SubscriberState ⇒ SubscriberState): (Set[(Subscriber[Value], Queue)], Outer) = {
    val subscriptions = state.getOrElse(key, Map.empty)
    val modified = subscriptions.mapValues(update.andThen(requestedElements))
    val toSend = modified.map { case (_, (queue, (subscriber, _, _))) ⇒ subscriber -> queue }.toSet
    val newSubscribersState = state + (key → modified.mapValues(_._2))
    (toSend, instantiate(newSubscribersState))
  }

  def updateSubscriber(key: K, index: Long, update: SubscriberState ⇒ SubscriberState): (Subscriber[Value], Queue, Outer) = {
    val subscriptions = state.getOrElse(key, Map.empty)
    val (toSend, subscription) = requestedElements(update(subscriptions(index)))
    val newSubscribersState = state.updated(key, subscriptions + (index → subscription))
    (subscription._1, toSend, instantiate(newSubscribersState))
  }

  def addDemand(demand: Long): SubscriberState ⇒ SubscriberState = {
    case (sub, oldDemand, queue) ⇒ (sub, oldDemand + demand, queue)
  }

  def enqueue(q2: Queue)(implicit ev: Semigroup[Queue]): SubscriberState ⇒ SubscriberState = {
    case (sub, demand, q1) ⇒ (sub, demand, q1 |+| q2)
  }

  protected def instantiate(inner: Inner): Outer

  protected def requestedElements(queue: Queue, demand: Long): (Queue, Queue, Long)

  private def requestedElements(state: SubscriberState): (Queue, SubscriberState) = {
    val (subscriber, demand, queue) = state
    val (toSend, toRetain, newDemand) = requestedElements(queue, demand)
    val subscriptionState = (subscriber, newDemand, toRetain)
    (toSend, subscriptionState)
  }
}