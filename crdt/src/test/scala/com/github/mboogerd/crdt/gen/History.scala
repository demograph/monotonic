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

package com.github.mboogerd.crdt.gen

import algebra.lattice.JoinSemilattice
import com.github.mboogerd.crdt.delta.causal.CausalContext
import com.github.mboogerd.crdt.delta.dot.{ Dot, DotMap, DotSet, Dots }
import com.github.mboogerd.crdt.delta.set.AWSet
import com.github.mboogerd.crdt.implicits.all._
import com.github.mboogerd.crdt.implicits.dots
import com.github.mboogerd.crdt.syntax.all._
import org.scalacheck.Arbitrary._
import org.scalacheck.{ Arbitrary, Gen }

import scala.collection.JavaConverters._
/**
 *
 */
object History {

  type Initial[T] = Gen[T]
  type Mutator[T] = Gen[T ⇒ T]

  def gen[T: JoinSemilattice](initial: Initial[T], mutators: (Int, Mutator[T])*): Gen[T] = for {
    i ← initial
    m ← mutator(mutators: _*)
  } yield m(i) join i

  def mutator[T: JoinSemilattice](mutators: (Int, Mutator[T])*): Gen[T ⇒ T] =
    Gen.listOf(Gen.frequency(mutators: _*)).map(_.foldLeft(identity[T] _)(_ andThen _)).map(f ⇒ t ⇒ f(t) join t)

  def genCausalContext[I](i: I): Gen[CausalContext[I]] = {
    Gen.posNum[Int]
      .map(version ⇒ 1 to version % 19 + 1) // versions between 1 and max. 20, inclusively
      .map(_.map(Dot(i, _)).toSet)
      .map(CausalContext(_))
  }

  implicit def arbitraryDot: Arbitrary[Dot[Int]] =
    Arbitrary(Gen.zip(arbitrary[Int], arbitrary[Int].map(_ % 10)).map { case (r, i) ⇒ Dot(r, i) })

  def dotsFromDot[I](dot: Dot[I]): List[Dot[I]] = (1 to dot.version).map(version ⇒ Dot(dot.replica, version)).toList

  implicit def arbitraryDots: Arbitrary[Dots[Int]] = Arbitrary(Gen.listOf(arbitrary[Dot[Int]].map(dotsFromDot)).map(_.flatten.toSet))

  implicit def arbitraryCausalContext: Arbitrary[CausalContext[Int]] = Arbitrary(arbitrary[Dots[Int]].map(CausalContext(_)))

  implicit def arbitraryAWSet[E: Arbitrary]: Arbitrary[AWSet[Int, E]] = Arbitrary(for {
    cc ← arbitrary[CausalContext[Int]]
    elems ← Gen.someOf(cc.dots)
    ds ← Gen.sequence(elems.map(d ⇒ arbitrary[E].map(_ → DotSet.single(d))))
  } yield AWSet(DotMap(Map(ds.asScala: _*)), cc))

  // DotMap[I, E, DotMap[I, Boolean, DotSet[I]]]
  //  implicit def arbitraryRWSet[E: Arbitrary]: Arbitrary[RWSet[Int, E]] = Arbitrary(for {
  //    cc ← arbitrary[CausalContext[Int]]
  //
  //  })

  //  def awSetGen[I: Arbitrary, E: Arbitrary](implicit ccGen: Arbitrary[I ⇒ CausalContext[I]]): Arbitrary[AWSet[I, E]] = Arbitrary(for {
  //    i ← arbitrary[I]
  //    cc ← ccGen.arbitrary.
  //    dots ← Gen.someOf(cc.dots)
  //    es ← Gen.sequence(dots.map(d ⇒ arbitrary[E].map(_ → DotSet.single(d))))
  //  } yield AWSet(DotMap(Map(es:_*)), cc))

  //  def replica[T: BoundedJoinSemilattice](mutators: (Int, Mutator[T])*): Gen[T]
}
