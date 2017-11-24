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

package io.demograph.monotonic.`var`

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.{ AtomicLong, AtomicReference }
import java.util.function.UnaryOperator

import io.demograph.monotonic.`var`.AtomicMVar.SubscriberState
import io.demograph.monotonic.queue.{ LWWQueue, Queue }
import org.reactivestreams.{ Publisher, Subscriber, Subscription }

/**
 *
 */
class AtomicVar[V](initialValue: V) extends Var[V] {

  protected val ref: AtomicReference[V] = new AtomicReference[V](initialValue)

  override def sample: V = ref.get()

  override protected[`var`] def _set(value: V): V = {
    val previous = ref.getAndSet(value)
    updateSubscribers(enqueue(value) andThen dispatchReadyElements)
    previous
  }

  override protected[`var`] def _update(f: V ⇒ V): V = {
    val previous = ref.getAndUpdate(new UnaryOperator[V] {
      override def apply(currentState: V): V = f(currentState)
    })
    updateSubscribers(enqueue(f(previous)) andThen dispatchReadyElements)
    previous
  }

  val index = new AtomicLong(0L)
  val atomicMap = new ConcurrentHashMap[Long, SubscriberState[_ >: V]]()

  override val publisher: Publisher[V] = new Publisher[V] {
    override def subscribe(s: Subscriber[_ >: V]): Unit = {

      val newIndex = index.incrementAndGet()
      val state = SubscriberState[V](s.asInstanceOf[Subscriber[V]], 0L, queueFromCurrentState)

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

  protected def queueFromCurrentState: Queue[V] = new LWWQueue[V](Some(sample))

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
