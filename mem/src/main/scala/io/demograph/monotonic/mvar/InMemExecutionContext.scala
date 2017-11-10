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

package io.demograph.monotonic.mvar

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import algebra.lattice.{ BoundedJoinSemilattice, JoinSemilattice }

/**
 *
 */
class InMemExecutionContext(implicit system: ActorSystem) extends ExecutionContext {

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  override def mvar[S: BoundedJoinSemilattice]: UpdatableMVar[S] = mvar(BoundedJoinSemilattice[S].zero)

  override def mvar[S: JoinSemilattice](initialValue: S): UpdatableMVar[S] =
    new WritableMVar[S](initialValue)

  override def map[S: JoinSemilattice, T: BoundedJoinSemilattice](mvarS: MVar[S])(f: (S) ⇒ T): MVar[T] = {
    // Subscribe for updates on `mvarS`
    // For each update `u`, apply `f` to obtain `f(u)`
    // Update the MVar[T] with `f(u)`
    // Propagate `f(u)` to all subscribers
    new MapMVar[S, T](mvarS, f, BoundedJoinSemilattice[T].zero)
  }

  override def product[S: BoundedJoinSemilattice, T: BoundedJoinSemilattice](mvarS: MVar[S])(mvarT: MVar[T]): MVar[(S, T)] = {
    // Subscribe for updates on `mvarS`
    // Subscribe for updates on `mvarT`
    // For each update `s`, lift `s` to `(s, t.bottom)`
    // For each update `t`, lift `t` to `(s.bottom, t)`
    // Merge updates from both subscriptions into a single subscription for `(s, t)`
    // Update the MVar[(S, T)] with `(s, t)`
    // Propagate `(s, t)` to all subscribers
    new ProductMVar[S, T](mvarS, mvarT)
  }
}

object InMemExecutionContext {
  def apply()(implicit system: ActorSystem): InMemExecutionContext = new InMemExecutionContext()(system)
}