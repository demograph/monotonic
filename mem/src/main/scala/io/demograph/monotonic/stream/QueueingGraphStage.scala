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

import akka.event.Logging
import akka.stream._
import akka.stream.stage.{ GraphStageLogic, GraphStageWithMaterializedValue, InHandler, OutHandler }
import io.demograph.monotonic.queue.{ Queue, QueueConsumer }

/**
 * This is mostly a copy of Akka 2.5.6 `akka.streams.impl.fusing.Ops.Buffer` with the exception that on
 * `preStart`, it initializes a `Queue` inside a `AtomicReference`, allowing concurrent access to the queues' elements
 */
class QueueingGraphStage[A](size: Int, queueFactory: Int ⇒ Queue[A]) extends GraphStageWithMaterializedValue[FlowShape[A, A], AtomicReference[QueueConsumer[A]]] {

  val in: Inlet[A] = Inlet[A](Logging.simpleName(this) + ".in")
  val out: Outlet[A] = Outlet[A](Logging.simpleName(this) + ".out")
  override val shape: FlowShape[A, A] = FlowShape(in, out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, AtomicReference[QueueConsumer[A]]) = {
    // The mutable state for this GraphStage is materialized outside the GraphStageLogic! Note however that in this
    // case it is an immutable datastructure inside a concurrency-safe `AtomicReference`
    val buffer: AtomicReference[Queue[A]] = new AtomicReference[Queue[A]](queueFactory(size))

    def updateBuffer(f: Queue[A] ⇒ Queue[A]): Queue[A] = {
      buffer.getAndUpdate(new UnaryOperator[Queue[A]] {
        override def apply(t: Queue[A]): Queue[A] = f(t)
      })
    }

    val logic = new GraphStageLogic(shape) with InHandler with OutHandler {

      override def preStart(): Unit = pull(in)

      override def onPush(): Unit = {
        val elem = grab(in)
        // If out is available, then it has been pulled but no dequeued element has been delivered.
        // It means the buffer at this moment is definitely empty,
        // so we just push the current element to out, then pull.
        if (isAvailable(out)) {
          push(out, elem)
          pull(in)
        } else {
          updateBuffer(_.enqueue(elem))
          pull(in)
        }
      }

      override def onPull(): Unit = {
        // Update the internal buffer
        val old: Queue[A] = updateBuffer { b ⇒ if (b.nonEmpty) b.dropHead() else b }
        // Push out the head element, if one existed
        if (old.nonEmpty) push(out, old.dequeue())

        if (isClosed(in)) {
          // if its size is 1, it means that element is just pushed from this immutable buffer
          if (old.size <= 1) completeStage()
        } else if (!hasBeenPulled(in)) {
          pull(in)
        }
      }

      override def onUpstreamFinish(): Unit = {
        if (buffer.get.isEmpty) completeStage()
      }

      setHandlers(in, out, this)
    }

    // Return the GraphStageLogic and materialized (concrrent) buffer
    (logic, buffer.asInstanceOf[AtomicReference[QueueConsumer[A]]])
  }
}