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

package com.github.mboogerd.monotonic

import akka.actor.{ ActorRef, ActorSystem }
import algebra.lattice.BoundedJoinSemilattice
import com.github.mboogerd.monotonic.InMemMVarActor.UpdateValue
import com.github.mboogerd.monotonic.mvar.{ AtomicMVar, MVar, Updatable }

/**
 *
 */
object InMemMVar {

  //  class ObservableMVar[V: BoundedJoinSemilattice](observer: ActorRef)(implicit system: ActorSystem) extends AtomicMVar[V] {
  //    override def update(v1: V): Unit = {
  //      super.update(v1)
  //      observer ! UpdateValue(v1) // fire and forget
  //    }
  //  }
  //
  //  def apply[V: BoundedJoinSemilattice](implicit system: ActorSystem): MVar[V] with Updatable[V] =
  //    new ObservableMVar[V](system.actorOf(InMemMVarActor.props[V]))
}
