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

package io.demograph.crdt.lattice.lexographic

import algebra.{ Eq, Order, PartialOrder }
import algebra.lattice.{ BoundedJoinSemilattice, JoinSemilattice }
import io.demograph.crdt.lattice._
import cats.syntax.partialOrder._
/**
 *
 */
object LexProductInstances extends LexProductSyntax {

  /**
   * We can compare lattice product's for equivalence given an Eq for both sides
   */
  implicit def lexProductEq[S: Eq, T: Eq]: Eq[S :⊠: T] = (x: S :⊠: T, y: S :⊠: T) => {
    val leftEq = Eq[S].eqv(x.left, y.left)
    lazy val rightEq = Eq[T].eqv(x.right, y.right)
    leftEq && rightEq
  }

  def defaultJoin[S: JoinSemilattice: Eq, T: JoinSemilattice: Eq](lhs: S :⊠: T, rhs: S :⊠: T): S :⊠: T = {
    val sOrder: PartialOrder[S] = JoinSemilattice[S].joinPartialOrder

    sOrder.partialCompare(lhs.left, rhs.left) match {
      case -1 ⇒ rhs
      case 1 ⇒ lhs
      case 0 ⇒ (lhs.left :⊠: (lhs.right ⊔ rhs.right))
      case Double.NaN ⇒ (lhs.left ⊔ rhs.left) :⊠: (lhs.right ⊔ rhs.right) // If `S` is a Chain, this clause is never hit
    }
  }

  def bottomJoin[S: JoinSemilattice: Eq, T: BoundedJoinSemilattice: Eq](lhs: S :⊠: T, rhs: S :⊠: T): S :⊠: T = {
    val sOrder: PartialOrder[S] = JoinSemilattice[S].joinPartialOrder

    sOrder.partialCompare(lhs.left, rhs.left) match {
      case -1 ⇒ rhs
      case 1 ⇒ lhs
      case 0 ⇒ (lhs.left :⊠: (lhs.right ⊔ rhs.right))
      case Double.NaN ⇒ (lhs.left ⊔ rhs.left) :⊠: rhs.right.bottom // If `S` is a Chain, this clause is never hit
    }
  }

  def bottomProduct[S: BoundedJoinSemilattice, T: BoundedJoinSemilattice]: S :⊠: T =
    BoundedJoinSemilattice[S].zero :⊠: BoundedJoinSemilattice[T].zero

  /**
   * A lexicographic product of a JSL and a bounded JSL is a JSL; When the first element's ordering is undefined,
   * we take bottom for the second element
   */
  class LexProductJSL[S: JoinSemilattice: Eq, T: BoundedJoinSemilattice: Eq] extends JoinSemilattice[S :⊠: T] {
    override def join(lhs: S :⊠: T, rhs: S :⊠: T): S :⊠: T = bottomJoin(lhs, rhs)
  }

  /**
   * A lexicographic product of two JSL's is a JSL; we take the largest or merge when ordering is undefined
   */
  class LexProductJSL2[S: JoinSemilattice: Eq, T: JoinSemilattice: Eq] extends JoinSemilattice[S :⊠: T] {
    override def join(lhs: S :⊠: T, rhs: S :⊠: T): S :⊠: T = defaultJoin(lhs, rhs)
  }

  /**
   * Two bounded JSL's create a bounded JSL product. Its bottom is the product of their respective bottoms
   */
  class LexProductBJSL[S: BoundedJoinSemilattice: Eq, T: BoundedJoinSemilattice: Eq] extends LexProductJSL[S, T] with BoundedJoinSemilattice[S :⊠: T] {
    override def zero: S :⊠: T = bottomProduct
  }

  /**
   * The product of two Chain's is itself a Chain
   */
  class LexProductChain[S: Chain, T: Chain] extends Chain[S :⊠: T] {
    override def compare(x: S :⊠: T, y: S :⊠: T): Int = {
      math.signum(Order[S].compare(x.left, y.left)) match {
        case 0 ⇒ Order[T].compare(x.right, y.right)
        case x ⇒ x
      }
    }

    override def join(lhs: S :⊠: T, rhs: S :⊠: T): S :⊠: T = defaultJoin(lhs, rhs)
  }

}
