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

import io.demograph.crdt.{ Session, TestSpec }
import io.demograph.crdt.delta.causal.{ CausalCRDT, CausalContext, DotCC }
import io.demograph.crdt.delta.dot._
import io.demograph.crdt.instances.AWSet
import io.demograph.crdt.instances.AWSet.AWSet
import io.demograph.crdt.implicits.all._
import cats.syntax.semigroup._

/**
 *
 */
class AWSetTest extends TestSpec with CRDTSpec {

  behavior of "AWSet"

  /* Basic tests */

  it should "support delta-addition" in {
    // Create a basic AWSetGen, where identities are represented using Int's and the payload-type is Long
    val awSet = AWSet.empty[Int, Long]

    val awDelta: AWSet[Dot[Int], Long] = awSet.add(Long.MaxValue)
    val expectedDot: Dot[Int] = Dot(replicaID, 0)
    val expectedAWSet: AWSet[Dot[Int], Long] = CausalCRDT(DotMap(Long.MaxValue → DotSet(expectedDot)), CausalContext(expectedDot))

    awDelta shouldBe expectedAWSet

    val updated = awSet combine awDelta

    updated shouldBe expectedAWSet
  }

  it should "support delta-removal" in {
    val dot: Dot[Int] = Dot(replicaID, 1)
    val awSet: AWSet[Dot[Int], Long] = CausalCRDT(DotMap(Long.MaxValue → DotSet(dot)), CausalContext(dot))
    val awDelta: AWSet[Dot[Int], Long] = awSet.remove(Long.MaxValue)

    val expectedAWSet: AWSet[Dot[Int], Long] = CausalCRDT(DotMap.empty, CausalContext(dot))

    awDelta shouldBe expectedAWSet

    val updated = awSet combine awDelta
    updated shouldBe expectedAWSet
  }

  it should "support clearing" in {
    val fst: Dot[Int] = Dot(0, 1)
    val snd: Dot[Int] = Dot(1, 1)
    val trd: Dot[Int] = Dot(0, 2)
    val dots: Dots[Int] = Set(fst, snd, trd)

    // Construct an AWSetGen with three added elements: {-1, 0, 1}
    val causalContext = CausalContext.from(dots)
    val awSet: AWSet[Dot[Int], Long] = CausalCRDT(
      DotMap(-1L → DotSet(fst), 0L → DotSet(snd), 1L → DotSet(trd)),
      causalContext)

    val awDelta: AWSet[Dot[Int], Long] = awSet.clear

    val expectedAWSet: AWSet[Dot[Int], Long] = CausalCRDT(DotMap.empty, causalContext)

    awDelta shouldBe expectedAWSet

    val updated = awSet combine awDelta
    updated shouldBe expectedAWSet
  }

  it should "support retrieving the current elements" in {
    val fst: Dot[Int] = Dot(0, 1)
    val snd: Dot[Int] = Dot(1, 1)
    val trd: Dot[Int] = Dot(0, 1)
    val dots: Dots[Int] = Set(fst, snd, trd)
    val causalContext = CausalContext.from(dots)

    // Construct an AWSetGen with three added elements: {-1, 0, 1}
    val awSet: AWSet[Dot[Int], Long] =
      CausalCRDT(DotMap(Map(-1L → DotSet(fst), 0L → DotSet(snd), 1L → DotSet(trd))), causalContext)

    awSet.elements should contain theSameElementsAs Set(-1L, 0L, 1L)
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
