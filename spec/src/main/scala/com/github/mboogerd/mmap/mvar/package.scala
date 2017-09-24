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

package com.github.mboogerd.mmap

/**
 *
 */
package object mvar extends Implicits {

  type UpdatableMVar[S] = MVar[S] with Updatable[S]

  //
  //  /**
  //    * Explicit representation of the computational graph that maintains eventual consistency over MVar's
  //    */
  //  trait MVarModel[S] {
  //    def key: String
  //
  //    def inspect(implicit ec: ExecutionContext): MVar[S] = ???
  //  }
  //
  //  case class WritableMVarModel[S](key: String) extends MVarModel[S]
  //
  //  case class MapMVarModel[S, T](source: MVarModel[S], f: S ⇒ T) extends MVarModel[T] {
  //    def key: String = this.hashCode().toString
  //  }
  //
  //  case class ProductMVarModel[S, T](s1: MVarModel[S], s2: MVarModel[T]) extends MVarModel[(S, T)] {
  //    def key: String = this.hashCode().toString
  //  }
  //
  //  /**
  //    * Alternative implementation that is more graph-oriented
  //    */
  //  trait MVarNode
  //  case class MVarEdge(init: MVarNode, term: MVarNode)
  //
  //  case class WritableMVarNode() extends MVarNode
  //  case class MapMVarNode[S, T](f: S ⇒ T) extends MVarNode
  //  case class ProductMVarNode[S, T]()
  //  // The trouble here is that case class identity is established through its parameters, and we may have arity 0.
  //  // i.e. it may be better to have the MVarNode/Model parameters be interpreted as edges.

  /* Explicit representation of the computational graph */
  trait MVarNode {
    def sources: List[MVarNode]
  }
  case class WritableMVarNode() extends MVarNode {
    val sources: List[MVarNode] = List.empty
  }
  case class MapMVarNode(source: MVarNode) extends MVarNode {
    val sources: List[MVarNode] = List(source)
  }
  case class ProductMVarNode(s1: MVarNode, s2: MVarNode) extends MVarNode {
    val sources: List[MVarNode] = List(s1, s2)
  }
}
