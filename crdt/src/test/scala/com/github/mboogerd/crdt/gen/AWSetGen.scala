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

import com.github.mboogerd.crdt.delta.set.AWSet
import com.github.mboogerd.crdt.gen.History.Mutator
import org.scalacheck.Arbitrary._
import org.scalacheck.{ Arbitrary, Cogen, Gen }
/**
 *
 */
object AWSetGen {

  implicit def awSetCoGen[I, E]: Cogen[AWSet[I, E]] = Cogen(a ⇒ a.dotStore.hashCode().asInstanceOf[Long] << 32 + a.context.hashCode())

  def addMutator[I, E: Arbitrary](i: I): Mutator[AWSet[I, E]] = arbitrary[E].map(e ⇒ (awSet: AWSet[I, E]) ⇒ awSet.add(i)(e))

  def removeMutator[I, E](i: I)(implicit a: Arbitrary[AWSet[I, E]]): Mutator[AWSet[I, E]] = Gen.function1(arbitrary[AWSet[I, E]])
}
