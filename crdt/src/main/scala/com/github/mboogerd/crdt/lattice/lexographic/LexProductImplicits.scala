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

package com.github.mboogerd.crdt.lattice.lexographic

import algebra.Eq
import algebra.lattice.{ BoundedJoinSemilattice, JoinSemilattice }
import com.github.mboogerd.crdt.lattice.Chain
import com.github.mboogerd.crdt.lattice.lexographic.LexProductInstances._

/**
 *
 */
trait LexProductImplicits3 extends LexProductSyntax {

  implicit def lexProductLattice2[S: JoinSemilattice: Eq, T: JoinSemilattice: Eq]: LexProductJSL2[S, T] =
    new LexProductJSL2[S, T]
}

trait LexProductImplicits2 extends LexProductImplicits3 {

  implicit def lexProductLattice[S: JoinSemilattice: Eq, T: BoundedJoinSemilattice: Eq]: LexProductJSL[S, T] =
    new LexProductJSL[S, T]
}

trait LexProductImplicits extends LexProductImplicits2 {

  implicit def boundedLexProductLattice[S: BoundedJoinSemilattice: Eq, T: BoundedJoinSemilattice: Eq]: LexProductBJSL[S, T] =
    new LexProductBJSL[S, T]

  implicit def chainLexProductLattice[S: Chain, T: Chain]: LexProductChain[S, T] = new LexProductChain[S, T]
}