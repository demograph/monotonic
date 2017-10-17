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

import io.demograph.crdt.Session
import io.demograph.crdt.delta.causal.{ CausalCRDT, CausalContext }
import io.demograph.crdt.delta.dot.{ Dot, DotMap, DotStore }
import io.demograph.crdt.instances.ORMap.{ Mutate, ORMap, Query }
import io.demograph.crdt.util.Empty

/**
 *
 */
trait ORMapImplicits {

  implicit class ORMapQueryOps[E, K, V <: DotStore[E]](map: ORMap[E, K, V]) extends Query[E, K, V] {
    override def contains(key: K): Boolean = map.eventStore.contains(key)

    override def size: Int = map.eventStore.size

    override def get(key: K): Option[V] = map.eventStore.dotMap.get(key)

    override def iterable: Iterable[(K, V)] = map.eventStore.dotMap
  }

  implicit class ORMapMutateOps[H: Session, K, V <: DotStore[Dot[H]]: Empty](map: ORMap[Dot[H], K, V]) extends Mutate[Dot[H], K, V] {
    override def mutateValue(f: CausalCRDT[Dot[H], V] ⇒ CausalCRDT[Dot[H], V], key: K): ORMap[Dot[H], K, V] = {
      val newValue = f(CausalCRDT(map.eventStore.dotMap.getOrElse(key, Empty[V].empty), map.context))
      CausalCRDT(DotMap(key → newValue.eventStore), newValue.context)
    }

    override def remove(key: K): ORMap[Dot[H], K, V] =
      CausalCRDT(DotMap.empty, CausalContext.from(map.eventStore.dotMap.getOrElse(key, Empty[V].empty).dots))

    override def clear: ORMap[Dot[H], K, V] =
      CausalCRDT(DotMap.empty, CausalContext.from(map.eventStore.dots))
  }
}

object ORMapImplicits extends ORMapImplicits
