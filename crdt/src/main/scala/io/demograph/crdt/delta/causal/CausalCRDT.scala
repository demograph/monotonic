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

import algebra.lattice.BoundedJoinSemilattice

trait CausalCRDT[I, DS, T] {

  /**
   * Retrieves the instance performing the role of dot-store.
   * @param t
   * @return
   */
  def dotStore(t: T): DS

  /**
   * Returns the CausalContext for this Causal CRDT
   * @param t
   * @return
   */
  def context(t: T): CausalContext[I]

  /**
   * Allows the creation of a new instance of T, given a dotStore and causalContext.
   * @param dotStore
   * @param context
   * @return
   */
  def instance(dotStore: DS, context: CausalContext[I]): T
}

object CausalCRDT {

  class CausalCRDTBJSL[I, DS, T](implicit causal: Causal[I, DS], crdt: CausalCRDT[I, DS, T]) extends BoundedJoinSemilattice[T] {
    override def zero: T = crdt.instance(causal.zero._1, causal.zero._2)

    override def join(lhs: T, rhs: T): T = {
      val joined: (DS, CausalContext[I]) = causal.join(
        (crdt.dotStore(lhs), crdt.context(lhs)),
        (crdt.dotStore(rhs), crdt.context(rhs)))
      crdt.instance(joined._1, joined._2)
    }
  }
}