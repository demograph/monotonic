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
object AWSet extends SetCRDT {

  type DS[E, A] = DotMap[E, A, DotSet[E]]
  /**
   * In an add-wins set removals do not affect elements that have been concurrently added. In this sense, under
   * concurrent updates, an add will win over a remove of the same element. This data-type can be seen as a map from
   * elements to enable-wins flags, but with a single common causal context, and keeping only elements mapped to an
   * enabled flag
   */
  type AWSet[E, A] = CausalCRDT[E, DS[E, A]]

  def empty[H: Session, A]: AWSet[Dot[H], A] = CausalCRDT(DotMap.empty, CausalContext.empty)

  trait Mutate[E, A] extends MutateSet[E, A, DS[E, A], AWSet[E, A]]
}
