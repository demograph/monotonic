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
import cats.kernel.Eq
import io.demograph.crdt.LatticeSpec
import io.demograph.crdt.lattice.ArbitraryLattices._
import io.demograph.crdt.lattice.LatticeSyntax._
import io.demograph.crdt.lattice.primitives.SingletonLattice._
import io.demograph.crdt.lattice.primitives._
import io.demograph.crdt.lattice.product._
import io.demograph.crdt.implicits.all._
import org.scalacheck.Arbitrary._
/**
 *
 */
class ProductLatticeTest extends LatticeSpec {

  test("ProductLattice should allow product of boolean lattices with the singleton") {
    import BooleanLattice.Ascending._
    val x = false :×: ⊥
    val y = true :×: ⊥

    x ⊔ y should equal(true :×: ⊥)
  }

  test("ProductLattice should allow product of ascending int lattices") {
    import IntegerLattice.Ascending._

    val x = 5 :×: 10
    val y = 8 :×: 2

    (x ⊔ y) should equal(8 :×: 10)
  }

  test("ProductLattice should allow product of descending int lattices") {
    import IntegerLattice.Descending._

    val x = 5 :×: 10
    val y = 8 :×: 2

    (x ⊔ y) should equal(5 :×: 2)
  }

  test("ProductLattice should be a bounded lattice if composed of bounded lattices") {
    import BooleanLattice.Ascending._
    import IntegerLattice.Ascending._
    (⊥ :×: ⊥).bottom should be(⊥ :×: ⊥)
    (true :×: 5).bottom should be(false :×: Int.MinValue)
  }
  //  def ascNatlatticeLaws = {
  //    implicit val ascNat = IntegerLattice.Ascending.AscIntegerLattice
  //    implicit val arbNat = arbitraryNat
  //    LatticeLaws[Int :×: Int].boundedJoinSemilattice
  //  }
  //
  //  def ascBoollatticeLaws = {
  //    implicit val ascBoolean = BooleanLattice.Ascending.ascBooleanLattice
  //    LatticeLaws[Boolean :×: Boolean].boundedJoinSemilattice
  //  }
}

trait ProductLatticeLaws {
  this: LatticeSpec ⇒

  import BooleanLattice.Ascending._
  import IntegerLattice.Ascending._

  checkAll("ProductIntLattice", LatticeLaws[Int :×: Int].boundedJoinSemilattice)
  checkAll("ProductBoolLattice", LatticeLaws[Boolean :×: Boolean].boundedJoinSemilattice)
}