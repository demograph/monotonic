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

import algebra.{ Eq, Order }
import algebra.lattice.{ BoundedJoinSemilattice, JoinSemilattice }

/**
 * These definitions of chain and bounded-chain have a risk of being less DRY than possible, but it is convenient in our context
 */
trait Chain[A] extends Order[A] with JoinSemilattice[A]

trait BoundedChain[A] extends Chain[A] with BoundedJoinSemilattice[A]
