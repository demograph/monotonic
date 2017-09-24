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

package com.github.mboogerd.crdt.delta.dot

/**
 * a DotSet is simply a set of dots
 *
 * @param dots
 * @tparam I
 */
case class DotSet[I](dots: Dots[I])

object DotSet {

  /**
   * Produces an empty DotSet
   */
  def empty[I]: DotSet[I] = DotSet(Dots.empty)

  /**
   * Produces a singleton DotSet
   */
  def single[I](i: I, v: Int = 0): DotSet[I] = single(Dot(i, v))

  /**
   * Produces a singleton DotSet
   */
  def single[I](dot: Dot[I]) = DotSet(Dots.single(dot))
}