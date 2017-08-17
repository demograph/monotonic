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
import algebra.lattice.BoundedJoinSemilattice
import com.github.mboogerd.mmap.InMemMonotonicMapActor.{Persisted, Propagated}

import scala.concurrent.duration._
import scala.util.Try

/**
  *
  */
class InMemMonotonicMapSpec extends ActorTestBase {

  behavior of "InMemoryMonotonicMap"

  override implicit def patienceConfig: PatienceConfig =
    super.patienceConfig.copy(timeout = remainingOrDefault, interval = 5.millisecond)

  override val remainingOrDefault: FiniteDuration = 20.millisecond

  case class Dummy(set: Set[String] = Set.empty)

  implicit object DummyLattice extends BoundedJoinSemilattice[Dummy] {
    override def zero: Dummy = Dummy()
    override def join(lhs: Dummy, rhs: Dummy): Dummy = Dummy(lhs.set ++ rhs.set)
  }

  it should "return an unproductive Producer if a key is queried without initial state or updates" in withStringMap() { monotonicMap ⇒
    val producer = monotonicMap.read[Dummy]("non-existing-key")

    val probe = TestSubscriber.probe[Dummy]()

    producer.subscribe(probe)
    probe.expectSubscription()

    probe.expectNoMsg(remainingOrDefault)
  }

  it should "return a Producer producing only the initial value if one is provided, it is queried for and no other updates follow" in
    withStringMap(Map("key" → Dummy())) { monotonicMap ⇒
    val producer = monotonicMap.read[Dummy]("key")

    val probe = TestSubscriber.probe[Dummy]()
    producer.subscribe(probe)

    probe.ensureSubscription()
    probe.expectNoMsg(remainingOrDefault)

    probe.request(1)
    probe.expectNext(Dummy())
    probe.expectNoMsg(remainingOrDefault)
  }

  it should "return a Producer for writes signaling a successful write to memory" in withStringMap() { monotonicMap ⇒
    val writeTracker = monotonicMap.write("key", Dummy())

    val probe = TestSubscriber.probe[WriteNotification]()
    writeTracker.subscribe(probe)

    probe.ensureSubscription()
    probe.expectNoMsg(remainingOrDefault)

    probe.request(1)
    probe.expectNext(Persisted())
    probe.expectNoMsg(remainingOrDefault)
  }

  it should "return a Reader that produces a previously written value" in withStringMap() { monotonicMap ⇒
    // Write then read
    subscribed(monotonicMap.write("key", Dummy()))
    val reader = monotonicMap.read("key")

    val probe = TestSubscriber.probe[Dummy]()
    reader.subscribe(probe)

    probe.ensureSubscription()
    probe.expectNoMsg(remainingOrDefault)

    probe.request(1)
    probe.expectNext(Dummy())
    probe.expectNoMsg(remainingOrDefault)
  }

  it should "return a Reader that produces a value after it is written" in withStringMap() { monotonicMap ⇒
    // Read then write
    val reader = monotonicMap.read("key")
    subscribed(monotonicMap.write("key", Dummy()))

    val probe = TestSubscriber.probe[Dummy]()
    reader.subscribe(probe)

    probe.ensureSubscription()
    probe.expectNoMsg(remainingOrDefault)

    probe.request(1)
    probe.expectNext(Dummy())
    probe.expectNoMsg(remainingOrDefault)
  }

  it should "return a Writer that signals propagation to an active Readers" in withStringMap() { monotonicMap ⇒
    // Read then write
    bufferAll(monotonicMap.read("key"))
    val writer = monotonicMap.write("key", Dummy())

    val probe = TestSubscriber.probe[WriteNotification]()
    writer.subscribe(probe)

    probe.ensureSubscription()
    probe.expectNoMsg(remainingOrDefault)

    probe.request(2)
    probe.expectNext(Persisted(), Propagated(1)) // 1 because the query being propagated to was the first subscriber
    probe.expectNoMsg(remainingOrDefault)
  }

