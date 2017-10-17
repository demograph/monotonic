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

package io.demograph.crdt.implicits

import io.demograph.crdt.Session
import io.demograph.crdt.delta.causal.{ CausalCRDT, CausalContext }
import io.demograph.crdt.delta.dot.{ Dot, DotMap, DotSet }
import io.demograph.crdt.implicits.DotImplicits._
import io.demograph.crdt.instances.AWSet.{ AWSet, Mutate, Query }

/**
 *
 */
trait AWSetImplicits {

  implicit class QueryAWSet[E, A](awSet: AWSet[E, A]) extends Query[E, A] {
    /**
     * @return The elements that are currently considered to be in the set
     */
    override def elements: Iterable[A] = awSet.eventStore.domain

    /**
     * Returns whether this AWSet contains the given element
     *
     * @return True if the element is in the set, false otherwise
     */
    override def contains(element: Any): Boolean = exists(_ == element)

    /**
     * Returns whether the given predicate is satisfied for this AWSet
     */
    override def exists(p: (A) ⇒ Boolean): Boolean = elements.exists(p)
  }

  implicit class MutateAWSet[H: Session, A](awSet: AWSet[Dot[H], A]) extends Mutate[Dot[H], A] {

    /**
     * When an element is added, all events in the corresponding entry will be replaced by a singleton set containing a
     * new dot
     */
    override def add(a: A): AWSet[Dot[H], A] = {
      val dot = awSet.context.nextDot
      CausalCRDT(DotMap(a → DotSet(dot)), CausalContext.from(awSet.eventStore.eventIDsFor(a) + dot))
    }

    /**
     * If a DotSet for some element were to become empty, such as when removing the element, join will remove the entry
     * from the resulting map.
     */
    override def remove(e: A): AWSet[Dot[H], A] =
      CausalCRDT(DotMap.empty, CausalContext.from(awSet.eventStore.eventIDsFor(e)))

    /**
     * The clear delta mutator will put all events from the dot store in the causal context, to be removed when joining
     */
    override def clear: AWSet[Dot[H], A] =
      CausalCRDT(DotMap.empty, CausalContext.from(awSet.eventStore.dots))
  }
}

object AWSetImplicits extends AWSetImplicits