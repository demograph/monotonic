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

import io.demograph.crdt.Session
import io.demograph.crdt.delta.causal.CausalCRDT
import io.demograph.crdt.delta.dot.{ Dot, DotSet }
import io.demograph.crdt.implicits.DotImplicits._
import io.demograph.crdt.instances.EWFlag._

/**
 *
 */
trait EWFlagImplicits {

  implicit class QueryEWFlag[E](ewFlag: EWFlag[E]) extends Query[E] {
    override def read: Boolean = ewFlag.eventStore.dots.nonEmpty
  }

  implicit class MutateEWFlag[H: Session](ewFlag: EWFlag[Dot[H]]) extends Mutate[Dot[H]] {

    override def enable: EWFlag[Dot[H]] = {
      val nextDot = ewFlag.context.nextDot
      CausalCRDT(DotSet(nextDot), ewFlag.context + nextDot)
    }

    override def disable: EWFlag[Dot[H]] = CausalCRDT(DotSet.empty, ewFlag.context)
  }
}

object EWFlagImplicits extends EWFlagImplicits