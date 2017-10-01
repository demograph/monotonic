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

import io.demograph.crdt.delta.set.{ AWSet, CRDTSet, RWSet }
import org.scalatest.enablers.{ Containing, Size }

/**
 *
 */
object CRDTImplicits {

  def crdtSetSize[I, E, R <: CRDTSet[I, E]]: Size[R] = new Size[R] {
    override def sizeOf(obj: R): Long = obj.elements.size.asInstanceOf[Long]
  }

  def crdtSetContaining[I, E, R <: CRDTSet[I, E]]: Containing[R] = new Containing[R] {
    val iterCont: Containing[Iterable[E]] = Containing.containingNatureOfGenTraversable[E, Iterable]

    override def contains(container: R, element: Any): Boolean = iterCont.contains(container.elements, element)

    override def containsOneOf(container: R, elements: Seq[Any]): Boolean = iterCont.containsOneOf(container.elements, elements)

    override def containsNoneOf(container: R, elements: Seq[Any]): Boolean = iterCont.containsNoneOf(container.elements, elements)
  }

  implicit def awSetSize[I, E]: Size[AWSet[I, E]] = crdtSetSize[I, E, AWSet[I, E]]
  implicit def awSetContaining[I, E]: Containing[AWSet[I, E]] = crdtSetContaining[I, E, AWSet[I, E]]

  implicit def rwSetSize[I, E]: Size[RWSet[I, E]] = crdtSetSize[I, E, RWSet[I, E]]
  implicit def rwSetContaining[I, E]: Containing[RWSet[I, E]] = crdtSetContaining[I, E, RWSet[I, E]]

}
