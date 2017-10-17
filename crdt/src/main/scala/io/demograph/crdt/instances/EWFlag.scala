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

package io.demograph.crdt.instances

import io.demograph.crdt.Session
import io.demograph.crdt.delta.causal.{ CausalCRDT, CausalContext }
import io.demograph.crdt.delta.dot.{ Dot, DotSet }
import io.demograph.crdt.implicits.DotImplicits.dottedDot

/**
 * Enable-wins flag is a concurrently mutable boolean. Concurrent updates converge to an equivalent state on different
 * replicas, where enable trumps any concurrent disable.
 */
object EWFlag {
  type EWFlag[E] = CausalCRDT[E, DotSet[E]]

  def empty[H: Session]: EWFlag[Dot[H]] = CausalCRDT(DotSet.empty, CausalContext.empty)

  trait Query[E] {
    def read: Boolean
  }

  trait Mutate[E] {
    def enable: EWFlag[E]

    def disable: EWFlag[E]
  }

}