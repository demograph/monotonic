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

package io.demograph.crdt.delta.flag

import io.demograph.crdt.delta.causal.{ CausalCRDT, CausalContext }
import io.demograph.crdt.delta.dot.DotSet

/**
 *
 */
object EWFlagInstances {

  class CausalCRDTewFlag[I] extends CausalCRDT[I, DotSet[I], EWFlag[I]] {
    override def dotStore(t: EWFlag[I]): DotSet[I] = t.dotStore

    override def context(t: EWFlag[I]): CausalContext[I] = t.context

    override def instance(dotStore: DotSet[I], context: CausalContext[I]): EWFlag[I] = EWFlag(dotStore, context)
  }
}
