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

package io.demograph.crdt.lattice.product

import algebra.lattice.JoinSemilattice

trait ProductSyntax {

  type :×:[S, T] = ProductLattice[S, T]

  implicit def laticeProductOps[T: JoinSemilattice](t: T): ProductOps[T] = new ProductOps[T](t)
}
final class ProductOps[T: JoinSemilattice](right: T) {
  def product[S: JoinSemilattice](left: S): S :×: T = new :×:(left, right)
  def :×:[S: JoinSemilattice](left: S): S :×: T = product(left)
}