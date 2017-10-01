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

package io.demograph.crdt

import org.scalacheck.Gen

/**
 * Created by merlijn on 09/12/15.
 */
object GenHelper {

  /**
   * Zips a generated value together with a value derived from it
   *
   * @param genBase       Any generator
   * @param genDerivative A parameterized generator, producing items with some base item as input
   * @tparam T Type of the first generator
   * @tparam S Type of the second generator
   * @return The combined result of both generators
   */
  def withDerivative[S, T](genBase: Gen[T])(genDerivative: T => Gen[S]): Gen[(T, S)] = for {
    base <- genBase
    alt <- genDerivative(base)
  } yield (base, alt)

  /* Zips a generated value together with a same-typed value derived from it */
  def withAlternative[T] = withDerivative[T, T] _

  /* Generates between lowerBound and upperBound number of entries */
  def between[T](lowerBound: Int, upperBound: Int, gen: Gen[T]): Gen[Traversable[T]] = for {
    size <- Gen.choose(lowerBound, upperBound)
    list <- Gen.listOfN(size, gen)
  } yield list

  /**
   * Maps an arbitrary number of entries, bounded by the supplied parameters min/max, but finally also by the actual
   * number of entries generated (i.e. an empty list generated, with a minimum of one modifications, still results in
   * an empty list).
   *
   * @param minModifications
   * @param maxModifications
   * @param genEntries
   * @return
   */
  def arbitrarilyMap[S, T <: S](minModifications: Int = 1, maxModifications: Int = 1, genEntries: Gen[List[S]], sToT: S => T) = for {
    entries <- genEntries
    modifications <- Gen.choose(math.min(minModifications, entries.size), math.min(maxModifications, entries.size))
    indicesToModify <- Gen.containerOfN[Set, Int](modifications, Gen.choose(0, entries.size - 1))
  } yield indicesToModify.foldLeft(entries) { case (list, index) => list.updated(index, sToT(list(index))) }

  /**
   * Choses at least one elements, up to the entire set, as a subset of the given set
   *
   * @param set
   * @tparam T
   * @return
   */
  def atLeastOne[T](set: Set[T]): Gen[Set[T]] = {
    assert(set.nonEmpty)
    for {
      some <- Gen.someOf(set)
      one <- Gen.oneOf(set.toSeq)
    } yield (some :+ one).toSet
  }

  /**
   * Fold-flatmaps the supplied function 'apply' an arbitrary number of times, given the supplied input 'initial'
   * TODO: Looks like Kleisli could cleanup some things potentially?
   * @param initial
   * @param apply The function
   * @tparam T
   * @return
   */
  def foldGen[T](initial: T)(apply: T => Gen[T]): Gen[T] = {
    Gen.oneOf(
      Gen.const(initial),
      Gen.lzy(for {
        applied <- apply(initial)
        reapplied <- foldGen(applied)(apply)
      } yield reapplied))
  }
}
