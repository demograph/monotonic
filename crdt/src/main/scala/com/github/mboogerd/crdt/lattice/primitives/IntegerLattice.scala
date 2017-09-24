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

package com.github.mboogerd.crdt.lattice.primitives

import algebra.lattice.BoundedJoinSemilattice
import cats.kernel.Order
import com.github.mboogerd.crdt.lattice.Chain
import cats.kernel.instances.IntOrder
/**
 *
 */
object IntegerLattice {

  object Ascending {
    implicit object AscIntegerLattice extends IntOrder with Chain[Int] with BoundedJoinSemilattice[Int] {
      override def join(lhs: Int, rhs: Int): Int = Order[Int].max(lhs, rhs)
      override def zero: Int = Int.MinValue
    }
  }

  object Descending {
    implicit object DescIntegerLattice extends IntOrder with Chain[Int] with BoundedJoinSemilattice[Int] {
      override def join(lhs: Int, rhs: Int): Int = math.min(lhs, rhs)
      override def zero: Int = Int.MaxValue
      override def compare(x: Int, y: Int): Int = super.compare(x, y) * -1
    }
  }
}
