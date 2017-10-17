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
import io.demograph.crdt.Session
import io.demograph.crdt.delta.causal.{ CausalContext, CopCC, DotCC, ProCC }
import io.demograph.crdt.delta.dot._
import io.demograph.crdt.util.Empty

/**
 *
 */
trait DotImplicits {

  implicit def emptyEventSet[E]: Empty[DotSet[E]] = new Empty[DotSet[E]] {
    override def empty: DotSet[E] = DotSet.empty
  }

  implicit def emptyEventFun[E, V: JoinSemilattice]: Empty[DotFun[E, V]] = new Empty[DotFun[E, V]] {
    override def empty: DotFun[E, V] = DotFun.empty
  }

  implicit def emptyEventMap[E, K, V <: DotStore[E]]: Empty[DotMap[E, K, V]] = new Empty[DotMap[E, K, V]] {
    override def empty: DotMap[E, K, V] = DotMap.empty
  }

  // TODO: Verify whether we need a Session instance for H
  implicit def dottedDot[H]: Dotted[Dot[H]] = new Dotted[Dot[H]] {
    override def causalContext: CausalContext[Dot[H]] = DotCC(CompactDotSet.empty)
  }
  implicit def dottedProduct[L: Dotted, R: Dotted]: Dotted[(L, R)] = new Dotted[(L, R)] {
    override def causalContext: CausalContext[(L, R)] = ProCC(Set.empty, Set.empty)
  }
  implicit def dottedCoproduct[L: Dotted, R: Dotted]: Dotted[Either[L, R]] = new Dotted[Either[L, R]] {
    override def causalContext: CausalContext[Either[L, R]] = CopCC(Set.empty, Set.empty)
  }

  implicit class DotSource[H: Session](cc: CausalContext[Dot[H]]) {
    def nextDot: Dot[H] = cc match {
      case dcc @ DotCC(_) ⇒ dcc.nextDot(Session[H].localhost)
      case other ⇒
        // Although technically we can extract the next dot from any dot-containing CausalContext, they are only meant
        // to be extracted from `DotCC`. If we encounter something else, something is definitely amiss!
        throw new IllegalArgumentException(s"Failed to compute the next available dot for replica `${Session[H].localhost}` because the supplied CausalContext was not a `DotCC`:\n$other")
    }
  }
}

object DotImplicits extends DotImplicits