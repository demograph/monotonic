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

/**
 *
 */
object LatticeSyntax extends LatticeSyntax

trait LatticeSyntax1 {
  implicit def latticeOps[S: JoinSemilattice](lat1: S): LatticeOps[S] = new LatticeOps(lat1)
}
trait LatticeSyntax extends LatticeSyntax1 {
  implicit def boundedLatticeOps[S: BoundedJoinSemilattice](lat1: S): BoundedLatticeOps[S] = new BoundedLatticeOps(lat1)
}

class LatticeOps[S: JoinSemilattice](lat1: S) {
  def join[T <: S](lat2: T): S = JoinSemilattice[S].join(lat1, lat2)
  def ⊔[T <: S](lat2: T): S = join(lat2)
}

final class BoundedLatticeOps[S: BoundedJoinSemilattice](lat1: S) extends LatticeOps[S](lat1) {
  def bottom: S = BoundedJoinSemilattice[S].zero
}