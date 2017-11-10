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

package io.demograph.monotonic

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.testkit.TestSubscriber
import akka.stream.testkit.TestSubscriber.Probe
import akka.stream.{ ActorMaterializer, Materializer }
import akka.testkit.TestKitBase
import algebra.lattice.JoinSemilattice
import com.typesafe.config.{ Config, ConfigFactory }
import org.reactivestreams.Publisher

/**
 *
 */
trait ActorTestBase extends TestKitBase with TestBase {

  lazy val stoppingConfigStr = """ akka.actor.guardian-supervisor-strategy = "akka.actor.StoppingSupervisorStrategy" """
  lazy val actorSystemConfig: Config = ConfigFactory.parseString(stoppingConfigStr)

  override implicit lazy val system: ActorSystem = ActorSystem("test", actorSystemConfig)
  implicit lazy val mat: Materializer = ActorMaterializer()

  override protected def afterAll(): Unit = {
    system.terminate()
    super.afterAll()
  }

  /* Helper methods */

  def subscribed[T](publisher: Publisher[T]): Probe[T] = {
    val probe = TestSubscriber.probe[T]()
    publisher.subscribe(probe)
    probe.ensureSubscription()
    probe
  }

  def bufferAll[T](publisher: Publisher[T]): Probe[T] = {
    val subscription = subscribed(publisher)
    subscription.request(Int.MaxValue)
  }

  def source[T](publisher: Publisher[T]): Source[T, NotUsed] =
    Source.fromPublisher(publisher)
}
