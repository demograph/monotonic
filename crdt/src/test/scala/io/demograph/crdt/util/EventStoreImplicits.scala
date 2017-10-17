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

package io.demograph.crdt.util

import algebra.lattice.JoinSemilattice
import cats.syntax.contravariant._
import io.demograph.crdt.delta.dot.{ DotFun, DotMap, DotSet, DotStore }
import org.scalatest.enablers.Aggregating.aggregatingNatureOfGenTraversable
import org.scalatest.enablers.Containing.containingNatureOfGenTraversable
import org.scalatest.enablers.Size.sizeOfGenTraversable
import org.scalatest.enablers.{ Aggregating, Containing, Size }

/**
 *
 */
trait EventStoreImplicits {

  implicit def eventSetSize[E]: Size[DotSet[E]] =
    sizeOfGenTraversable[Traversable[E]].contramap(traversableEventSet)

  implicit def eventSetContaining[E]: Containing[DotSet[E]] =
    containingNatureOfGenTraversable[E, Traversable].contramap(traversableEventSet)

  implicit def eventSetAggregating[E]: Aggregating[DotSet[E]] =
    aggregatingNatureOfGenTraversable[E, Traversable].contramap(traversableEventSet)

  implicit def eventFunSize[E, V: JoinSemilattice]: Size[DotFun[E, V]] =
    sizeOfGenTraversable[Traversable[V]].contramap(traversableEventFun)

  implicit def eventFunContaining[E, V: JoinSemilattice]: Containing[DotFun[E, V]] =
    containingNatureOfGenTraversable[V, Traversable].contramap(traversableEventFun)

  implicit def eventFunAggregating[E, V: JoinSemilattice]: Aggregating[DotFun[E, V]] =
    aggregatingNatureOfGenTraversable[V, Traversable].contramap(traversableEventFun)

  implicit def eventMapSize[E, K, V <: DotStore[E]]: Size[DotMap[E, K, V]] =
    sizeOfGenTraversable[Traversable[K]].contramap(traversableEventMap)

  implicit def eventMapContaining[E, K, V <: DotStore[E]]: Containing[DotMap[E, K, V]] =
    containingNatureOfGenTraversable[K, Traversable].contramap(traversableEventMap)

  implicit def eventMapAggregating[E, K, V <: DotStore[E]]: Aggregating[DotMap[E, K, V]] =
    aggregatingNatureOfGenTraversable[K, Traversable].contramap(traversableEventMap)
}

object EventStoreImplicits extends EventStoreImplicits
