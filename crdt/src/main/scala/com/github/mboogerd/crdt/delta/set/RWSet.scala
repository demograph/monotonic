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
import com.github.mboogerd.crdt.delta.dot.{ DotMap, DotSet }
import com.github.mboogerd.crdt.implicits.all._

/**
 * Under concurrent adds and removes of the same element, a remove-wins set will make removes win. To obtain this
 * behaviour, it uses a map from elements to a nested map from booleans to sets of dots.
 */
object RWSet {
  type DS[I, E] = DotMap[I, E, DotMap[I, Boolean, DotSet[I]]]
  type CRDT[I, E] = CausalCRDT[I, DS[I, E], RWSet[I, E]]
}
case class RWSet[I, E](dotStore: RWSet.DS[I, E], context: CausalContext[I]) extends CRDTSet[I, E] {

  override type Repr = RWSet[I, E]

  /**
   * Inserts element `e` under identity `i` and returns the delta-RWSet.
   *
   * The nested map is cleared (by the delta mutator inserting all corresponding dots into the causal context), and a
   * new mapping from True to a singleton new dot is added.
   */
  def add(i: I)(e: E): RWSet[I, E] = {
    val d = context.nextDot(i)
    RWSet(DotMap(e → DotMap(true → DotSet.single(d))), CausalContext(dotStore.dots + d))
  }

  /**
   * Removes element `e` under identity `i` and returns the delta-RWSet
   *
   * The nested map is cleared (by the delta mutator inserting all corresponding dots into the causal context), and a
   * new mapping from False to a singleton new dot is added.
   */
  def remove(i: I)(e: E): RWSet[I, E] = {
    val d = context.nextDot(i)
    RWSet(DotMap(e → DotMap(false → DotSet.single(d))), CausalContext(dotStore.dots + d))
  }

  /**
   * Empties this RWSet
   */
  def clear(i: I): RWSet[I, E] = RWSet(DotMap.empty, CausalContext(dotStore.dots))

  /**
   * @return The elements currently considered to be part of this RWSet
   */
  def elements: Iterable[E] = dotStore.dotMap.collect { case (k, v) if !v.dotMap.contains(false) ⇒ k }
}
