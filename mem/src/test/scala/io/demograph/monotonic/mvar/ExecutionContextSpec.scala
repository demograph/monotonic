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

import io.demograph.monotonic.ActorTestBase
import algebra.instances.set._
import algebra.instances.int.IntMinMaxLattice
import algebra.instances.long.LongMinMaxLattice
import algebra.lattice.BoundedDistributiveLattice
import ExecutionContextSpec._
import org.scalatest.concurrent.IntegrationPatience
/**
 *
 */
class ExecutionContextSpec extends ActorTestBase with IntegrationPatience {

  behavior of "ExecutionContext"

  implicit val ec: InMemExecutionContext = new InMemExecutionContext()

  it should "create MVars initialized at lattice-bottom" in {
    val intSet: UpdatableMVar[Set[Int]] = ec.mvar[Set[Int]]

    intSet.sample shouldBe Set.empty
  }

  it should "create MVars initialized at the given value" in {
    val intSet: UpdatableMVar[Set[Int]] = ec.mvar(Set(1, 2, 3))

    intSet.sample shouldBe Set(1, 2, 3)
  }

  it should "allow updates of MVars" in {
    val intSet: UpdatableMVar[Set[Int]] = ec.mvar[Set[Int]]

    intSet.update(Set(1))
    eventually(intSet.sample should contain only 1)

    intSet.update(Set(2))
    eventually(intSet.sample should contain theSameElementsAs Set(1, 2))
  }

  it should "allow functorial map of MVars" in {
    val intSet: UpdatableMVar[Set[Int]] = ec.mvar(Set(1, 2, 3))
    val setWithout4 = intSet.map(_ - 4)

    eventually(setWithout4.sample shouldBe Set(1, 2, 3))

    intSet.update(Set(4))
    intSet.update(Set(5))

    eventually(setWithout4.sample shouldBe Set(1, 2, 3, 5))
  }

  it should "allow zipping two MVars into their product" in {
    val intSet: UpdatableMVar[Set[Int]] = ec.mvar(Set(1))
    val longSet: UpdatableMVar[Set[Long]] = ec.mvar(Set(1L))

    val product = intSet.product(longSet)
    eventually(product.sample shouldBe (Set(1), Set(1L)))

    intSet.update(Set(2))
    eventually(product.sample shouldBe (Set(1, 2), Set(1L)))

    longSet.update(Set(2L))
    eventually(product.sample shouldBe (Set(1, 2), Set(1L, 2L)))
  }
}

object ExecutionContextSpec {
  implicit val intLattice: BoundedDistributiveLattice[Int] = IntMinMaxLattice
  implicit val longLattice: BoundedDistributiveLattice[Long] = LongMinMaxLattice
}