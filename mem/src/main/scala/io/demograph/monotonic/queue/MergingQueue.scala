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

package io.demograph.monotonic.queue

import algebra.lattice.JoinSemilattice
import cats.kernel.Semigroup

import scala.collection.TraversableOnce

object MergingQueue {
  def empty[V: JoinSemilattice]: MergingQueue[V] = MergingQueue()
}
case class MergingQueue[V: JoinSemilattice](state: Option[V] = None) extends Queue[V] {
  override type Repr = MergingQueue[V]

  implicit def semigroupJSL[X: JoinSemilattice]: Semigroup[X] = (x: X, y: X) => JoinSemilattice[X].join(x, y)

  override def enqueue(v: V): MergingQueue[V] = MergingQueue(Some(Semigroup.maybeCombine[V](v, state)))

  override def dequeue(count: Int): (Int, TraversableOnce[V], MergingQueue[V]) = {
    if (count > 0)
      (count - 1, state, MergingQueue.empty)
    else
      (count, Traversable.empty, this)
  }

  /**
   * @return true if this queue contains no elements, false otherwise
   */
  override def isEmpty: Boolean = state.isEmpty
}