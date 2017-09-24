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

package com.github.mboogerd.crdt.lattice.product

import algebra.Eq
import algebra.lattice.{ BoundedJoinSemilattice, JoinSemilattice }
import com.github.mboogerd.crdt.lattice.LatticeSyntax._
/**
 *
 */
object ProductInstances extends ProductSyntax {

  class ProductJSLEq[S: Eq, T: Eq] extends Eq[S :×: T] {
    override def eqv(x: S :×: T, y: S :×: T): Boolean =
      Eq[S].eqv(x.left, y.left) && Eq[T].eqv(x.right, y.right)
  }

  class ProductJoinSemilattice[S: JoinSemilattice, T: JoinSemilattice] extends JoinSemilattice[S :×: T] {
    override def join(lhs: S :×: T, rhs: S :×: T): S :×: T =
      lhs.left ⊔ rhs.left :×: lhs.right ⊔ rhs.right
  }

  class ProductBoundedJoinSemilattice[S: BoundedJoinSemilattice, T: BoundedJoinSemilattice] extends ProductJoinSemilattice[S, T] with BoundedJoinSemilattice[S :×: T] {
    override def zero: S :×: T =
      BoundedJoinSemilattice[S].zero :×: BoundedJoinSemilattice[T].zero
  }
}
