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

import cats.syntax.contravariant._
import io.demograph.crdt.delta.dot.DotStore
import io.demograph.crdt.instances.AWSet.AWSet
import io.demograph.crdt.instances.ORMap.ORMap
import io.demograph.crdt.instances.RWSet.RWSet
import org.scalatest.enablers.Containing.containingNatureOfGenTraversable
import org.scalatest.enablers.Size.sizeOfGenTraversable
import org.scalatest.enablers.Aggregating.aggregatingNatureOfGenTraversable
import org.scalatest.enablers.{ Aggregating, Containing, Size }
/**
 *
 */
trait CRDTTestImplicits {

  implicit def awSetSize[E, A]: Size[AWSet[E, A]] =
    sizeOfGenTraversable[Traversable[A]].contramap(traversableAWSet)

  implicit def awSetContaining[E, A]: Containing[AWSet[E, A]] =
    containingNatureOfGenTraversable[A, Traversable].contramap(traversableAWSet)

  implicit def awSetAggregating[E, A]: Aggregating[AWSet[E, A]] =
    aggregatingNatureOfGenTraversable[A, Traversable].contramap(traversableAWSet)

  implicit def rwSetSize[E, A]: Size[RWSet[E, A]] =
    sizeOfGenTraversable[Traversable[A]].contramap(traversableRWSet)

  implicit def rwSetContaining[E, A]: Containing[RWSet[E, A]] =
    containingNatureOfGenTraversable[A, Traversable].contramap(traversableRWSet)

  implicit def rwSetAggregating[E, A]: Aggregating[RWSet[E, A]] =
    aggregatingNatureOfGenTraversable[A, Traversable].contramap(traversableRWSet)

  implicit def orMapSize[E, K, V <: DotStore[E]]: Size[ORMap[E, K, V]] =
    sizeOfGenTraversable[Traversable[(K, V)]].contramap(traversableORMap)

  implicit def orMapContaining[E, K, V <: DotStore[E]]: Containing[ORMap[E, K, V]] =
    containingNatureOfGenTraversable[(K, V), Traversable].contramap(traversableORMap)

  implicit def orMapAggregating[E, K, V <: DotStore[E]]: Aggregating[ORMap[E, K, V]] =
    aggregatingNatureOfGenTraversable[(K, V), Traversable].contramap(traversableORMap)
}

object CRDTTestImplicits extends CRDTTestImplicits
