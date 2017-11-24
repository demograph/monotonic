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

import java.util.function.UnaryOperator

import algebra.lattice.{ BoundedJoinSemilattice, JoinSemilattice }
import io.demograph.monotonic.queue.{ MergingQueue, Queue }
import org.reactivestreams.Subscriber

/**
 * A variable whose content is monotonic (all operations are monotonic), and whose content can be accessed / manipulated
 * in an atomic fashion.
 */
class AtomicMVar[V: JoinSemilattice](initialValue: V) extends AtomicVar[V](initialValue) with MVar[V] {

  /**
   * Note that `set` here means really `inflate`. The method ensures that the update is an inflation w.r.t. the
   * previous state.
   */
  override protected[`var`] def _set(delta: V): V = {
    val previous = ref.getAndUpdate(new UnaryOperator[V] {
      override def apply(currentState: V): V = JoinSemilattice.join(currentState, delta)
    })
    updateSubscribers(enqueue(delta) andThen dispatchReadyElements)
    previous
  }

  /**
   * @param f The function to apply (note that this function should be without side-effects.
   *          In the current implementation, this function _will_ be applied twice!
   * @return The previous state of this `MVar`
   */
  override protected[`var`] def _update(f: V ⇒ V): V = {
    val oldState = ref.getAndUpdate(new UnaryOperator[V] {
      override def apply(currentState: V): V = JoinSemilattice.join(currentState, f(currentState))
    })
    updateSubscribers(enqueue(f(oldState)) andThen dispatchReadyElements)
    oldState
  }

  override protected def queueFromCurrentState = new MergingQueue[V](Some(sample))
}

object AtomicMVar {

  def apply[V: JoinSemilattice](initialValue: V): MVar[V] = new AtomicMVar[V](initialValue)

  def apply[V: BoundedJoinSemilattice](): MVar[V] = apply(BoundedJoinSemilattice[V].zero)

  case class SubscriberState[V](subscriber: Subscriber[V], demand: Long, queue: Queue[V])
}