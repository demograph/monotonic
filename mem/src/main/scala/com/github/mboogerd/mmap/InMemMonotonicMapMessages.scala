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

import algebra.lattice.JoinSemilattice
import org.reactivestreams.Subscriber

/**
 * INTERNAL API
 */
object InMemMonotonicMapMessages {

  /**
   * Signals that the given subscriber desires read-subscription for the given key
   */
  case class Read(key: Any, subscriber: Subscriber[Any])

  /**
   * @param key The key to unsubscribe from
   * @param index The index of the subscription to cancel
   * @param writer True if the given index is a Writer, false if it is a Reader
   */
  case class Unsubscribe(key: Any, index: Long, writer: Boolean)

  /**
   * Signals that the subscription with the given key and index has additional demand
   */
  case class UpdateDemand(key: Any, index: Long, demand: Long, writer: Boolean)

  /**
   * Adds a value to the given key, with `subscriber` requiring feedback about its propagation
   * @param key The key to write to
   * @param value The value to add to `key`
   * @param joinSemilattice How to combine `value` with an existing value for `key`
   * @param subscriber The subscriber for `WriteNotification`s
   * @tparam V The type of the value in `key`
   */
  case class Write[V](key: Any, value: V, joinSemilattice: JoinSemilattice[V], subscriber: Subscriber[WriteNotification])

  /**
   * Signals that the write was stored in the memory state. This happens max. 1 time and should be the first event
   */
  case class Persisted() extends WriteNotification

  /**
   * Signals that the write was propagated to a Subscriber.
   *
   * @param query The internal index of the query, only there to aid in identification (may be removed in the future)
   */
  case class Propagated(query: Long) extends WriteNotification
}
