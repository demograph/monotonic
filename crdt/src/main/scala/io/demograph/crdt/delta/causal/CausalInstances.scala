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

import algebra.lattice.JoinSemilattice
import cats.implicits._
import io.demograph.crdt.delta.dot._
import io.demograph.crdt.syntax.all._
/**
 *
 */
object CausalInstances {

  class CausalDotSet[I] extends Causal[I, DotSet[I]] {

    override def zero: (DotSet[I], CausalContext[I]) = (DotSet.empty, CausalContext.empty)

    override def join(lhs: (DotSet[I], CausalContext[I]), rhs: (DotSet[I], CausalContext[I])): (DotSet[I], CausalContext[I]) = {
      val lDS = lhs._1.dots
      val lCC = lhs._2.dots
      val rDS = rhs._1.dots
      val rCC = rhs._2.dots

      (DotSet((lDS intersect rDS) union (lDS diff rCC) union (rDS diff lCC)), CausalContext(lCC union rCC))
    }
  }

  class CausalDotFun[I, V: JoinSemilattice] extends Causal[I, DotFun[I, V]] {

    override def zero: (DotFun[I, V], CausalContext[I]) = (DotFun.empty, CausalContext.empty)

    override def join(lhs: (DotFun[I, V], CausalContext[I]), rhs: (DotFun[I, V], CausalContext[I])): (DotFun[I, V], CausalContext[I]) = {
      val lDF: Map[Dot[I], V] = lhs._1.dotFun
      val lCC: Dots[I] = lhs._2.dots
      val rDF: Map[Dot[I], V] = rhs._1.dotFun
      val rCC: Dots[I] = rhs._2.dots

      val leftMap: Map[Dot[I], V] = lDF.filterKeys(rDF.keys.toSet.contains)
      val leftCCMap = lDF.filterKeys(rCC.contains)
      val rightMap: Map[Dot[I], V] = rDF.filterKeys(lDF.keys.toSet.contains)
      val rightCCMap = rDF.filterKeys(lCC.contains)

      (DotFun(leftMap combine rightMap ++ leftCCMap ++ rightCCMap), CausalContext(lCC union rCC))
    }
  }

  class CausalDotMap[I, K, V](implicit dotStore: DotStore[V, I], causalV: Causal[I, V]) extends Causal[I, DotMap[I, K, V]] {

    override def zero: (DotMap[I, K, V], CausalContext[I]) = (DotMap.empty, CausalContext.empty)

    override def join(lhs: (DotMap[I, K, V], CausalContext[I]), rhs: (DotMap[I, K, V], CausalContext[I])): (DotMap[I, K, V], CausalContext[I]) = {
      def v(k: K): V = causalV.join(
        (lhs._1.dotMap.getOrElse(k, dotStore.zero), lhs._2),
        (rhs._1.dotMap.getOrElse(k, dotStore.zero), rhs._2))._1

      val keySet = lhs._1.dotMap.keys.toSet union rhs._1.dotMap.keys.toSet
      (
        DotMap(keySet.map(key ⇒ key → v(key)).filterNot { case (_, v) ⇒ v == dotStore.zero }.toMap),
        CausalContext(lhs._2.dots union rhs._2.dots))
    }
  }

  class CausalDotStoreProduct[I, CDS1, CDS2](implicit
    ds1: DotStore[CDS1, I],
    ds2: DotStore[CDS2, I],
    c1: Causal[I, CDS1],
    c2: Causal[I, CDS2]) extends Causal[I, (CDS1, CDS2)] {

    override def zero: ((CDS1, CDS2), CausalContext[I]) =
      ((c1.zero._1, c2.zero._1), CausalContext(c1.zero._2.dots union c2.zero._2.dots))

    override def join(
      lhs: ((CDS1, CDS2), CausalContext[I]),
      rhs: ((CDS1, CDS2), CausalContext[I])): ((CDS1, CDS2), CausalContext[I]) = {
      val j1: (CDS1, CausalContext[I]) = c1.join((lhs._1._1, lhs._2), (rhs._1._1, rhs._2))
      val j2: (CDS2, CausalContext[I]) = c2.join((lhs._1._2, lhs._2), (rhs._1._2, rhs._2))
      ((j1._1, j2._1), CausalContext(j1._2.dots union j2._2.dots))
    }
  }
}
