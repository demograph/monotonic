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

import algebra.lattice.JoinSemilattice
import io.demograph.crdt.delta.causal.Causal
import io.demograph.crdt.delta.causal.CausalInstances._
import io.demograph.crdt.delta.dot._

/**
 *
 */
trait CausalImplicits {

  /* implicit Causal instances */
  implicit def causalDotSet[I]: Causal[I, DotSet[I]] = new CausalDotSet[I]

  implicit def causalDotFun[I, V: JoinSemilattice]: Causal[I, DotFun[I, V]] = new CausalDotFun[I, V]

  implicit def causalDotMap[I, K, V](implicit dotStore: DotStore[V, I], causalV: Causal[I, V]): Causal[I, DotMap[I, K, V]] = new CausalDotMap[I, K, V]

  implicit def causalDotStoreProduct[I, CDS1, CDS2](implicit ds1: DotStore[CDS1, I], ds2: DotStore[CDS2, I], c1: Causal[I, CDS1], c2: Causal[I, CDS2]): Causal[I, (CDS1, CDS2)] = new CausalDotStoreProduct[I, CDS1, CDS2]
}
