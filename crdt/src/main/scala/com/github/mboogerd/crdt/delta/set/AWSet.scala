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

package com.github.mboogerd.crdt.delta.set

import com.github.mboogerd.crdt.delta.causal.{ CausalCRDT, CausalContext }
import com.github.mboogerd.crdt.delta.dot.{ Dot, DotMap, DotSet }
import com.github.mboogerd.crdt.implicits.all._

/**
 * In an add-wins set removals do not affect elements that have been concurrently added. In this sense, under
 * concurrent updates, an add will win over a remove of the same element. This data-type can be seen as a map from
 * elements to enable-wins flags, but with a single common causal context, and keeping only elements mapped to an
 * enabled flag
 */
object AWSet {
  type DS[I, E] = DotMap[I, E, DotSet[I]]
  type CRDT[I, E] = CausalCRDT[I, DS[I, E], AWSet[I, E]]
  def empty[I, E]: AWSet[I, E] = AWSet(DotMap.empty, CausalContext.empty)
}

case class AWSet[I, E](dotStore: AWSet.DS[I, E], context: CausalContext[I]) extends CRDTSet[I, E] {

  override type Repr = AWSet[I, E]

  /**
   * When an element is added, all dots in the corresponding entry will be replaced by a singleton set containing a
   * new dot
   */
  def add(i: I)(e: E): AWSet[I, E] = {
    val d: Dot[I] = context.nextDot(i)
    AWSet(
      DotMap(e → DotSet.single(d)),
      CausalContext(dotStore.dots(e) + d))
  }

  /**
   * If a DotSet for some element were to become empty, such as when removing the element, join will remove the entry
   * from the resulting map.
   */
  def remove(i: I)(e: E): AWSet[I, E] = AWSet(
    DotMap.empty,
    CausalContext(dotStore.dots(e)))

  /**
   * The clear delta mutator will put all dots from the dot store in the causal context, to be removed when joining
   */
  def clear(i: I): AWSet[I, E] = AWSet(
    DotMap.empty,
    CausalContext(dotStore.dots))

  /**
   * @return The elements that are currently considered to be in the set
   */
  def elements: Iterable[E] = dotStore.domain

  /**
   * Returns whether this AWSet contains the given element
   * @return True if the element is in the set, false otherwise
   */
  def contains(element: Any): Boolean = exists(_ == element)

  /**
   * Returns whether the given predicate is satisfied for this AWSet
   */
  def exists(p: E => Boolean): Boolean = dotStore.domain.exists(p)

  def size: Int = dotStore.dotMap.size

  def isEmpty: Boolean = size == 0

  def nonEmpty: Boolean = !isEmpty
}
