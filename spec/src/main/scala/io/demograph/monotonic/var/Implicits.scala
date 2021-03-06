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

package io.demograph.monotonic.`var`
import algebra.lattice.BoundedJoinSemilattice
/**
 *
 */
trait Implicits {

  /**
   * An MVar, with an ExecutionContext in scope, permits the creation of graph stages (such as `map` and `product`)
   */
  implicit class MVarExecutionContext[A: BoundedJoinSemilattice](mvar: MVar[A])(implicit ec: ExecutionContext) extends MVarOps[A] {

    override def map[T: BoundedJoinSemilattice](f: (A) ⇒ T): MVar[T] = ec.map(mvar)(f)

    override def product[T: BoundedJoinSemilattice](mvarT: MVar[T]): MVar[(A, T)] = ec.product(mvar)(mvarT)
  }
}
