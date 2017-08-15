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

import akka.stream.testkit.TestSubscriber
import akka.stream.testkit.TestSubscriber.Probe
import algebra.lattice.BoundedJoinSemilattice
import com.github.mboogerd.mmap.InMemMonotonicMapActor.{ Persisted, Propagated }
import org.reactivestreams.Publisher

import scala.concurrent.duration._
/**
 *
 */
class InMemMonotonicMapSpec extends ActorTestBase {

  behavior of "InMemoryMonotonicMap"

  case class Dummy(set: Set[String] = Set.empty)
  implicit object DummyLattice extends BoundedJoinSemilattice[Dummy] {
    override def zero: Dummy = Dummy()
    override def join(lhs: Dummy, rhs: Dummy): Dummy = Dummy(lhs.set ++ rhs.set)
  }

  it should "return an unproductive Producer if a key is queried without initial state or updates" in {
    val monotonicMap = InMemMonotonicMap[String]()
    val producer = monotonicMap.read[Dummy]("non-existing-key")

    val probe = TestSubscriber.probe[Dummy]()

    producer.subscribe(probe)
    probe.expectSubscription()

    probe.expectNoMsg(100.milliseconds)
  }

  it should "return a Producer producing only the initial value if one is provided, it is queried for and no other updates follow" in {
    val monotonicMap = InMemMonotonicMap[String](Map("key" → Dummy()))
    val producer = monotonicMap.read[Dummy]("key")

    val probe = TestSubscriber.probe[Dummy]()
    producer.subscribe(probe)

    probe.ensureSubscription()
    probe.expectNoMsg(100.milliseconds)

    probe.request(1)
    probe.expectNext(Dummy())
    probe.expectNoMsg(100.milliseconds)
  }

  it should "return a Producer for writes signaling a successful write to memory" in {
    val monotonicMap = InMemMonotonicMap[String]()
    val writeTracker = monotonicMap.write("key", Dummy())

    val probe = TestSubscriber.probe[WriteNotification]()
    writeTracker.subscribe(probe)

    probe.ensureSubscription()
    probe.expectNoMsg(100.milliseconds)

    probe.request(1)
    probe.expectNext(Persisted())
    probe.expectNoMsg(100.milliseconds)
  }

  it should "return a Reader that produces a previously written value" in {
    val monotonicMap = InMemMonotonicMap[String]()

    // Write then read
    subscribed(monotonicMap.write("key", Dummy()))
    val reader = monotonicMap.read("key")

    val probe = TestSubscriber.probe[Dummy]()
    reader.subscribe(probe)

    probe.ensureSubscription()
    probe.expectNoMsg(100.milliseconds)

    probe.request(1)
    probe.expectNext(Dummy())
    probe.expectNoMsg(100.milliseconds)
  }

  it should "return a Reader that produces a value after it is written" in {
    val monotonicMap = InMemMonotonicMap[String]()

    // Read then write
    val reader = monotonicMap.read("key")
    subscribed(monotonicMap.write("key", Dummy()))

    val probe = TestSubscriber.probe[Dummy]()
    reader.subscribe(probe)

    probe.ensureSubscription()
    probe.expectNoMsg(100.milliseconds)

    probe.request(1)
    probe.expectNext(Dummy())
    probe.expectNoMsg(100.milliseconds)
  }

  it should "return a Writer that signals propagation to an active Readers" in {
    val monotonicMap = InMemMonotonicMap[String]()

    // Read then write
    consumeAll(monotonicMap.read("key"))
    val writer = monotonicMap.write("key", Dummy())

    val probe = TestSubscriber.probe[WriteNotification]()
    writer.subscribe(probe)

    probe.ensureSubscription()
    probe.expectNoMsg(100.milliseconds)

    probe.request(2)
    probe.expectNext(Persisted(), Propagated(1)) // 1 because the query being propagated to was the first subscriber
    probe.expectNoMsg(100.milliseconds)
  }

  it should "return a Writer that signals propagation for Readers initiated after the write occurred" in {
    val monotonicMap = InMemMonotonicMap[String]()

    // Read then write
    val writer = monotonicMap.write("key", Dummy())

    val probe = TestSubscriber.probe[WriteNotification]()
    writer.subscribe(probe)

    probe.ensureSubscription()
    probe.expectNoMsg(100.milliseconds)

    probe.request(2)
    probe.expectNext(Persisted())
    probe.expectNoMsg(100.milliseconds)

    consumeAll(monotonicMap.read("key"))

    probe.expectNext(Propagated(2)) // 2 because the query being propagated to was the 2nd subscriber (the write preceded)
    probe.expectNoMsg(100.milliseconds)
  }

  it should "join two consecutive unconsumed writes" in {
    val monotonicMap = InMemMonotonicMap[String]()
    val w1 = monotonicMap.write("key", Dummy(Set("a")))
    val w2 = monotonicMap.write("key", Dummy(Set("b")))

    consumeAll(w1)
    consumeAll(w2)

    val probe = TestSubscriber.probe[Dummy]()
    val reader = monotonicMap.read[Dummy]("key")
    reader.subscribe(probe)

    probe.ensureSubscription()
    probe.expectNoMsg(100.milliseconds)

    probe.request(2)
    probe.expectNext(Dummy(Set("a", "b")))
    probe.expectNoMsg(100.milliseconds)
  }
  
  def subscribed[T](publisher: Publisher[T]): Probe[T] = {
    val probe = TestSubscriber.probe[T]()
    publisher.subscribe(probe)
    probe.ensureSubscription()
    probe
  }

  def consumeAll[T](publisher: Publisher[T]): Probe[T] = {
    val subscription = subscribed(publisher)
    subscription.request(Int.MaxValue)
  }
}
