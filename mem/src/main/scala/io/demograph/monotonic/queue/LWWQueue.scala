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
case class LWWQueue[V](state: Option[V]) extends Queue[V] {
  override type Repr = LWWQueue[V]

  /**
   * Enqueues the given element into this queue
   *
   * @param v
   * @return
   */
  override def enqueue(v: V): LWWQueue[V] = LWWQueue(Some(v))

  /**
   * Dequeues the given number of elements from the queue, and returns that and the remaining queue.
   *
   * @param count
   * @return
   */
  override def dequeue(count: Int): (Int, TraversableOnce[V], LWWQueue[V]) = {
    if (count > 0)
      (count - (if (state.isDefined) 1 else 0), state, LWWQueue(None))
    else
      (count, Traversable.empty, this)
  }

  /**
   * @return true if this queue contains no elements, false otherwise
   */
  override def isEmpty: Boolean = state.isEmpty
}
object LWWQueue {
  def empty[V]: LWWQueue[V] = new LWWQueue[V](None)
}
