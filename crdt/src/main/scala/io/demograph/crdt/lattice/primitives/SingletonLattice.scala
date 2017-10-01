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

package io.demograph.crdt.lattice.primitives

import algebra.Eq
import algebra.lattice.BoundedJoinSemilattice

/**
 *
 */

object SingletonLattice {

  case object ⊥

  implicit val bottomLattice: BoundedJoinSemilattice[⊥.type] = new SingletonLattice
  implicit val bottomEq: Eq[⊥.type] = (x: ⊥.type, y: ⊥.type) => x == y

  class SingletonLattice extends BoundedJoinSemilattice[⊥.type] {
    override def join(lhs: ⊥.type, rhs: ⊥.type): ⊥.type = zero
    override val zero: ⊥.type = ⊥
  }
}
