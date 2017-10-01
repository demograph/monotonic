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
object ORMap {
  type DS[I, K, V] = DotMap[I, K, V]
  type CRDT[I, K, V, C] = CausalCRDT[I, DS[I, K, V], ORMap[I, K, V, C]]

  def empty[I, K, V, C](implicit ds: DotStore[V, I], causal: CausalCRDT[I, V, C]): ORMap[I, K, V, C] =
    ORMap(DotMap.empty, CausalContext.empty)
}

case class ORMap[I, K, V, C](dotStore: ORMap.DS[I, K, V], causalContext: CausalContext[I])(implicit ds: DotStore[V, I], causal: CausalCRDT[I, V, C]) {

  def apply(o: C ⇒ C, k: K): ORMap[I, K, V, C] = {
    val vc: C = o(causal.instance(dotStore.dotMap.getOrElse(k, ds.zero), causalContext))
    ORMap(DotMap(k → causal.dotStore(vc)), causal.context(vc))
  }

  def remove(k: K): ORMap[I, K, V, C] = ORMap(
    DotMap.empty,
    CausalContext(ds.dots(dotStore.dotMap.getOrElse(k, ds.zero))))

  def clear: ORMap[I, K, V, C] = ORMap(
    DotMap.empty,
    CausalContext(dotStore.dots))

  def contains(key: K): Boolean = dotStore.contains(key)

  def size: Int = dotStore.size

  def get(key: K): Option[C] = dotStore.dotMap.get(key).map(v ⇒ causal.instance(v, causalContext))
}