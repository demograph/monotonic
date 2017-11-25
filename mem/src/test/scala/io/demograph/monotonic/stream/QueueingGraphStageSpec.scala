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

package io.demograph.monotonic.stream

import java.util.concurrent.atomic.AtomicReference
import java.util.function.UnaryOperator

import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import akka.stream.testkit.{ TestPublisher, TestSubscriber }
import io.demograph.monotonic.ActorTestBase
import io.demograph.monotonic.queue.OverflowStrategies.{ DropHead, OverflowStrategy, PurgingOverflowStrategy }
import io.demograph.monotonic.queue.{ PurgingQueue, QueueConsumer }
import org.scalatest.concurrent.Eventually

import scala.concurrent.duration._

/**
 *
 */
class QueueingGraphStageSpec extends ActorTestBase with Eventually {

  behavior of "QueuingGraphStage"

  it should "queue elements" in {
    val (sourceProbe, _, sinkProbe) = bufferedStream[Int](DropHead, capacity = 2)

    withClue("Queueing demand is independent of downstream") {
      sourceProbe.expectRequest()
      sourceProbe.sendNext(1)
      sourceProbe.sendNext(2)

      sinkProbe.expectNoMessage(100.millis)
    }

    withClue("Once downstream signals demand, elements are delivered in order") {
      sinkProbe.request(2)
      sinkProbe.expectNext(1)
      sinkProbe.expectNext(2)
    }

    withClue("Once downstream stops demand, buffering continues") {
      sourceProbe.sendNext(3)
      sinkProbe.expectNoMessage(100.millis)
    }
  }

  it should "drop the head of the queue when required" in {
    val (sourceProbe, _, sinkProbe) = bufferedStream[Int](DropHead)

    sourceProbe.sendNext(1)
    sourceProbe.sendNext(2)
    sourceProbe.expectNoMessage(100.millis)

    withClue("1 was popped out because of pushing 2 while queue-capacity is just 1") {
      sinkProbe.request(1)
      sinkProbe.expectNext(2)
    }
  }

  it should "allow concurrent peeking into to the buffer" in {
    val (sourceProbe, buffer, sinkProbe) = bufferedStream[Int](DropHead, capacity = 2)

    sourceProbe.sendNext(1)
    sourceProbe.sendNext(2)
    eventually(buffer.get.peek(2) shouldBe Seq(1, 2))
    sinkProbe.request(2)
    sinkProbe.expectNext(1)
    sinkProbe.expectNext(2)
  }

  it should "not produce concurrently dequeued elements downstream" in {
    val (sourceProbe, buffer, sinkProbe) = bufferedStream[Int](DropHead, capacity = 2)

    sourceProbe.sendNext(1)
    sourceProbe.sendNext(2)
    eventually(buffer.get.size shouldBe 2)

    buffer.getAndUpdate(new UnaryOperator[QueueConsumer[Int]] {
      override def apply(t: QueueConsumer[Int]): QueueConsumer[Int] = t.dropHead()
    }).dequeue() shouldBe 1

    sinkProbe.request(2)
    sinkProbe.expectNext(2)
    sinkProbe.expectNoMessage(100.millis)
  }

  it should "not dequeue elements that are produced downstream" in {
    val (sourceProbe, buffer, sinkProbe) = bufferedStream[Int](DropHead, capacity = 2)

    sourceProbe.sendNext(1)
    sourceProbe.sendNext(2)
    sinkProbe.request(1)
    sinkProbe.expectNext(1)
    eventually(buffer.get.dequeue() shouldBe 2)
  }

  def bufferFlow[A](overflowStrategy: OverflowStrategy, capacity: Int = 1): QueueingGraphStage[A] = {
    overflowStrategy match {
      case s: PurgingOverflowStrategy ⇒ new QueueingGraphStage[A](capacity, _ ⇒ new PurgingQueue[A](s, capacity, Vector.empty))
      //      case s: MergingOverflowStrategy ⇒ new QueueingGraphStage[A](capacity, _ ⇒ new MergingQueue[A](s, capacity, Vector.empty))
      case other ⇒ fail(s"Unsupported overflow-strategy $other")
    }
  }

  def bufferedStream[A](overflowStrategy: OverflowStrategy, capacity: Int = 1): (TestPublisher.Probe[A], AtomicReference[QueueConsumer[A]], TestSubscriber.Probe[A]) = {
    val ((sourceProbe, buffer), sinkProbe) = TestSource.probe[A](system)
      .viaMat(bufferFlow[A](overflowStrategy, capacity))(Keep.both)
      .toMat(TestSink.probe)(Keep.both)
      .run()

    sourceProbe.ensureSubscription()
    sinkProbe.ensureSubscription()

    (sourceProbe, buffer, sinkProbe)
  }
}
