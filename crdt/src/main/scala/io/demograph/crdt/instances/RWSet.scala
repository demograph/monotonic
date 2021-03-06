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

package io.demograph.crdt.instances

import io.demograph.crdt.Session
import io.demograph.crdt.delta.causal.{ CausalCRDT, CausalContext }
import io.demograph.crdt.delta.dot.{ Dot, DotMap, DotSet }
import io.demograph.crdt.implicits.DotImplicits.dottedDot
/**
 *
 */
object RWSet extends SetCRDT {

  type DS[E, A] = DotMap[E, A, DotMap[E, Boolean, DotSet[E]]]
  /**
   * Under concurrent adds and removes of the same element, a remove-wins set will make removes win. To obtain this
   * behaviour, it uses a map from elements to a nested map from booleans to sets of events.
   */
  type RWSet[E, A] = CausalCRDT[E, DS[E, A]]

  def empty[H: Session, A]: RWSet[Dot[H], A] = CausalCRDT(DotMap.empty, CausalContext.empty)

  trait Mutate[E, A] extends MutateSet[E, A, DS[E, A], RWSet[E, A]]
}
