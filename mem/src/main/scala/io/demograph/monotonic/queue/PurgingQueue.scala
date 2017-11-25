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

import io.demograph.monotonic.queue.OverflowStrategies._

/**
 *
 */
final case class PurgingQueue[A](overflowStrategy: PurgingOverflowStrategy, capacity: Int, override val vector: Vector[A]) extends Queue[A](vector) {

  require(capacity > 0, "Capacity must be positive")
  require(vector.size <= capacity, "|vector| must not exceed `capacity`")

  override def enqueue(a: A): Queue[A] = {
    if (isFull) {
      overflowStrategy match {
        case DropHead ⇒ dropHead().enqueue(a)
        case DropTail ⇒ dropTail().enqueue(a)
        case DropBuffer ⇒ clear().enqueue(a)
        case DropNew ⇒ this
      }
    } else {
      fromVector(vector :+ a)
    }
  }

  override protected def fromVector(v: Vector[A]): Queue[A] = new PurgingQueue[A](overflowStrategy, capacity, v)
}

object PurgingQueue {
  def empty[A](overflowStrategy: PurgingOverflowStrategy, capacity: Int): PurgingQueue[A] =
    PurgingQueue(overflowStrategy, capacity, Vector.empty)
}