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

/**
 *
 */
abstract class BoundedQueue[A](vector: Vector[A]) extends QueueConsumer[A] {

  override type QC <: BoundedQueue[A]

  override def isEmpty: Boolean = vector.isEmpty

  override def nonEmpty: Boolean = vector.nonEmpty

  override def size: Int = vector.size

  override def peek(n: Int): Traversable[A] = vector.take(n)

  override def dropHead(): QC = fromVector(vector.tail)

  override def dropTail(): QC = fromVector(Vector(vector.head))

  def clear(): QC = fromVector(Vector.empty)

  override def dequeue(): A = vector.head

  override def dequeue(i: Int): (Traversable[A], QC) = {
    val (first, second) = vector.splitAt(i)
    (first, fromVector(second))
  }

  protected def fromVector(v: Vector[A]): QC

  def isFull: Boolean = vector.size >= capacity

  def capacity: Int
}