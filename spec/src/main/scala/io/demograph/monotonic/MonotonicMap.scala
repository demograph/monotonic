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

package io.demograph.monotonic

import algebra.lattice.BoundedJoinSemilattice
import io.demograph.monotonic.mvar.UpdatableMVar

import scala.reflect.runtime.universe._
/**
 * This defines the central interface to MonotonicMap implementations.
 *
 * MonotonicMap implementations define live queries:
 * - query: Rather than retrieving a snapshot of a particular instant in time, retrieves a reactive stream of deltas
 * - write: Rather than returning success/failure, returns a reactive stream of progress indications for writes.
 */
trait MonotonicMap[K] {

  /**
   * Retrieves the monotonic variable that supposedly is stored under `Key`. If the key does not exist, it will
   * instantiate a new variable with initialValue, and register it for the given key
   */
  def get[V: BoundedJoinSemilattice: TypeTag](key: K, initialValue: V): UpdatableMVar[V]

  /**
   * Retrieves the monotonic variable that is stored in this map under `key`, or returns a new bound `MVar` starting
   * at the bottom value for `V`
   *
   * @param key
   * @tparam V
   * @return
   */
  def get[V: BoundedJoinSemilattice: TypeTag](key: K): UpdatableMVar[V] = get(key, BoundedJoinSemilattice[V].zero)

  /**
   * Binds the given mvar to the given key.
   */
  def put[V: BoundedJoinSemilattice: TypeTag](key: K, mvar: UpdatableMVar[V]): Unit

  //  /**
  //    * TODO: Binds the given mvar to the given key. The given monitor will be fed notifications of write progress.
  //    * @param key
  //    * @param mvar
  //    * @param monitor
  //    * @tparam V
  //    */
  //  def put[V: JoinSemilattice](key: K, mvar: MVar[V], monitor: Subscriber[WriteNotification]): Unit
}