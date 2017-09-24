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

package com.github.mboogerd.crdt.delta.set

import com.github.mboogerd.crdt.delta.causal.CausalContext

/**
 *
 */
object CRDTSetInstances {

  class CausalCRDTawSet[I, E] extends AWSet.CRDT[I, E] {
    override def dotStore(t: AWSet[I, E]): AWSet.DS[I, E] = t.dotStore

    override def context(t: AWSet[I, E]): CausalContext[I] = t.context

    override def instance(dotStore: AWSet.DS[I, E], context: CausalContext[I]): AWSet[I, E] = AWSet(dotStore, context)
  }

  class CausalCRDTrwSet[I, E] extends RWSet.CRDT[I, E] {
    override def dotStore(t: RWSet[I, E]): RWSet.DS[I, E] = t.dotStore

    override def context(t: RWSet[I, E]): CausalContext[I] = t.context

    override def instance(dotStore: RWSet.DS[I, E], context: CausalContext[I]): RWSet[I, E] = RWSet(dotStore, context)
  }
}
