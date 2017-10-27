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

package io.demograph.monotonic.mvar

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.{ AtomicLong, AtomicReference }
import java.util.function.UnaryOperator

import algebra.lattice.{ BoundedJoinSemilattice, JoinSemilattice }
import io.demograph.monotonic.mvar.AtomicMVar.SubscriberState
import io.demograph.monotonic.queue.{ MergingQueue, Queue }
import org.reactivestreams.{ Publisher, Subscriber, Subscription }

/**
 * Ideas:
 * - Update the variable atomically, propagate asynchronously to subscribers
 * - Wait for all subscriber demand to be positive, then update and propagate?
 */
class AtomicMVar[V: JoinSemilattice](initialValue: V) extends MVar[V] {

  protected val value: AtomicReference[V] = new AtomicReference[V](initialValue)

  override def sample: V = value.get()

  override protected[mvar] def onUpdate(delta: V): Unit = {
    value.getAndUpdate(new UnaryOperator[V] {
      override def apply(currentState: V): V = JoinSemilattice.join(currentState, delta)
    })
    updateSubscribers(enqueue(delta) andThen dispatchReadyElements)
  }

  override protected[mvar] def onUpdate(f: V ⇒ V): Unit = {
    value.getAndUpdate(new UnaryOperator[V] {
      override def apply(currentState: V): V = {
        val delta = f(currentState)
        val newState = JoinSemilattice.join(currentState, delta)
        updateSubscribers(enqueue(delta) andThen dispatchReadyElements)
        newState
      }
    })
  }

  val index = new AtomicLong(0L)
  val atomicMap = new ConcurrentHashMap[Long, SubscriberState[_ >: V]]()

  override val publisher: Publisher[V] = new Publisher[V] {
    override def subscribe(s: Subscriber[_ >: V]): Unit = {

      val newIndex = index.incrementAndGet()
      val state = SubscriberState[V](s.asInstanceOf[Subscriber[V]], 0L, new MergingQueue[V](Some(sample)))

      atomicMap.put(newIndex, state)
      val subscription = new Subscription {
        override def cancel(): Unit = atomicMap.remove(newIndex)
        override def request(n: Long): Unit = {
          updateSubscriber(newIndex, addDemand(n) andThen dispatchReadyElements)
        }
      }
      s.onSubscribe(subscription)
    }
  }

  /* FIXME: Temporarily changed visibility to protected. Should probably be reverted! */
  protected def updateSubscribers(f: SubscriberState[V] ⇒ SubscriberState[V]): Unit = {
    atomicMap.replaceAll((t: Long, u: SubscriberState[_ >: V]) => f(u.asInstanceOf[SubscriberState[V]]))
  }
  protected def updateSubscriber(index: Long, f: SubscriberState[V] ⇒ SubscriberState[V]): Unit = {
    atomicMap.computeIfPresent(index, (_: Long, u: SubscriberState[_ >: V]) => f(u.asInstanceOf[SubscriberState[V]]))
  }

  protected def addDemand(demand: Long): SubscriberState[V] ⇒ SubscriberState[V] =
    state ⇒ state.copy(demand = demand + state.demand)

  protected def enqueue(element: V): SubscriberState[V] ⇒ SubscriberState[V] =
    state ⇒ state.copy(queue = state.queue.enqueue(element))

  protected def dispatchReadyElements: SubscriberState[V] ⇒ SubscriberState[V] = state ⇒ {
    import state._
    val (sendSize, toSend, newQueue) = queue.dequeue(math.min(Int.MaxValue, demand).toInt)

    // FIXME: Here we have a side-effect, there should be a better way of atomically updating the subscriber,
    // acquiring what needs to be dispatched and then actually dispatching it
    toSend.foreach(subscriber.onNext)

    SubscriberState[V](subscriber, demand - sendSize, newQueue)
  }
}
object AtomicMVar {

  def apply[V: JoinSemilattice](initialValue: V): MVar[V] = new AtomicMVar[V](initialValue)

  def apply[V: BoundedJoinSemilattice](): MVar[V] = apply(BoundedJoinSemilattice[V].zero)

  case class SubscriberState[V](subscriber: Subscriber[V], demand: Long, queue: Queue[V])
}