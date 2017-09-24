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

package com.github.mboogerd.crdt.delta

import algebra.lattice.BoundedJoinSemilattice
import cats.instances.list._
import cats.syntax.foldable._
import com.github.mboogerd.crdt.TestSpec
import com.github.mboogerd.crdt.delta.causal.{ Causal, CausalCRDT }
import com.github.mboogerd.crdt.delta.dot.DotStore
import com.github.mboogerd.crdt.delta.map.ORMap
import com.github.mboogerd.crdt.delta.set.AWSet
import com.github.mboogerd.crdt.delta.set.AWSet.DS
import com.github.mboogerd.crdt.implicits.all._
import com.github.mboogerd.crdt.syntax.joinSyntax._
/**
 *
 */
class ORMapTest extends TestSpec {

  behavior of "ORMap"

  it should "add bottom elements by default" in {
    val map = ORMap.empty[Int, String, DS[Int, String], AWSet[Int, String]]
    map should have size 0

    val addKeyToMap = map.apply(identity, "testkey")
    addKeyToMap should have size 1

    val empty = addKeyToMap.get("testkey")
    empty shouldBe 'defined

    empty.value shouldBe 'empty
  }

  it should "allow modification of a value for a given key" in {
    val map = ORMap.empty[Int, String, DS[Int, String], AWSet[Int, String]]

    val addKeyToMap = map.apply(_.add(1)("testvalue"), "testkey")
    addKeyToMap should have size 1

    val testKeyWithValue = addKeyToMap.get("testkey")
    testKeyWithValue shouldBe 'defined
    testKeyWithValue.value should contain("testvalue")
  }

  it should "join sequential updates" in {
    val map = ORMap.empty[Int, String, DS[Int, String], AWSet[Int, String]]
    val modified = updateSequentially(map)(
      ("key1", _.add(0)("value1")),
      ("key2", _.add(1)("value2")),
      ("key3", _.add(0)("value3")),
      ("key4", _.add(1)("value4")))

    modified should have size 4
  }

  it should "join concurrent updates" in {
    val map = ORMap.empty[Int, String, DS[Int, String], AWSet[Int, String]]
    val modified = updateConcurrently(map)(
      ("key1", _.add(0)("value1")),
      ("key2", _.add(1)("value2")),
      ("key1", _.add(2)("value3")),
      ("key2", _.add(3)("value4")))

    modified should have size 2
    modified.get("key1").value should have size 2
    modified.get("key2").value should have size 2
  }

  it should "represent deletions within nested datastructures as deltas" in {
    val map = updateConcurrently(ORMap.empty[Int, String, DS[Int, String], AWSet[Int, String]])(
      ("key1", _.add(0)("value1")),
      ("key2", _.add(1)("value2")),
      ("key1", _.add(2)("value3")),
      ("key2", _.add(3)("value4")))

    val deletion = map.apply(_.remove(0)("value1"), "key1")
    deletion should have size 1
    deletion.get("key1").value should have size 0
    deletion.get("key1").value.context.dots should have size 1
  }

  it should "represent updates within nested datastructures as deltas" in {
    val map = updateConcurrently(ORMap.empty[Int, String, DS[Int, String], AWSet[Int, String]])(
      ("key1", _.add(0)("value1")),
      ("key2", _.add(1)("value2")),
      ("key1", _.add(2)("value3")),
      ("key2", _.add(3)("value4")))

    val addition = map.apply(_.add(0)("value5"), "key1")
    addition should have size 1
    addition.get("key1").value should have size 1
    addition.get("key1").value.context.dots should have size 1
  }

  def updateSequentially[I, K, V, C](map: ORMap[I, K, V, C])(kvs: (K, C ⇒ C)*)(implicit ds: DotStore[V, I], crdt: CausalCRDT[I, V, C], causal: Causal[I, V]): ORMap[I, K, V, C] = {
    kvs.foldLeft(map) {
      case (m, (key, mutate)) ⇒
        val delta: ORMap[I, K, V, C] = m.apply(mutate, key)
        m.join(delta)
    }
  }

  def updateConcurrently[I, K, V, C](map: ORMap[I, K, V, C])(kvs: (K, C ⇒ C)*)(implicit ds: DotStore[V, I], crdt: CausalCRDT[I, V, C], causal: Causal[I, V]): ORMap[I, K, V, C] = {
    val updates = kvs.map { case (key, update) ⇒ map.apply(update, key) }.toList
    updates.foldMap(identity)(BoundedJoinSemilattice[ORMap[I, K, V, C]].joinSemilattice)
  }
}
