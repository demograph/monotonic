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

package io.demograph.crdt.delta.graph

import io.demograph.crdt.delta.causal.{ CausalCRDT, CausalContext }
import io.demograph.crdt.delta.dot.{ DotMap, DotSet }
import io.demograph.crdt.delta.graph.RWGraph.Edge

/**
 *
 */
object GraphInstances {

  class CausalCRDTrwGraph[I, V, E <: Edge[V]] extends CausalCRDT[I, (DotMap[I, V, DotSet[I]], DotMap[I, E, DotSet[I]]), RWGraph[I, V, E]] {

    override def dotStore(t: RWGraph[I, V, E]): (DotMap[I, V, DotSet[I]], DotMap[I, E, DotSet[I]]) = (t.vertices, t.edges)

    override def context(t: RWGraph[I, V, E]): CausalContext[I] = t.context

    override def instance(dotStore: (DotMap[I, V, DotSet[I]], DotMap[I, E, DotSet[I]]), context: CausalContext[I]): RWGraph[I, V, E] =
      RWGraph(dotStore._1, dotStore._2, context)
  }
}
