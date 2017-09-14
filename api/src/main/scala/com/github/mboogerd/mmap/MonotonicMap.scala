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
import org.reactivestreams.Publisher

/**
 * This defines the central interface to MonotonicMap implementations.
 *
 * MonotonicMap implementations define live queries:
 * - query: Rather than retrieving a snapshot of a particular instant in time, retrieves a reactive stream of deltas
 * - write: Rather than returning success/failure, returns a reactive stream of progress indications for writes.
 */
trait MonotonicMap[K] {

  /**
   * Attempts to read a stream of updates for `key` as type `V`
   *
   * @param key The key to read an update-stream from
   * @tparam V The value type, which should be a JoinSemilattice (only such values can be written and thus cause updates)
   * @return A (never-terminating) Publisher with updates for `key` of type `V`. The stream is expected to fail iif any
   *         element cannot be handled as if being of type `V`. If no Subscriber is created for the Publisher, or the
   *         subscriber terminates, the Publisher is expected to clean up after itself.
   */
  def read[V](key: K): Publisher[V]

  /**
   * Attempts to write `value` to `key`. We expect a `JoinSemilattice` for `V` as we ought to be able to merge any
   * value, if one were to exist.
   *
   * @param key   The key to write the new value to
   * @param value The value to be written
   * @tparam V The type of the value, which should be compatible with any existing value
   * @return A Publisher that propagates updates of the writes. This Publisher should never fail except for fatal JVM
   *         exceptions. All other errors should be transformed to instances of `WriteNotification`. Implementations
   *         are expected to clean up after themselves if subscribers terminate. Implementations are expected to
   *         close the stream cleanly if no further progress can be made and no FatalFailure occurred.
   */
  def write[V: JoinSemilattice](key: K, value: V): Publisher[WriteNotification]

}