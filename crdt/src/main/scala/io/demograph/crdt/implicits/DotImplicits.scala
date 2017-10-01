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
import io.demograph.crdt.delta.dot.DotInstances.{ DotFunStore, DotMapStore, DotSetStore }
import io.demograph.crdt.delta.dot.{ DotFun, DotMap, DotSet, DotStore }

/**
 *
 */
trait DotImplicits {

  implicit def dotSetStore[I]: DotStore[DotSet[I], I] = new DotSetStore[I]

  implicit def dotFunStore[I, V: JoinSemilattice]: DotStore[DotFun[I, V], I] = new DotFunStore[I, V]

  implicit def dotMapStore[I, K, V](implicit ds: DotStore[V, I]): DotStore[DotMap[I, K, V], I] = new DotMapStore[I, K, V]

}
