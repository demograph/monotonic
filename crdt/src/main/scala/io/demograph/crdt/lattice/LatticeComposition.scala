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

import io.demograph.crdt.lattice.lexographic.LexProductImplicits
import io.demograph.crdt.lattice.linearsum.LinearSumImplicits
import io.demograph.crdt.lattice.product.ProductImplicits

/**
 *
 */
object LatticeComposition extends LatticeComposition

trait LatticeComposition
  extends ProductImplicits
  with LexProductImplicits
  with LinearSumImplicits
