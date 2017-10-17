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

/**
 * A remove-wins graph is an unconstrained directed graph, where remove of a vertex is prioritized over concurrently
 * adding an edge to this vertex.
 */
object RWGraph {

  //  type RWGraph[E, V, R <: Edge[V]] = CausalCRDT[(DotMap[E, V, DotSet[E]])]
  //  trait Edge[V] {
  //    def initial: V
  //
  //    def terminal: V
  //  }
  //
  //  def empty[E, V, R <: Edge[V]]: RWGraph[E, V, R] = apply(DotMap.empty, DotMap.empty, CausalContext.empty)
}
//
//case class RWGraph[E, V, R <: Edge[V]](vertices: DotMap[E, V, DotSet[E]], edges: DotMap[E, R, DotSet[E]], context: CausalContext[E]) {
//
//  def addVertex(i: E)(vertex: V): RWGraph[E, V, R] = {
//    // TODO: check if this replica already added this
//    //    if (!vertices.dotMap.get(vertex).forall(_.events.exists(_.replica == i))) {
//    val nextDot = context.nextEventId(i)
//    RWGraph(DotMap(Map(vertex → DotSet.single(nextDot))), DotMap.empty, CausalContext(vertices.eventIDsFor(vertex) + nextDot))
//    //    } else
//    //      RWGraph.empty
//  }
//
//  def addEdge(i: E)(edge: R): RWGraph[E, V, R] = {
//    if (contains(edge.initial) && contains(edge.terminal)) {
//      val nextDot = context.nextEventId(i)
//      RWGraph(DotMap.empty, DotMap(Map(edge → DotSet.single(nextDot))), CausalContext(edges.eventIDsFor(edge) + nextDot))
//    } else
//      RWGraph.empty
//  }
//
//  def removeDisconnectedVertex(i: E)(vertex: V): RWGraph[E, V, R] = {
//    if (contains(vertex) && edges.dotMap.keys.forall(e ⇒ e.initial != vertex && e.terminal != vertex))
//      RWGraph(DotMap.empty, DotMap.empty, CausalContext(vertices.eventIDsFor(vertex)))
//    else
//      RWGraph.empty
//  }
//
//  def removeVertex(i: E)(vertex: V): RWGraph[E, V, R] = {
//    if (contains(vertex)) {
//      val connected: Iterable[SetCRDT[E]] = edges.dotMap.collect {
//        case (edge, dotset) if edge.initial == vertex || edge.terminal == vertex ⇒ dotset.dots
//      }
//      val dots: SetCRDT[E] = vertices.eventIDsFor(vertex) ++ connected.flatten
//      RWGraph(DotMap.empty, DotMap.empty, CausalContext(dots))
//    } else
//      RWGraph.empty
//  }
//
//  def removeEdge(i: E)(edge: R): RWGraph[E, V, R] = {
//    if (contains(edge))
//      RWGraph(DotMap.empty, DotMap.empty, CausalContext(edges.eventIDsFor(edge)))
//    else
//      RWGraph.empty
//  }
//
//  def contains(vertex: V): Boolean = vertices.dotMap.contains(vertex)
//
//  def contains(edge: R): Boolean = edges.dotMap.contains(edge) && contains(edge.initial) && contains(edge.terminal)
//}
