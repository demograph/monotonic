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

package io.demograph.crdt.lattice

import algebra.laws.LatticeLaws
import cats.kernel.laws.OrderLaws
import io.demograph.crdt.Discipline
import io.demograph.crdt.lattice.ArbitraryLattices._
import io.demograph.crdt.lattice.primitives.IntegerLattice.Ascending._
import io.demograph.crdt.lattice.primitives._
import org.scalacheck.Arbitrary._
import org.scalacheck.Cogen
import org.scalatest.{ FunSuiteLike, Matchers }

/**
 *
 */
class LexographicProductTest extends FunSuiteLike with Discipline with Matchers {

  test("Lexographic Product should work") {
    val middle = 5 :⊠: 5
    val secondLower = 5 :⊠: 3
    val firstLower = 3 :⊠: 5
    val mixed = 3 :⊠: 10

    middle ⊔ secondLower should equal(5 :⊠: 5)
    middle ⊔ firstLower should equal(5 :⊠: 5)
    middle ⊔ mixed should equal(5 :⊠: 5)
  }

  test("Lexographic Product should be bounded when both operands are bounded") {
    (5 :⊠: 6).bottom should equal(Int.MinValue :⊠: Int.MinValue)
  }

  def lexProductJSLLaws = {
    LatticeLaws[Int :⊠: Int].boundedJoinSemilattice
  }

  def lexProductOrderLaws = {
    implicit val coGen: Cogen[Int :⊠: Int] = Cogen(x ⇒ (x.left.asInstanceOf[Long] << 32) + x.right)
    OrderLaws[Int :⊠: Int].order
  }

  checkAll("Lexographic Product JSL", lexProductJSLLaws)
  checkAll("Lexographic Product Order", lexProductOrderLaws)
}