  it should "return a Writer that signals propagation for Readers initiated after the write occurred" in withStringMap() { monotonicMap ⇒
    // Read then write
    val writer = monotonicMap.write("key", Dummy())

    val probe = TestSubscriber.probe[WriteNotification]()
    writer.subscribe(probe)

    probe.ensureSubscription()
    probe.expectNoMsg(remainingOrDefault)

    probe.request(2)
    probe.expectNext(Persisted())
    probe.expectNoMsg(remainingOrDefault)

    bufferAll(monotonicMap.read("key"))

    probe.expectNext(Propagated(2)) // 2 because the query being propagated to was the 2nd subscriber (the write preceded)
    probe.expectNoMsg(remainingOrDefault)
  }

  it should "join a write with an unconsumed initial state" in withStringMap(Map("key" → Dummy(Set("a")))) { monotonicMap ⇒
    val write = monotonicMap.write("key", Dummy(Set("b")))

    bufferAll(write)

    val probe = TestSubscriber.probe[Dummy]()
    val reader = monotonicMap.read[Dummy]("key")
    reader.subscribe(probe)

    probe.ensureSubscription()
    probe.expectNoMsg(remainingOrDefault)

    probe.request(2)
    probe.expectNext(Dummy(Set("a", "b")))
    probe.expectNoMsg(remainingOrDefault)
  }

  it should "join consecutive unconsumed writes" in withStringMap() { monotonicMap ⇒
    val w1 = monotonicMap.write("key", Dummy(Set("a")))
    val w2 = monotonicMap.write("key", Dummy(Set("b")))

    bufferAll(w1)
    bufferAll(w2)

    val probe = TestSubscriber.probe[Dummy]()
    val reader = monotonicMap.read[Dummy]("key")
    reader.subscribe(probe)

    probe.ensureSubscription()
    probe.expectNoMsg(remainingOrDefault)

    probe.request(2)
    probe.expectNext(Dummy(Set("a", "b")))
    probe.expectNoMsg(remainingOrDefault)
  }

  it should "broadcast a write to all readers" in withStringMap(Map("key" → Dummy(Set("a")))) { monotonicMap ⇒
    val write = monotonicMap.write("key", Dummy(Set("b")))

    bufferAll(write)

    val p1 = TestSubscriber.probe[Dummy]()
    val p2 = TestSubscriber.probe[Dummy]()
    val r1 = monotonicMap.read[Dummy]("key")
    val r2 = monotonicMap.read[Dummy]("key")
    r1.subscribe(p1)
    r2.subscribe(p2)

    p1.ensureSubscription()
    p1.expectNoMsg(remainingOrDefault)
    p2.ensureSubscription()
    p2.expectNoMsg(remainingOrDefault)

    p1.request(2)
    p1.expectNext(Dummy(Set("a", "b")))
    p1.expectNoMsg(remainingOrDefault)

    p2.request(2)
    p2.expectNext(Dummy(Set("a", "b")))
    p2.expectNoMsg(remainingOrDefault)
  }

  it should "not send messages to an unsubscribed Reader" in withStringMap() { monotonicMap ⇒
    val reader = monotonicMap.read("key")

    val probe = TestSubscriber.probe[Dummy]()
    reader.subscribe(probe)

    probe.ensureSubscription()
    probe.expectNoMsg(remainingOrDefault)

    probe.request(1)
    probe.cancel()

    subscribed(monotonicMap.write("key", Dummy()))

    probe.expectNoMsg(remainingOrDefault)
  }

  it should "not send messages to an unsubscribed Writer" in withStringMap() { monotonicMap ⇒
    val r1Probe = subscribed(monotonicMap.read[Dummy]("key"))
    val writerProbe = subscribed(monotonicMap.write("key", Dummy()))

    withClue("Reader propagation should not be signaled if there was no Writer demand before its cancellation") {
      r1Probe.request(1)
      r1Probe.expectNext(Dummy())

      writerProbe.cancel()

      writerProbe.expectNoMsg(remainingOrDefault)
    }

    withClue("Reader propagation should not be signaled to the Writer after its cancellation") {
      val r2Probe = bufferAll(monotonicMap.read[Dummy]("key"))
      r2Probe.expectNext(Dummy())

      writerProbe.expectNoMsg(remainingOrDefault)
    }
  }

