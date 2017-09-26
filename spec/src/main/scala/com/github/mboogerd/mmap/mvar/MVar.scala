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

import org.reactivestreams.Publisher

/**
 * MVar wraps a single monotonic value, typically a single causal context. When updated, it notifies a reactive-streams
 * observer, which publishes the delta to any (MVar) subscribers.
 *
 * Example:
 * x: UpdatableMVar[Byte] = ec.mvar
 * y: UpdatableMVar[Int] = ec.mvar
 * z = x.map(Char.fromAscii) Create new MappedMVar with backing subscriber and function
 * joined: MVar[(Int, Char)] = y.product(z)
 *
 * x.update(100) (updatable -> sync atomic update on MVar for sample -> async broadcast to subscribers)
 * y.update(50) (updatable -> sync atomic update on MVar for sample -> async broadcast to subscribers)
 * eventually(joined.sample() shouldBe (50, Char(100)))
 * Neither `z` nor `joined` are updatable
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
   * Exposes the reactive-streams Publisher interface so that derived MVars can subscribe. This Publisher will send
   * the state of this MVar, and all future evolutions, to the Subscriber. Dependent on the type of `S`, this may be
   * a stream of deltas or a stream of full states.
   */
  def publisher: Publisher[S]
}