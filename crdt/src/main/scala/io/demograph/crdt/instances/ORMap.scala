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

import io.demograph.crdt.delta.causal.{ CausalCRDT, CausalContext }
import io.demograph.crdt.delta.dot._

/**
 *
 */
object ORMap {
  type ORMap[E, K, V <: DotStore[E]] = CausalCRDT[E, DotMap[E, K, V]]

  def empty[E: Dotted, K, V <: DotStore[E]]: ORMap[E, K, V] = CausalCRDT(DotMap.empty, CausalContext.empty)

  trait Query[E, K, V <: DotStore[E]] {
    def contains(key: K): Boolean

    def size: Int

    def get(key: K): Option[V]

    def iterable: Iterable[(K, V)]
  }

  trait Mutate[E, K, V <: DotStore[E]] {
    def mutateValue(f: CausalCRDT[E, V] ⇒ CausalCRDT[E, V], key: K): ORMap[E, K, V]

    def remove(key: K): ORMap[E, K, V]

    def clear: ORMap[E, K, V]
  }

}