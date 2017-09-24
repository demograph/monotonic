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

package com.github.mboogerd.crdt.delta.dot

import algebra.lattice.JoinSemilattice
import com.github.mboogerd.crdt.syntax.all._

/**
 *
 */
object DotInstances {

  class DotSetStore[I] extends DotStore[DotSet[I], I] {
    override def dots(store: DotSet[I]): Dots[I] = store.dots

    override def zero: DotSet[I] = DotSet.empty

    override def join(lhs: DotSet[I], rhs: DotSet[I]): DotSet[I] = DotSet(lhs.dots ++ rhs.dots)
  }

  class DotFunStore[I, V: JoinSemilattice] extends DotStore[DotFun[I, V], I] {
    override def dots(store: DotFun[I, V]): Dots[I] = store.dots

    override def zero: DotFun[I, V] = DotFun.empty

    override def join(lhs: DotFun[I, V], rhs: DotFun[I, V]): DotFun[I, V] = DotFun(lhs.dotFun join rhs.dotFun)
  }

  class DotMapStore[I, K, V](implicit ds: DotStore[V, I]) extends DotStore[DotMap[I, K, V], I] {
    override def dots(store: DotMap[I, K, V]): Dots[I] = store.dots

    override def zero: DotMap[I, K, V] = DotMap.empty

    override def join(lhs: DotMap[I, K, V], rhs: DotMap[I, K, V]): DotMap[I, K, V] = DotMap(lhs.dotMap join rhs.dotMap)
  }

}
