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

import algebra.Eq
import algebra.laws.LogicLaws
import io.demograph.crdt.CatsSpec
import io.demograph.crdt.delta.dot.{ CompactDotSet, Dot }
import org.scalacheck.Arbitrary._
import org.scalacheck.{ Arbitrary, Gen }

/**
 *
 */
class CompactDotSetTest extends CatsSpec {
  implicit def eqCDS: Eq[CompactDotSet[String]] = Eq.fromUniversalEquals

  implicit val arbitraryDot: Arbitrary[Dot[String]] = Arbitrary(for {
    replica <- Gen.alphaNumStr
    id ← arbitrary[Int]
  } yield Dot(replica, id))

  implicit def arbCDS: Arbitrary[CompactDotSet[String]] = Arbitrary(for {
    dots ← Gen.listOf(arbitrary[Dot[String]])
  } yield CompactDotSet(dots: _*))

  checkAll("GenBool[CompactDotSet]", LogicLaws[CompactDotSet[String]].generalizedBool)
}
