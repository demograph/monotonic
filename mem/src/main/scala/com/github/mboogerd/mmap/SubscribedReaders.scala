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
object SubscribedReaders {
  type Inner[K] = Map[K, Map[Long, (Subscriber[Any], Long, Option[(Set[Long], Any)])]]
  def apply[K](): SubscribedReaders[K] = new SubscribedReaders(Map.empty)
}

case class SubscribedReaders[K] private[mmap] (state: SubscribedReaders.Inner[K]) extends Subscriptions[K] {

  type Value = Any
  type Queue = Option[(Set[Long], Any)]
  type Outer = SubscribedReaders[K]

  override protected def instantiate(inner: Inner): SubscribedReaders[K] = SubscribedReaders(inner)

  override def requestedElements(queue: Queue, demand: Long): (Queue, Queue, Long) =
    if (demand > 0) (queue, None, demand - 1) else (None, queue, demand)

  def unsubscribeWriter(key: K, index: Long): SubscribedReaders[K] = {
    def pruneFromQueue(queue: Queue): Queue = queue.map { case (writers, value) ⇒ (writers - index, value) }
    val (_, newState) = updateSubscribers(key, { case (s, d, q) ⇒ (s, d, pruneFromQueue(q)) })
    newState
  }
}