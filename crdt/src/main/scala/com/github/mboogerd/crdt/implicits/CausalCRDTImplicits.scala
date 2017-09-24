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

package com.github.mboogerd.crdt.implicits

import algebra.lattice.BoundedJoinSemilattice
import com.github.mboogerd.crdt.delta.causal.CausalCRDT.CausalCRDTBJSL
import com.github.mboogerd.crdt.delta.causal.{ Causal, CausalCRDT }
import com.github.mboogerd.crdt.delta.dot.{ DotMap, DotSet, DotStore }
import com.github.mboogerd.crdt.delta.flag.EWFlag
import com.github.mboogerd.crdt.delta.flag.EWFlagInstances.CausalCRDTewFlag
import com.github.mboogerd.crdt.delta.graph.GraphInstances.CausalCRDTrwGraph
import com.github.mboogerd.crdt.delta.graph.RWGraph
import com.github.mboogerd.crdt.delta.graph.RWGraph.Edge
import com.github.mboogerd.crdt.delta.map.ORMap
import com.github.mboogerd.crdt.delta.map.ORMapInstances.CausalCRDTorMap
import com.github.mboogerd.crdt.delta.set.{ AWSet, RWSet }
import com.github.mboogerd.crdt.delta.set.CRDTSetInstances.{ CausalCRDTawSet, CausalCRDTrwSet }

/**
 *
 */
trait CausalCRDTImplicits {

  /**
   * Given a Causal instance for a DotStore, and a CausalCRDT based on this DotStore, we prove that this type is a BoundedJoinSemiLattice
   */
  implicit def causalCRDTBJSL[I, DS, T](implicit crdt: CausalCRDT[I, DS, T], causal: Causal[I, DS]): BoundedJoinSemilattice[T] = new CausalCRDTBJSL[I, DS, T]

  /* Enable-Wins Flag */
  implicit def ewFlagCausalCRDT[I]: CausalCRDT[I, DotSet[I], EWFlag[I]] = new CausalCRDTewFlag[I]

  /* Add-Wins Set */
  implicit def awSetCausalCRDT[I, E]: CausalCRDT[I, AWSet.DS[I, E], AWSet[I, E]] = new CausalCRDTawSet[I, E]

  /* Remove-Wins Set */
  implicit def rwSetCausalCRDT[I, E]: CausalCRDT[I, RWSet.DS[I, E], RWSet[I, E]] = new CausalCRDTrwSet[I, E]

  /* Observed-Removed Map */
  implicit def orMapCausalCRDT[I, K, V, C](implicit ds: DotStore[V, I], causal: CausalCRDT[I, V, C]): ORMap.CRDT[I, K, V, C] = new CausalCRDTorMap[I, K, V, C]

  /* Remove(vertex)-Wins Graph */
  implicit def rwGraphCausalCRDT[I, V, E <: Edge[V]]: CausalCRDT[I, (DotMap[I, V, DotSet[I]], DotMap[I, E, DotSet[I]]), RWGraph[I, V, E]] = new CausalCRDTrwGraph[I, V, E]

}
