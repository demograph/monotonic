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

package io.demograph.crdt.lattice

import algebra.instances.boolean._
import algebra.laws.LatticeLaws
import io.demograph.crdt.CatsSpec
import io.demograph.crdt.lattice.ArbitraryLattices._
import io.demograph.crdt.lattice.primitives.SingletonLattice._
import io.demograph.crdt.lattice.primitives._
/**
 *
 */
class PrimitiveLaws extends CatsSpec {

  checkAll("⊥", LatticeLaws[⊥.type].boundedJoinSemilattice)
  checkAll("Boolean", LatticeLaws[Boolean].boundedJoinSemilattice)
  checkAll("Int.ascending", LatticeLaws[Int].joinSemilattice(IntegerLattice.Ascending.AscIntegerLattice))
  checkAll("Int.descending", LatticeLaws[Int].joinSemilattice(IntegerLattice.Descending.DescIntegerLattice))
}
