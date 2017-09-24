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

package com.github.mboogerd.crdt.delta.register

import algebra.lattice.JoinSemilattice
import com.github.mboogerd.crdt.delta.causal.CausalContext
import com.github.mboogerd.crdt.delta.dot.{ DotFun, DotStore }

/**
 *
 */
object MVRegister {
  def empty[I, V: JoinSemilattice](implicit ds: DotStore[DotFun[I, V], I]): MVRegister[I, V] =
    MVRegister(DotFun.empty, CausalContext.empty)
}
case class MVRegister[I, V: JoinSemilattice](dotStore: DotFun[I, V], context: CausalContext[I])(implicit ds: DotStore[DotFun[I, V], I]) {

  /**
   * the write delta mutator returns a causal context with all dots in the store, so that they are removed upon join,
   * together with a single mapping from a new dot to the value written; as usual, the new dot is also put in the context
   */
  def write(i: I)(v: V): MVRegister[I, V] = {
    val next = context.nextDot(i)
    MVRegister(DotFun.single(next, v), CausalContext(dotStore.dots + next))
  }

  /**
   * A clear operation simply removes current dots, leaving the register in the initial empty state
   */
  def clear(i: I): MVRegister[I, V] = MVRegister(DotFun.empty, CausalContext(dotStore.dots))

  /**
   * Reading simply returns all values mapped in the store.
   */
  def read: Iterable[V] = dotStore.dotFun.values
}
