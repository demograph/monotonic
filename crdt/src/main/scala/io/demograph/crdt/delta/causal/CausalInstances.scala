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

import algebra.lattice.{ BoundedJoinSemilattice, JoinSemilattice }
import cats.instances.map._
import cats.syntax.monoid._
import io.demograph.crdt.delta.dot._
import io.demograph.crdt.implicits.ShimImplicits._
/**
 *
 */
object CausalInstances {

  class DotSetCRDTLattice[E: Dotted] extends BoundedJoinSemilattice[EventSetCRDT[E]] {

    override def zero: EventSetCRDT[E] = CausalCRDT(DotSet.empty, CausalContext.empty)

    override def join(lhs: EventSetCRDT[E], rhs: EventSetCRDT[E]): EventSetCRDT[E] = {
      val lDS = lhs.eventStore.dots
      val lCC = lhs.context
      val rDS = rhs.eventStore.dots
      val rCC = rhs.context

      val eventSet = DotSet((lDS intersect rDS) union (lDS diff rCC) union (rDS diff lCC))
      CausalCRDT(eventSet, lCC union rCC)
    }
  }

  class DotFunCRDTLattice[E: Dotted, V: JoinSemilattice] extends BoundedJoinSemilattice[EventFunCRDT[E, V]] {

    override def zero: EventFunCRDT[E, V] = CausalCRDT(DotFun.empty, CausalContext.empty)

    override def join(lhs: EventFunCRDT[E, V], rhs: EventFunCRDT[E, V]): EventFunCRDT[E, V] = {
      val lDF: Map[E, V] = lhs.eventStore.dotFun
      val lCC = lhs.context
      val rDF: Map[E, V] = rhs.eventStore.dotFun
      val rCC = rhs.context

      val leftMap: Map[E, V] = lDF.filterKeys(rDF.keys.toSet.contains)
      val leftCCMap = lDF.filterKeys(rCC.contains)
      val rightMap: Map[E, V] = rDF.filterKeys(lDF.keys.toSet.contains)
      val rightCCMap = rDF.filterKeys(lCC.contains)

      CausalCRDT(DotFun(leftMap combine rightMap ++ leftCCMap ++ rightCCMap), lCC union rCC)
    }
  }

  class DotMapCRDTLattice[E: Dotted, K, V <: DotStore[E]](implicit vLattice: BoundedJoinSemilattice[CausalCRDT[E, V]])
    extends BoundedJoinSemilattice[EventMapCRDT[E, K, V]] {

    override def zero: EventMapCRDT[E, K, V] = CausalCRDT(DotMap.empty, CausalContext.empty)

    override def join(lhs: EventMapCRDT[E, K, V], rhs: EventMapCRDT[E, K, V]): EventMapCRDT[E, K, V] = {
      def v(k: K): V = vLattice.join(
        CausalCRDT(lhs.eventStore.dotMap.getOrElse(k, vLattice.zero.eventStore), lhs.context),
        CausalCRDT(rhs.eventStore.dotMap.getOrElse(k, vLattice.zero.eventStore), rhs.context)).eventStore

      val keySet = lhs.eventStore.dotMap.keys.toSet union rhs.eventStore.dotMap.keys.toSet
      val data: Map[K, V] = keySet.map(key ⇒ key → v(key)).filterNot { case (_, v) ⇒ v == vLattice.zero.eventStore }.toMap

      CausalCRDT(DotMap(data), lhs.context union rhs.context)
    }
  }
}
