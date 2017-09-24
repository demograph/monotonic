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

package com.github.mboogerd.crdt.lattice.linearsum

import algebra.Eq
import algebra.lattice.{ BoundedJoinSemilattice, JoinSemilattice }
import com.github.mboogerd.crdt.lattice.linearsum.LinearSumInstances.{ LinearSumBoundedJoinSemilattice, LinearSumEq, LinearSumJoinSemilattice }

/**
 *
 */
trait LinearSumImplicits1 extends LinearSumSyntax {

  implicit def linearSumLattice[S: JoinSemilattice, T: JoinSemilattice]: JoinSemilattice[S :⊕: T] =
    new LinearSumJoinSemilattice[S, T]
}

trait LinearSumImplicits extends LinearSumImplicits1 {

  implicit def boundedlinearSumLattice[S: BoundedJoinSemilattice, T: BoundedJoinSemilattice]: BoundedJoinSemilattice[S :⊕: T] =
    new LinearSumBoundedJoinSemilattice[S, T]

  implicit def linearSumEq[S: Eq, T: Eq]: Eq[S :⊕: T] = new LinearSumEq[S, T]
}