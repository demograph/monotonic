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

package io.demograph.crdt.delta.dot

import algebra.Eq
import algebra.laws.LogicLaws
import cats.functor.Contravariant
import io.demograph.crdt.CatsSpec
import io.demograph.crdt.util.ScalaTestImplicits
import org.scalacheck.Arbitrary._
import org.scalacheck.{ Arbitrary, Gen }
import org.scalatest.Matchers
import org.scalatest.enablers.Aggregating._
import org.scalatest.enablers.Aggregating
import org.scalatest.prop.ScalaCheckPropertyChecks

import scala.collection.GenTraversable

/**
 *
 */
class CompactDotSetTest extends CatsSpec with Matchers with ScalaCheckPropertyChecks with ScalaTestImplicits {

  implicit def eqCDS: Eq[CompactDotSet[String]] = Eq.fromUniversalEquals

  implicit val arbitraryDot: Arbitrary[Dot[String]] = Arbitrary(for {
    replica <- Gen.alphaNumStr
    id ← Gen.chooseNum(CompactDotSet.MIN_VERSION, Int.MaxValue)
  } yield Dot(replica, id))

  implicit def arbCDS: Arbitrary[CompactDotSet[String]] = Arbitrary(for {
    dots ← Gen.listOf(arbitrary[Dot[String]])
  } yield CompactDotSet(dots: _*))

  /* Law Tests */

  checkAll("GenBool[CompactDotSet]", LogicLaws[CompactDotSet[String]].generalizedBool)

  /* Unit Tests */

  test("CompactDotSet[T] should behave like Set[Dot[T]] when passing elements to the constructor") {
    forAll { (dotlist: List[Dot[String]]) ⇒
      CompactDotSet(dotlist: _*) should contain theSameElementsAs Set(dotlist: _*)
    }
  }

  test("CompactDotSet[T] should behave like Set[Dot[T]] when adding an element") {
    forAll { (dotlist: List[Dot[String]], dot: Dot[String]) ⇒
      (CompactDotSet(dotlist: _*) + dot) should contain theSameElementsAs (Set(dotlist: _*) + dot)
    }
  }

  test("CompactDotSet[T] should behave like Set[Dot[T]] when removing an element") {
    forAll { (dotlist: List[Dot[String]], dot: Dot[String]) ⇒
      (CompactDotSet(dotlist: _*) - dot) should contain theSameElementsAs (Set(dotlist: _*) - dot)
    }
  }

  test("CompactDotSet[T] union should be equivalent to union of Set[Dot[T]]") {
    forAll { (dl1: List[Dot[String]], dl2: List[Dot[String]]) ⇒
      val cds1 = CompactDotSet(dl1: _*)
      val cds2 = CompactDotSet(dl2: _*)
      val dots1 = Set(dl1: _*)
      val dots2 = Set(dl2: _*)

      val cdsCds = cds1 union cds2
      val cdsDots = cds1 union dots2
      val dotsCds = dots1 union cds2
      val dotsDots = dots1 union dots2

      cdsCds should contain theSameElementsAs cdsDots
      cdsDots should contain theSameElementsAs dotsCds
      dotsCds should contain theSameElementsAs dotsDots
    }
  }

  test("CompactDotSet[T] intersect should be equivalent to intersect of Set[Dot[T]]") {
    forAll { (dl1: List[Dot[String]], dl2: List[Dot[String]]) ⇒
      val cds1 = CompactDotSet(dl1: _*)
      val cds2 = CompactDotSet(dl2: _*)
      val dots1 = Set(dl1: _*)
      val dots2 = Set(dl2: _*)

      val cdsCds = cds1 intersect cds2
      val cdsDots = cds1 intersect dots2
      val dotsCds = dots1 intersect cds2
      val dotsDots = dots1 intersect dots2

      cdsCds should contain theSameElementsAs cdsDots
      cdsDots should contain theSameElementsAs dotsCds
      dotsCds should contain theSameElementsAs dotsDots
    }
  }

  test("CompactDotSet[T] diff should be equivalent to diff of Set[Dot[T]]") {
    forAll { (dl1: List[Dot[String]], dl2: List[Dot[String]]) ⇒
      val cds1 = CompactDotSet(dl1: _*)
      val cds2 = CompactDotSet(dl2: _*)
      val dots1 = Set(dl1: _*)
      val dots2 = Set(dl2: _*)

      val cdsCds = cds1 diff cds2
      val cdsDots = cds1 diff dots2
      val dotsCds = dots1 diff cds2
      val dotsDots = dots1 diff dots2

      cdsCds should contain theSameElementsAs cdsDots
      cdsDots should contain theSameElementsAs dotsCds
      dotsCds should contain theSameElementsAs dotsDots
    }
  }
}