  it should "send only WriteNotification to the initiator of the write" in withStringMap() { monotonicMap ⇒
    bufferAll(monotonicMap.read("key"))

    val w1 = bufferAll(monotonicMap.write("key", Dummy(Set("a"))))
    val w2 = bufferAll(monotonicMap.write("key", Dummy(Set("b"))))

    withClue("Two consecutive writes to an existing single reader should only result in one update each") {
      w1.expectNext(Persisted())
      w2.expectNext(Persisted())

      w1.expectNext(Propagated(1))
      w2.expectNext(Propagated(1))

      w1.expectNoMsg(remainingOrDefault)
      w2.expectNoMsg(remainingOrDefault)
    }

    withClue("Two consecutive writes to a later single reader should only result in one update each") {
      val expectedReaderIndex = 4
      // Because 1 query, 2,3 writer, 4 => new query
      bufferAll(monotonicMap.read("key"))

      w1.expectNext(Propagated(expectedReaderIndex))
      w2.expectNext(Propagated(expectedReaderIndex))

      w1.expectNoMsg(remainingOrDefault)
      w2.expectNoMsg(remainingOrDefault)
    }
  }

  it should "signal Propagation even if demand splits writes" in withStringMap() { monotonicMap ⇒
    val w1 = bufferAll(monotonicMap.write("key", Dummy(Set("a"))))
    val query = subscribed(monotonicMap.read[Dummy]("key"))

    w1.expectNext(Persisted())
    w1.expectNoMsg(100.millisecond)
    query.expectNoMsg(100.millisecond)

    query.request(2)
    query.expectNext(Dummy(Set("a")))
    w1.expectNext(Propagated(2))

    val w2 = bufferAll(monotonicMap.write("key", Dummy(Set("b"))))
    w2.expectNext(Persisted())
    query.expectNext(Dummy(Set("b")))
    w2.expectNext(Propagated(2))
  }

  it should "use batching when there is no demand and propagate opportunistically when there is" in withStringMap() { monotonicMap ⇒
    val reader = monotonicMap.read("key")

    val readerProbe = TestSubscriber.probe[Dummy]()
    reader.subscribe(readerProbe)
    readerProbe.ensureSubscription()

    subscribed(monotonicMap.write("key", Dummy(Set("a"))))
    subscribed(monotonicMap.write("key", Dummy(Set("b"))))
    subscribed(monotonicMap.write("key", Dummy(Set("c"))))
    subscribed(monotonicMap.write("key", Dummy(Set("d"))))

    // The first four should be received in batch, and satisfy only 1 demand
    readerProbe.request(3)
    readerProbe.expectNext(Dummy(Set("a", "b", "c", "d")))

    subscribed(monotonicMap.write("key", Dummy(Set("e"))))
    subscribed(monotonicMap.write("key", Dummy(Set("f"))))

    subscribed(monotonicMap.write("key", Dummy(Set("g"))))
    subscribed(monotonicMap.write("key", Dummy(Set("h"))))

    // The second set of two should be individually dispatched, as demand of 2 was still there
    readerProbe.expectNext(Dummy(Set("e")))
    readerProbe.expectNext(Dummy(Set("f")))

    // The last set of two should again be merged, as there was no demand
    readerProbe.request(2)
    readerProbe.expectNext(Dummy(Set("g", "h")))
  }

  def withStringMap(initialState: Map[String, AnyRef] = Map.empty[String, AnyRef])
                   (test: InMemMonotonicMap[String] ⇒ Any): Unit = {

    // Instantiate the implementing actor, and wrap a map around it
    val actor = watch(system.actorOf(InMemMonotonicMapActor.props(initialState)))
    val monotonicMap = new InMemMonotonicMap[String](actor)
    // execute the test
    test(monotonicMap)
    // We don't expect any messages from the watch (it would be `Terminated`)
    Try(expectNoMsg(remainingOrDefault)).failed.foreach(
      fail("Deathwatch of InMemMonotonicMapActor was triggered by message", _)
    )
  }
}
