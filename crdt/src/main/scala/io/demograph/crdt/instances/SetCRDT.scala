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

import io.demograph.crdt.delta.causal.CausalCRDT
import io.demograph.crdt.delta.dot.DotStore

/**
 * Base trait for Set CRDT interface
 */
trait SetCRDT {

  trait Query[E, A] {
    /**
     * @return The elements that are currently considered to be in the set
     */
    def elements: Iterable[A]

    /**
     * Returns whether this AWSet contains the given element
     * @return True if the element is in the set, false otherwise
     */
    def contains(element: Any): Boolean

    /**
     * Returns whether the given predicate is satisfied for this AWSet
     */
    def exists(p: A => Boolean): Boolean
  }

  trait MutateSet[E, A, DS <: DotStore[E], CRDT <: CausalCRDT[E, DS]] {
    /**
     * When an element is added, all events in the corresponding entry will be replaced by a singleton set containing a
     * new dot
     */
    def add(e: A): CRDT

    /**
     * If a DotSet for some element were to become empty, such as when removing the element, join will remove the entry
     * from the resulting map.
     */
    def remove(e: A): CRDT

    /**
     * The clear delta mutator will put all events from the dot store in the causal context, to be removed when joining
     */
    def clear: CRDT
  }
}