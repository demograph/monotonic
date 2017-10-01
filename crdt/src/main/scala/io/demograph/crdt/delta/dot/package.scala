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

package io.demograph.crdt.delta

/**
 *
 */
package object dot {

  /**
   * Naive, but correct, implementation of having a collection of Dots. No compression is done, which makes this
   * _highly_ inefficient!
   *
   * TODO: Create a collection of disjoint dot-intervals, that offers the full set interface
   *
   * @tparam I replica identifier type
   */
  type Dots[I] = Set[Dot[I]]

  object Dots {

    /**
     * Produces an empty DotSet
     */
    def empty[I]: Dots[I] = Set.empty[Dot[I]]

    /**
     * Produces a singleton DotSet
     */
    def single[I](i: I, v: Int = 0): Dots[I] = single(Dot(i, v))

    /**
     * Produces a singleton DotSet
     */
    def single[I](dot: Dot[I]): Dots[I] = Set(dot)
  }
}
