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

package io.demograph.crdt.delta

import io.demograph.crdt.TestSpec
import io.demograph.crdt.delta.RWGraphTest.{ TestEdge, TestEntry }
import io.demograph.crdt.delta.graph.RWGraph
import io.demograph.crdt.delta.graph.RWGraph.Edge
import io.demograph.crdt.implicits.all._
import io.demograph.crdt.syntax.joinSyntax._
import org.scalacheck.Gen
/**
 *
 */
class RWGraphTest extends TestSpec {

  behavior of "RWGraph"

  val emptyGraph = RWGraph.empty[Int, TestEntry, TestEdge]

  /* Simple tests */

  it should "allow adding a vertex" in {
    val t1: TestEntry = TestEntry()
    val addVertex = emptyGraph.addVertex(0)(t1)
    val withVertex = emptyGraph join addVertex

    withVertex.contains(t1) shouldBe true
  }

  it should "allow adding vertices concurrently" in {
    val t1: TestEntry = TestEntry()
    val t2: TestEntry = TestEntry()
    val t3: TestEntry = TestEntry()
    val v1 = emptyGraph.addVertex(0)(t1)
    val v2 = emptyGraph.addVertex(1)(t2)
    val v3 = emptyGraph.addVertex(2)(t3)
    val vertices = v1 join v2 join v3

    vertices.contains(t1) shouldBe true
    vertices.contains(t2) shouldBe true
    vertices.contains(t3) shouldBe true
  }

  it should "allow adding vertices sequentially" in {
    val t1: TestEntry = TestEntry()
    val t2: TestEntry = TestEntry()
    val t3: TestEntry = TestEntry()
    val v1 = emptyGraph.addVertex(0)(t1)
    val v2 = v1.addVertex(0)(t2)
    val v3 = v2.addVertex(0)(t3)
    val vertices = v1 join v2 join v3

    vertices.contains(t1) shouldBe true
    vertices.contains(t2) shouldBe true
    vertices.contains(t3) shouldBe true
  }

  it should "prevent adding edges to non-existing vertices" in {
    emptyGraph.addEdge(0)(TestEdge(TestEntry(), TestEntry())) shouldBe emptyGraph
  }

  it should "allow adding an edge between vertices" in {
    val t1: TestEntry = TestEntry()
    val t2: TestEntry = TestEntry()
    val first = emptyGraph.addVertex(0)(t1)
    val second = emptyGraph.addVertex(1)(t2)
    val vertices = first join second

    val edge: TestEdge = TestEdge(t1, t2)
    val withEdge = vertices.addEdge(2)(edge)
    (vertices join withEdge).contains(edge) shouldBe true
  }

  it should "allow adding edges concurrently" in {

  }

  it should "allow adding edges sequentially" in {

  }

  it should "prevent removeDisconnected of a connected vertex" in {

  }

  it should "allow removal of an edge" in {

  }

  it should "properly deal with duplicate removal of an edge" in {

  }

  it should "allow removing a disconnected vertex" in {

  }

  it should "allow removal of a connected vertex by removing all connecting edges" in {

  }

}

object RWGraphTest {

  case class TestEntry(s: String = Gen.alphaStr.sample.get)

  case class TestEdge(initial: TestEntry, terminal: TestEntry) extends Edge[TestEntry]

}
