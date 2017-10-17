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

package io.demograph.crdt.implicits

import algebra.lattice.JoinSemilattice
import io.demograph.crdt.Session
import io.demograph.crdt.delta.causal.{ CausalCRDT, CausalContext }
import io.demograph.crdt.delta.dot.{ Dot, DotFun }
import io.demograph.crdt.implicits.DotImplicits._
import io.demograph.crdt.instances.MVRegister.{ MVRegister, Mutate, Query }

/**
 *
 */
trait MVRegisterImplicits {

  implicit def queryMVRegister[E, V: JoinSemilattice]: Query[E, V] =
    (register: MVRegister[E, V]) => register.eventStore.dotFun.values

  implicit def mutateMVRegister[H: Session, V: JoinSemilattice]: Mutate[Dot[H], V] = new Mutate[Dot[H], V] {
    override def write(register: MVRegister[Dot[H], V])(v: V): MVRegister[Dot[H], V] = {
      val nextDot = register.context.nextDot
      CausalCRDT(DotFun((nextDot, v)), CausalContext.from(register.eventStore.dots + nextDot))
    }

    override def clear(register: MVRegister[Dot[H], V]): MVRegister[Dot[H], V] =
      CausalCRDT(DotFun.empty, CausalContext.from(register.eventStore.dots))
  }
}

object MVRegisterImplicits extends MVRegisterImplicits