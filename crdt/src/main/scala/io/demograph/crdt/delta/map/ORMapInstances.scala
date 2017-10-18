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

package io.demograph.crdt.delta.map

import io.demograph.crdt.delta.causal.{ CausalCRDT, CausalContext }
import io.demograph.crdt.delta.dot.{ DotMap, DotStore }

/**
 *
 */
object ORMapInstances {

  class CausalCRDTorMap[I, K, V, C](implicit ds: DotStore[V, I], causal: CausalCRDT[I, V, C]) extends CausalCRDT[I, DotMap[I, K, V], ORMap[I, K, V, C]] {

    override def dotStore(t: ORMap[I, K, V, C]): DotMap[I, K, V] = t.dotStore

    override def context(t: ORMap[I, K, V, C]): CausalContext[I] = t.causalContext

    override def instance(dotStore: DotMap[I, K, V], context: CausalContext[I]): ORMap[I, K, V, C] = ORMap(dotStore, context)
  }
}