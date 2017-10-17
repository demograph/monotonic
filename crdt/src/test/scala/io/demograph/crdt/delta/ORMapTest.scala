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

import cats.instances.list._
import cats.syntax.foldable._
import cats.syntax.semigroup._
import io.demograph.crdt.delta.dot.Dot
import io.demograph.crdt.implicits.all._
import io.demograph.crdt.instances.{ AWSet, ORMap }
import io.demograph.crdt.util.{ CRDTTestImplicits, EventStoreImplicits }
import io.demograph.crdt.{ Session, TestSpec }

/**
 *
 */
class ORMapTest extends TestSpec with CRDTTestImplicits with EventStoreImplicits {

  behavior of "ORMap"

  final val initial = withSession(0)(implicit s ⇒ ORMap.empty[Dot[Int], String, AWSet.DS[Dot[Int], String]])

  it should "add bottom elements by default" in {
    initial should have size 0

    val addKeyToMap = withSession(0)(implicit s ⇒ initial.mutateValue(identity, "testkey"))
    addKeyToMap should have size 1

    val empty = addKeyToMap.get("testkey")

    empty shouldBe 'defined
    empty.value should have size 0
  }

  it should "allow modification of a value for a given key" in {
    val addKeyToMap = withSession(0)(implicit s ⇒ initial.mutateValue(_.add("testvalue"), "testkey"))
    addKeyToMap should have size 1

    val testKeyWithValue = addKeyToMap.get("testkey")
    testKeyWithValue shouldBe 'defined
    testKeyWithValue.value should contain("testvalue")
  }

  it should "join sequential updates" in {
    val update1 = withSession(0)(implicit s ⇒ initial.mutateValue(_.add("value1"), "key1"))
    val update2 = withSession(1)(implicit s ⇒ update1.mutateValue(_.add("value2"), "key2"))
    val update3 = withSession(2)(implicit s ⇒ update2.mutateValue(_.add("value3"), "key1"))

    val aggregateDelta = update1 |+| update2 |+| update3

    aggregateDelta should have size 2
    aggregateDelta.get("key1").value should have size 2
    aggregateDelta.get("key2").value should have size 1
  }

  it should "join concurrent updates" in {
    val map = List(
      withSession(0)(implicit s ⇒ initial.mutateValue(_.add("value1"), "key1")),
      withSession(1)(implicit s ⇒ initial.mutateValue(_.add("value2"), "key2")),
      withSession(2)(implicit s ⇒ initial.mutateValue(_.add("value3"), "key1")),
      withSession(3)(implicit s ⇒ initial.mutateValue(_.add("value4"), "key2"))).combineAll

    map should have size 2
    map.get("key1").value should have size 2
    map.get("key2").value should have size 2
  }

  it should "represent deletions within nested datastructures as deltas" in {
    val map = List(
      withSession(0)(implicit s ⇒ initial.mutateValue(_.add("value1"), "key1")),
      withSession(1)(implicit s ⇒ initial.mutateValue(_.add("value2"), "key2")),
      withSession(2)(implicit s ⇒ initial.mutateValue(_.add("value1"), "key1"))).combineAll

    val deletion = withSession(0)(implicit session ⇒ map.mutateValue(_.remove("value1"), "key1"))

    deletion should have size 1
    deletion.context should contain only (Dot(0, 0), Dot(2, 0))
    deletion.get("key1").value should have size 0
    deletion.get("key1").value.dots should have size 0
  }

  it should "represent updates within nested datastructures as deltas" in {
    val map = List(
      withSession(0)(implicit session ⇒ initial.mutateValue(_.add("value1"), "key1")),
      withSession(1)(implicit session ⇒ initial.mutateValue(_.add("value2"), "key2")),
      withSession(2)(implicit session ⇒ initial.mutateValue(_.add("value3"), "key1"))).combineAll

    val addition = withSession(0)(implicit session ⇒ map.mutateValue(_.add("value5"), "key1"))
    addition should have size 1
    addition.get("key1").value should have size 1
    addition.get("key1").value.dots should have size 1
  }

  def withSession[H, T](sessionId: H)(f: Session[H] ⇒ T): T = {
    val session = new Session[H] {
      override def localhost = sessionId
    }
    f(session)
  }
}
