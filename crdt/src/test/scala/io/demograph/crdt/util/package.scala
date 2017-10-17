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

package io.demograph.crdt

import algebra.lattice.JoinSemilattice
import io.demograph.crdt.delta.dot.{ DotFun, DotMap, DotSet, DotStore }
import io.demograph.crdt.instances.AWSet.AWSet
import io.demograph.crdt.instances.ORMap.ORMap
import io.demograph.crdt.instances.RWSet.RWSet

/**
 *
 */
package object util extends ScalaTestImplicits {

  def traversableAWSet[E, A]: AWSet[E, A] ⇒ Traversable[A] = _.eventStore.domain
  def traversableRWSet[E, A]: RWSet[E, A] ⇒ Traversable[A] = _.eventStore.dotMap.collect { case (k, v) if !v.dotMap.contains(false) ⇒ k }
  def traversableORMap[E, K, V <: DotStore[E]]: ORMap[E, K, V] ⇒ Traversable[(K, V)] = _.eventStore.dotMap

  def traversableEventSet[E]: DotSet[E] ⇒ Traversable[E] = _.dots
  def traversableEventFun[E, V: JoinSemilattice]: DotFun[E, V] ⇒ Traversable[V] = _.dotFun.values
  def traversableEventMap[E, K, V <: DotStore[E]]: DotMap[E, K, V] ⇒ Traversable[K] = _.dotMap.keys

}
