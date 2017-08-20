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

package com.github.mboogerd.mmap

import org.reactivestreams.Subscriber

/**
 *
 */
object SubscribedWriters {
  type Inner[K] = Map[K, Map[Long, (Subscriber[WriteNotification], Long, Vector[WriteNotification])]]
  def apply[K](): SubscribedWriters[K] = new SubscribedWriters(Map.empty)
}
case class SubscribedWriters[K] private[mmap] (state: SubscribedWriters.Inner[K]) extends Subscriptions[K] {

  override type Value = WriteNotification
  override type Queue = Vector[WriteNotification]
  override type Outer = SubscribedWriters[K]

  override protected def instantiate(inner: Inner): SubscribedWriters[K] = SubscribedWriters(inner)

  def requestedElements(vector: Queue, demand: Long): (Queue, Queue, Long) = {
    val (toSend, toRetain) = vector.splitAt(math.min(Int.MaxValue, demand).toInt)
    (toSend, toRetain, demand - toSend.size)
  }

}