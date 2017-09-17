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

package com.github.mboogerd.mmap.mvar

import algebra.lattice.BoundedJoinSemilattice
import org.reactivestreams.Publisher

/**
 * This represents a monotonic variable with a single causal context, possibly shared with many peers.
 *
 * TODO:
 * - Rewrite MVar to expose a Publisher interface (needs multi-subscribe + single-element merging queue)
 * - Allow for MVar/Publisher to write back to MonotonicMap
 * - Allow writing derived MVars, say `map`
 *   - Observer interface
 *   - Broadcasting Publisher with `queueSize` for all subscribers
 *   - Later? We want to have an explicit representation of the logical/computational pipeline for the MVar.
 *     - We can quite easily represent non-parameterized stages (such as product/intersect/union),
 *       but not functional/higher-order ones (map, filter, fold) because we lack a representation for the function
 *   - Decide where to store a representation of the logical pipeline... (it seems like it could be a graph CRDT?
 *     or each node can be a Top-most CRDT under its own key? i.e. no re-writes allowed?)
 *
 * MVar design
 * - MVar can be instantiated standalone: Updates are written synchronously
 * - MVar can be instantiated as a replica: Updates are exchanged asynchronously
 * - MVar can be instantiated as a derivative: Updates are received asynchronously
 *   - product / zip
 *   - lexicographic product
 *   - linear sum
 *   - mmap (with higher order inflations on the inner Set(Map)-like lattice)
 *     - map (can change signature, i.e. monotonic but no inflation)
 *     - filter
 *     - fold (can change signature i.e. monotonic but no inflation)
 *     - union
 *     - intersection
 *     - product (will change signature i.e. monotonic but no inflation)
 *     - diff
 *
 * MVar wraps a single monotonic value, typically a single causal context. When updated, it notifies a reactive-streams
 * observer, which publishes the delta to any (MVar) subscribers.
 *
 * Example:
 * x: UpdatableMVar[Byte]
 * y: UpdatableMVar[Int]
 * z = x.mmap(Char.fromAscii) Create new MappedMVar with backing subscriber and function
 * joined: MVar[(Int, Char)] = y.product(z)
 *
 * x.update(100) (updatable -> sync atomic update on MVar for sample -> async broadcast to subscribers)
 * y.update(50) (updatable -> sync atomic update on MVar for sample -> async broadcast to subscribers)
 * joined.sample() shouldBe (50, Char(100))
 * Neither `z` nor `joined` should be updatable
 */
trait MVar[S] {

  /**
   * @return A snapshot of the current state of this variable
   */
  def sample: S

  /**
   * When an update is dispatched, it can be handled here. Responsibilities include:
   * - Updating the state of the `NVar`
   * - Notifying any subscribers
   *
   * @param s
   */
  protected[mvar] def onUpdate(s: S): Unit = ()

  /**
   * Exposes the reactive-streams Publisher interface
   */
  def publisher: Publisher[S]
}

object MVar {

  //
  //  class AtomicMVar[V: JoinSemilattice] private[this](value: AtomicReference[V]) extends MVar[V] {
  //    override def sample: V = value.get()
  //  }
  //
  //  def updatable[V: JoinSemilattice](initialValue: V): (Updatable[V], MVar[V]) = {
  //    val ref = new AtomicReference[V](initialValue)
  //    (new AtomicUpdatable[V](ref), new AtomicMVar[V](ref))
  //  }
  //
  //  def updatable[V: BoundedJoinSemilattice]: (Updatable[V], MVar[V]) = updatable[V](BoundedJoinSemilattice.zero)
  //
  //  /**
  //   * @return An MVar compatible with delta-updates (it will join each update into the existing state)
  //   */
  //  def apply[V: BoundedJoinSemilattice](): MVar[V] = new AtomicMVar[V](BoundedJoinSemilattice[V].zero)

  /**
   * @return An MVar that will merge in all elements produced by the given publisher
   */
  //  def apply[V: BoundedJoinSemilattice](publisher: Publisher[V]): MVar[V] = {
  //    val (updatableMVar, mvar) = updatable[V]
  //    var subscription: Subscription = null
  //
  //    publisher.subscribe(new Subscriber[V] {
  //      override def onError(t: Throwable): Unit = () // log error?
  //      override def onComplete(): Unit = () // doing nothing is fine
  //      override def onNext(t: V): Unit = {
  //        subscription.request(1)
  //        updatableMVar.update(t)
  //      }
  //      override def onSubscribe(s: Subscription): Unit = {
  //        subscription = s
  //        subscription.request(1)
  //      }
  //    })
  //
  //    mvar
  //  }
}