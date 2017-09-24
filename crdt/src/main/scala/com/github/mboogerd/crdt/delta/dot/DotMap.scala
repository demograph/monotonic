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

import scala.collection.immutable.Map

/**
 * the generic DotMap[K,V] is a map from some set K into some dot store V
 *
 * @param dotMap
 * @tparam I
 * @tparam K
 * @tparam V
 */
case class DotMap[I, K, V](dotMap: Map[K, V])(implicit ds: DotStore[V, I]) {
  val dots: Dots[I] = dotMap.values.flatMap(ds.dots).toSet
  def dots(key: K): Dots[I] = dotMap.get(key).map(ds.dots).getOrElse(Dots.empty)
  def domain: Set[K] = dotMap.keySet
  def contains(key: K): Boolean = dotMap.contains(key)
  def size: Int = dotMap.size
}

object DotMap {

  /**
   * Produces an empty DotMap
   */
  def empty[I, K, V](implicit ds: DotStore[V, I]): DotMap[I, K, V] = new DotMap[I, K, V](Map.empty)

  /**
   * Produces a DotMap with the given pairs
   */
  def apply[I, K, V](pairs: (K, V)*)(implicit ds: DotStore[V, I]): DotMap[I, K, V] = new DotMap(Map(pairs: _*))

}

