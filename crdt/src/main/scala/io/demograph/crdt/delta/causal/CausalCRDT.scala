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

package io.demograph.crdt.delta.causal

import io.demograph.crdt.delta.dot.DotStore

case class CausalCRDT[E, ES <: DotStore[E]](eventStore: ES, context: CausalContext[E])

object CausalCRDT {

  // A generic representation of a Causal CRDT. This is just the datastructure without any particular operations attached to it
  //  final case class Generic[E, ES <: DotStore[E]](eventStore: ES, context: CausalContext[E]) extends CausalCRDT[E, ES]

  //  class CausalCRDTBJSL[E, DS, T](implicit causal: Causal[E, DS], crdt: CausalCRDT[E, DS, T]) extends BoundedJoinSemilattice[T] {
  //    override def zero: T = crdt.instance(causal.zero._1, causal.zero._2)
  //
  //    override def join(lhs: T, rhs: T): T = {
  //      val joined: (DS, CausalContext[E]) = causal.join(
  //        (crdt.eventStore(lhs), crdt.context(lhs)),
  //        (crdt.eventStore(rhs), crdt.context(rhs)))
  //      crdt.instance(joined._1, joined._2)
  //    }
  //  }
}