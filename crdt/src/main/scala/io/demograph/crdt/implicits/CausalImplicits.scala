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

package io.demograph.crdt.implicits

import algebra.lattice.{ BoundedJoinSemilattice, JoinSemilattice }
import io.demograph.crdt.delta.causal.CausalInstances._
import io.demograph.crdt.delta.causal.{ CausalCRDT, _ }
import io.demograph.crdt.delta.dot.{ Dotted, DotStore }

/**
 *
 */
trait CausalImplicits {

  /* implicit Causal instances */
  implicit def causalDotSet[E: Dotted]: BoundedJoinSemilattice[EventSetCRDT[E]] = new DotSetCRDTLattice[E]

  implicit def causalDotFun[E: Dotted, V: JoinSemilattice]: BoundedJoinSemilattice[EventFunCRDT[E, V]] = new DotFunCRDTLattice[E, V]

  implicit def causalDotMap[E: Dotted, K, V <: DotStore[E]](implicit vLattice: BoundedJoinSemilattice[CausalCRDT[E, V]]): BoundedJoinSemilattice[EventMapCRDT[E, K, V]] =
    new DotMapCRDTLattice[E, K, V]

  //  implicit def causalDotStoreProduct[E, CDS1, CDS2](implicit ds1: DotStore[CDS1, E], ds2: DotStore[CDS2, E], c1: Causal[E, CDS1], c2: Causal[E, CDS2]): Causal[E, (CDS1, CDS2)] = new CausalDotStoreProduct[E, CDS1, CDS2]
}
