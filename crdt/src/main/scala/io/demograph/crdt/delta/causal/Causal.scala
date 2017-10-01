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

package io.demograph.crdt.delta.causal

import algebra.lattice.BoundedJoinSemilattice

/**
 * A Causal type is the product of a DotStore and a CausalContext. We are more lenient than the paper in not
 * requiring it actually being a DotStore instance. This eases the creation of derived Causal instances, such as
 * using a product of two DotStores as the dotStore instance.
 */
trait Causal[I, DS] extends BoundedJoinSemilattice[(DS, CausalContext[I])]
