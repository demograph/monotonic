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

package com.github.mboogerd.crdt.util

import com.github.mboogerd.crdt.delta.causal.CausalCRDT
import com.github.mboogerd.crdt.delta.dot.DotStore
import com.github.mboogerd.crdt.delta.map.ORMap
import com.github.mboogerd.crdt.delta.set.AWSet
import org.scalatest.enablers.Containing

/**
 *
 */
trait ScalaTestImplicits {

  implicit def awSetContaining[I, E]: Containing[AWSet[I, E]] = new Containing[AWSet[I, E]] {
    override def contains(container: AWSet[I, E], element: Any): Boolean =
      container.contains(element)

    override def containsOneOf(container: AWSet[I, E], elements: Seq[Any]): Boolean =
      elements.count(container.contains) == 1

    override def containsNoneOf(container: AWSet[I, E], elements: Seq[Any]): Boolean =
      !elements.exists(container.contains)
  }

  implicit def orMapContaining[I, K, V, C](implicit ds: DotStore[V, I], causal: CausalCRDT[I, V, C]): Containing[ORMap[I, K, V, C]] =
    new Containing[ORMap[I, K, V, C]] {
      override def contains(container: ORMap[I, K, V, C], element: Any): Boolean = element match {
        case k: K @unchecked ⇒ container.contains(k)
        case v: V @unchecked ⇒ container.dotStore.dotMap.valuesIterator.contains(v)
        case kv: (K, V) @unchecked ⇒ container.dotStore.dotMap.exists(_ == kv)
        case _ ⇒ false
      }

      override def containsOneOf(container: ORMap[I, K, V, C], elements: Seq[Any]): Boolean =
        elements.count(e ⇒ contains(container, e)) == 1

      override def containsNoneOf(container: ORMap[I, K, V, C], elements: Seq[Any]): Boolean =
        elements.count(e ⇒ contains(container, e)) == 0
    }

}
