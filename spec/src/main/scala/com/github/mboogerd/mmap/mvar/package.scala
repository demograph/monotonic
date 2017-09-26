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
