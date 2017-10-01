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

import io.demograph.crdt.delta.causal.CausalContext
import io.demograph.crdt.delta.dot.{ DotMap, DotSet, Dots }
import io.demograph.crdt.delta.graph.RWGraph.Edge
import io.demograph.crdt.implicits.all._

import scala.collection.immutable.Iterable
/**
 * A remove-wins graph is an unconstrained directed graph, where remove of a vertex is prioritized over concurrently
 * adding an edge to this vertex.
 */
object RWGraph {

  trait Edge[V] {
    def initial: V

    def terminal: V
  }

  def empty[I, V, E <: Edge[V]]: RWGraph[I, V, E] = apply(DotMap.empty, DotMap.empty, CausalContext.empty)
}

case class RWGraph[I, V, E <: Edge[V]](vertices: DotMap[I, V, DotSet[I]], edges: DotMap[I, E, DotSet[I]], context: CausalContext[I]) {

  def addVertex(i: I)(vertex: V): RWGraph[I, V, E] = {
    // TODO: check if this replica already added this
    //    if (!vertices.dotMap.get(vertex).forall(_.dots.exists(_.replica == i))) {
    val nextDot = context.nextDot(i)
    RWGraph(DotMap(Map(vertex → DotSet.single(nextDot))), DotMap.empty, CausalContext(vertices.dots(vertex) + nextDot))
    //    } else
    //      RWGraph.empty
  }

  def addEdge(i: I)(edge: E): RWGraph[I, V, E] = {
    if (contains(edge.initial) && contains(edge.terminal)) {
      val nextDot = context.nextDot(i)
      RWGraph(DotMap.empty, DotMap(Map(edge → DotSet.single(nextDot))), CausalContext(edges.dots(edge) + nextDot))
    } else
      RWGraph.empty
  }

  def removeDisconnectedVertex(i: I)(vertex: V): RWGraph[I, V, E] = {
    if (contains(vertex) && edges.dotMap.keys.forall(e ⇒ e.initial != vertex && e.terminal != vertex))
      RWGraph(DotMap.empty, DotMap.empty, CausalContext(vertices.dots(vertex)))
    else
      RWGraph.empty
  }

  def removeVertex(i: I)(vertex: V): RWGraph[I, V, E] = {
    if (contains(vertex)) {
      val connected: Iterable[Dots[I]] = edges.dotMap.collect {
        case (edge, dotset) if edge.initial == vertex || edge.terminal == vertex ⇒ dotset.dots
      }
      val dots: Dots[I] = vertices.dots(vertex) ++ connected.flatten
      RWGraph(DotMap.empty, DotMap.empty, CausalContext(dots))
    } else
      RWGraph.empty
  }

  def removeEdge(i: I)(edge: E): RWGraph[I, V, E] = {
    if (contains(edge))
      RWGraph(DotMap.empty, DotMap.empty, CausalContext(edges.dots(edge)))
    else
      RWGraph.empty
  }

  def contains(vertex: V): Boolean = vertices.dotMap.contains(vertex)

  def contains(edge: E): Boolean = edges.dotMap.contains(edge) && contains(edge.initial) && contains(edge.terminal)
}
