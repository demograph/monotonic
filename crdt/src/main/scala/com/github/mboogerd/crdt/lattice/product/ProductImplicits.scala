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
import com.github.mboogerd.crdt.lattice.product.ProductInstances._

/**
 *
 */
trait ProductImplicits extends ProductSyntax {

  implicit def productEq[S: Eq, T: Eq]: Eq[S :×: T] =
    new ProductJSLEq[S, T]

  implicit def productLattice[S: JoinSemilattice, T: JoinSemilattice]: ProductJoinSemilattice[S, T] =
    new ProductJoinSemilattice[S, T]

  implicit def boundedProductLattice[S: BoundedJoinSemilattice, T: BoundedJoinSemilattice]: ProductBoundedJoinSemilattice[S, T] =
    new ProductBoundedJoinSemilattice[S, T]
}
