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

package com.github.mboogerd.crdt.lattice

import algebra.laws.LatticeLaws
import com.github.mboogerd.crdt.Discipline
import com.github.mboogerd.crdt.lattice.ArbitraryLattices._
import com.github.mboogerd.crdt.lattice.linearsum._
import com.github.mboogerd.crdt.lattice.primitives.IntegerLattice
import org.scalatest.{ FunSuiteLike, Matchers }
/**
 *
 */
class LinearSumTest extends FunSuiteLike with Discipline with Matchers {

  test("Linear sum should work") {
    import IntegerLattice.Ascending.AscIntegerLattice

    val first: Int :⊕: Int = 10 :⊕: 5
    first should matchPattern { case Right(5) ⇒ }

    val second: Int :⊕: Int = Left[Int, Int](15)
    first ⊔ second should matchPattern { case Right(5) ⇒ }

    val third: Int :⊕: Int = Left[Int, Int](5)
    third ⊔ second should matchPattern { case Left(15) ⇒ }

    val fourth: Int :⊕: Int = Right[Int, Int](10)
    first ⊔ fourth should matchPattern { case Right(10) ⇒ }
  }

  def linearSumJSLLaws = {
    implicit val ascInt = IntegerLattice.Ascending.AscIntegerLattice
    LatticeLaws[Int :⊕: Int].boundedJoinSemilattice
  }

  checkAll("Linear sum JSL", linearSumJSLLaws)
}