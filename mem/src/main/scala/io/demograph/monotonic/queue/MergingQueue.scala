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

package io.demograph.monotonic.queue

import cats.instances.vector._
import cats.kernel.Monoid
import cats.syntax.foldable._
import cats.syntax.semigroup._
import io.demograph.monotonic.queue.OverflowStrategies._

final case class MergingQueue[A: Monoid](
  overflowStrategy: MergingOverflowStrategy,
  capacity: Int,
  override val vector: Vector[A] = Vector.empty) extends Queue[A](vector) {

  require(capacity > 0, "Capacity must be positive")
  require(vector.size <= capacity, "|vector| must not exceed `capacity`")

  override protected def fromVector(v: Vector[A]): Queue[A] = MergingQueue(overflowStrategy, capacity, v)

  override def enqueue(a: A): Queue[A] = {
    if (isFull && capacity > 1) {
      overflowStrategy match {
        case MergeHead ⇒ fromVector(vector.take(2).combineAll +: vector.drop(2) :+ a)
        case MergePairs ⇒ fromVector(vector.grouped(2).map(_.combineAll).toVector :+ a)
        case MergeAll ⇒ fromVector(Vector(vector.combineAll, a))
        // TODO: Merge in a way that each slot has the same number of merges (requires maintaining some state)
        case BalancedMerge ⇒ ???
        case BoundedBalancedMerge(maxMerges, fallback) ⇒ ???
      }
    } else if (isFull) {
      fromVector(Vector(vector.combineAll |+| a))
    } else {
      fromVector(vector :+ a)
    }
  }
}

object MergingQueue {
  def empty[V: Monoid](overflowStrategy: MergingOverflowStrategy, capacity: Int): MergingQueue[V] =
    MergingQueue(overflowStrategy, capacity)
}