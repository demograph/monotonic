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

package io.demograph.crdt.instances

import algebra.lattice.JoinSemilattice
import io.demograph.crdt.delta.causal.{ CausalCRDT, CausalContext }
import io.demograph.crdt.delta.dot.{ Dotted, DotFun }
/**
 *
 */
object MVRegister {
  type MVRegister[E, V] = CausalCRDT[E, DotFun[E, V]]

  def empty[E: Dotted, V: JoinSemilattice]: MVRegister[E, V] = CausalCRDT(DotFun.empty, CausalContext.empty)

  trait Query[E, V] {
    /**
     * Reading simply returns all values mapped in the store.
     */
    def read(register: MVRegister[E, V]): Iterable[V]
  }

  trait Mutate[E, V] {

    /**
     * the write delta mutator returns a causal context with all events in the store, so that they are removed upon join,
     * together with a single mapping from a new dot to the value written; as usual, the new dot is also put in the context
     */
    def write(register: MVRegister[E, V])(v: V): MVRegister[E, V]

    /**
     * A clear operation simply removes current events, leaving the register in the initial empty state
     */
    def clear(register: MVRegister[E, V]): MVRegister[E, V]
  }

}