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

package io.demograph.monotonic.mvar

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import algebra.lattice.{ JoinSemilattice, BoundedJoinSemilattice ⇒ BJSL }
import ProductMVar._
/**
 *
 */
class ProductMVar[S: BJSL, T: BJSL](mvarS: MVar[S], mvarT: MVar[T])(implicit mat: Materializer) extends AtomicMVar[(S, T)]((BJSL[S].zero, BJSL[T].zero)) {

  private val srcS = Source.fromPublisher(mvarS.publisher).map(liftL[S, T])
  private val srcT = Source.fromPublisher(mvarT.publisher).map(liftR[S, T])

  srcS.merge(srcT).runForeach(onUpdate)
}

object ProductMVar {
  def liftL[A: BJSL, B: BJSL](a: A): (A, B) = (a, BJSL[B].zero)
  def liftR[A: BJSL, B: BJSL](b: B): (A, B) = (BJSL[A].zero, b)

  implicit def productLattice[A: JoinSemilattice, B: JoinSemilattice]: JoinSemilattice[(A, B)] =
    (lhs: (A, B), rhs: (A, B)) => (JoinSemilattice[A].join(lhs._1, rhs._1), JoinSemilattice[B].join(lhs._2, rhs._2))
}
