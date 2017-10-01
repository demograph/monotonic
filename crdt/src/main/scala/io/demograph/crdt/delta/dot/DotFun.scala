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
 * the generic DotFun[V] is a map from dots to some JoinSemilattice V
 *
 * @param dotFun
 * @param ev$1 Proof that V is a JoinSemilattice, note that the paper requires it to be a Lattice. Not sure if that was
 *             a case of brevity, or if that is actually a requirement... guess we'll find out soon enough :)
 * @tparam I
 * @tparam V
 */
case class DotFun[I, V: JoinSemilattice](dotFun: Map[Dot[I], V]) {
  val dots: Dots[I] = dotFun.keys.toSet
}
object DotFun {

  /**
   * Produces an empty Dot-function
   */
  def empty[I, V: JoinSemilattice]: DotFun[I, V] = new DotFun(Map.empty)

  /**
   * Produces a singleton Dot-function
   */
  def single[I, V: JoinSemilattice](i: I, v: Int, value: V): DotFun[I, V] = single(Dot(i, v), value)

  /**
   * Produces a singleton Dot-function
   */
  def single[I, V: JoinSemilattice](dot: Dot[I], v: V): DotFun[I, V] = new DotFun(Map(dot → v))
}
