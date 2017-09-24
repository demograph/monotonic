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

package com.github.mboogerd.crdt.delta.flag

import com.github.mboogerd.crdt.delta.causal.CausalContext
import com.github.mboogerd.crdt.delta.dot.{ Dot, DotSet }
/**
 * Enable-wins flag is a concurrently mutable boolean. Concurrent updates converge to an equivalent state on different
 * replicas, where enable trumps any concurrent disable.
 */
object EWFlag {
  def empty[I]: EWFlag[I] = new EWFlag[I](DotSet.empty, CausalContext.empty)
}

case class EWFlag[I](dotStore: DotSet[I], context: CausalContext[I]) {

  def enable(i: I): EWFlag[I] = {
    val next: Dot[I] = context.nextDot(i)
    EWFlag(DotSet.single(next), CausalContext(context.dots + next))
  }

  def disable(i: I): EWFlag[I] = EWFlag[I](DotSet.empty, new CausalContext(dotStore.dots))

  def read: Boolean = dotStore.dots.nonEmpty
}
