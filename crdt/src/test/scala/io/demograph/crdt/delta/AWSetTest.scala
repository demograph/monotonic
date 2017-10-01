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

package io.demograph.crdt.delta

import algebra.lattice.BoundedJoinSemilattice
import io.demograph.crdt.TestSpec
import io.demograph.crdt.delta.causal.CausalContext
import io.demograph.crdt.delta.dot._
import io.demograph.crdt.delta.set.AWSet
import io.demograph.crdt.implicits.all._
import io.demograph.crdt.syntax.joinSyntax._
import io.demograph.crdt.gen.History._
import CRDTImplicits._
import org.scalatest.prop.Generator

/**
 *
 */
class AWSetTest extends TestSpec {

  behavior of "AWSet"

  /* Basic tests */

  it should "support delta-addition" in {
    // Create a basic AWSetGen, where identities are represented using Int's and the payload-type is Long
    val awSet = AWSet.empty[Int, Long]

    val awDelta: AWSet[Int, Long] = awSet.add(0)(-1)
    val expectedDots: Dots[Int] = Set(Dot(0, 1))
    val expectedAWSet: AWSet[Int, Long] = AWSet(DotMap(Map(-1l → DotSet(expectedDots))), CausalContext(expectedDots))

    awDelta shouldBe expectedAWSet

    val updated = joinSyntax(awSet) join awDelta

    updated shouldBe expectedAWSet
  }

  it should "support delta-removal" in {
    val dots: Dots[Int] = Set(Dot(0, 1))
    val dotMap = DotMap(Map(-1l → DotSet(dots)))
    val causalContext = CausalContext(dots)
    val awSet = AWSet(dotMap, causalContext)

    val awDelta = awSet.remove(0)(-1)

    val expectedAWSet: AWSet[Int, Long] = AWSet(DotMap.empty, CausalContext(dots))

    awDelta shouldBe expectedAWSet

    val updated = awSet join awDelta
    updated shouldBe expectedAWSet
  }

  it should "support clearing" in {
    val fst: Dots[Int] = Set(Dot(0, 1))
    val snd: Dots[Int] = Set(Dot(1, 1))
    val trd: Dots[Int] = Set(Dot(0, 1))
    val dots: Dots[Int] = Set(fst, snd, trd).flatten
    val dotMap = DotMap(Map(-1l → DotSet(fst), 0l → DotSet(snd), 1l → DotSet(trd)))
    val causalContext = CausalContext(dots)

    // Construct an AWSetGen with three added elements: {-1, 0, 1}
    val awSet: AWSet[Int, Long] = AWSet(dotMap, causalContext)
    val awDelta: AWSet[Int, Long] = awSet.clear(0)
    val expectedAWSet: AWSet[Int, Long] = AWSet(DotMap.empty, causalContext)

    awDelta shouldBe expectedAWSet

    val updated = awSet join awDelta
    updated shouldBe expectedAWSet
  }

  it should "support retrieving the current elements" in {
    val fst: Dots[Int] = Set(Dot(0, 1))
    val snd: Dots[Int] = Set(Dot(1, 1))
    val trd: Dots[Int] = Set(Dot(0, 1))
    val dots: Dots[Int] = Set(fst, snd, trd).flatten
    val dotMap = DotMap(Map(-1l → DotSet(fst), 0l → DotSet(snd), 1l → DotSet(trd)))
    val causalContext = CausalContext(dots)

    // Construct an AWSetGen with three added elements: {-1, 0, 1}
    val awSet: AWSet[Int, Long] = AWSet(dotMap, causalContext)

    awSet.elements should contain theSameElementsAs Set(-1l, 0l, 1l)
  }

  /* Property checks - Doesn't work anymore since migrating to newer scala-test. Requires undocumented props.Generator */
  //  it should "contain the element if an element was added" in forAll { (awSet: AWSet[Int, Double], replica: Int, newElement: Double) ⇒
  //    (awSet.add(replica)(newElement) join awSet) should contain (newElement)
  //  }
  //
  //  it should "have size+1 if a new element was added" in forAll { (awSet: AWSet[Int, Double], replica: Int, newElement: Double) ⇒
  //    whenever(!awSet.elements.toSet.contains(newElement)) {
  //      (awSet.add(replica)(newElement) join awSet) should have size (awSet.elements.size.asInstanceOf[Long] + 1)
  //    }
  //  }
  //
  //  it should "not contain the element if it is removed" in forAll { (awSet: AWSet[Int, Double], replica: Int, element: Double) =>
  //    (awSet.remove(replica)(element) join awSet) should not contain element
  //  }
  //
  //  it should "have size-1 if an element was removed" in forAll { (awSet: AWSet[Int, Double], replica: Int) ⇒
  //    whenever(awSet.elements.nonEmpty) {
  //      (awSet.remove(replica)(awSet.elements.head) join awSet) should have size (awSet.elements.size.asInstanceOf[Long] - 1)
  //    }
  //  }
  //
  //  it should "have zero elements after removal" in forAll { (awSet: AWSet[Int, Double], replica: Int) ⇒
  //    (awSet.clear(replica) join awSet) should have size 0
  //  }
  //
  //  /* Laws */
  //  it should "behave as lawful bounded-join-semilattice" in forAll { (awSet: AWSet[Int, Double]) ⇒
  //
  //  }

}
