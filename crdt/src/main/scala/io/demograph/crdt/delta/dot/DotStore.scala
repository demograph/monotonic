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

package io.demograph.crdt.delta.dot

import algebra.lattice.BoundedJoinSemilattice

/**
 * The state of a causal CRDT will use some kind of dot store, which acts as a container for data-type specific
 * information. A dot store can be queried about the set of event identifiers (dots) corresponding to the relevant
 * operations in the container, by function dots, which takes a dot store and returns a set of dots
 */
trait DotStore[DS, I] extends BoundedJoinSemilattice[DS] {
  def dots(store: DS): Dots[I]
}
