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

package io.demograph.crdt.delta.dot

import algebra.lattice.JoinSemilattice
/**
 * the generic DotFun[V] is a map from events to some JoinSemilattice V
 *
 * @param dotFun
 * @tparam E
 * @tparam V
 */
case class DotFun[E, V: JoinSemilattice](dotFun: Map[E, V]) extends DotStore[E] {
  override lazy val dots: Set[E] = dotFun.keySet
}
object DotFun {

  /**
   * Produces an empty Dot-function
   */
  def empty[E, V: JoinSemilattice]: DotFun[E, V] = new DotFun(Map.empty)

  /**
   * Produces a singleton Dot-function
   */
  def single[E, V: JoinSemilattice](dot: E, v: V): DotFun[E, V] = new DotFun(Map(dot → v))

  /**
   * Produces an Event function based on the given dot-value pairs
   * @param dotValues
   * @tparam E
   * @tparam V
   * @return
   */
  def apply[E, V: JoinSemilattice](dotValues: (E, V)*): DotFun[E, V] = new DotFun(Map(dotValues: _*))
}
