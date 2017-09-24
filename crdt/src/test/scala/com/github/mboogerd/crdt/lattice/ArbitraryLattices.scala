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

import algebra.lattice.{ BoundedJoinSemilattice, JoinSemilattice }
import com.github.mboogerd.crdt.lattice.primitives.SingletonLattice._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{ Arbitrary, Gen }
/**
 *
 */
object ArbitraryLattices {

  implicit val arbitraryBottom: Arbitrary[⊥.type] = Arbitrary(⊥)

  implicit def arbitraryProduct[S: Arbitrary: JoinSemilattice, T: Arbitrary: JoinSemilattice]: Arbitrary[S :×: T] = {
    Arbitrary(for {
      genS ← arbitrary[S]
      genT ← arbitrary[T]
    } yield genS :×: genT)
  }

  implicit def arbitraryLexProduct[S: Arbitrary: JoinSemilattice, T: Arbitrary: BoundedJoinSemilattice]: Arbitrary[S :⊠: T] = {
    Arbitrary(for {
      genS ← arbitrary[S]
      genT ← arbitrary[T]
    } yield genS :⊠: genT)
  }

  implicit def arbitraryLinearSum[S: Arbitrary: JoinSemilattice, T: Arbitrary: JoinSemilattice]: Arbitrary[S :⊕: T] = {
    import linearsum._
    Arbitrary(for {
      genS ← arbitrary[S]
      genT ← arbitrary[T]
      linSum ← Gen.oneOf(Left[S, T](genS), Right[S, T](genT))
    } yield linSum)
  }
}