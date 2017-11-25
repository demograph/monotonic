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

package io.demograph.monotonic.queue

import cats.instances.int._
import io.demograph.monotonic.TestBase
import io.demograph.monotonic.queue.OverflowStrategies._
/**
 *
 */
class MergingQueueSpec extends TestBase {

  behavior of "MergingQueue"

  it should "respect the MergeHead overflowStrategy" in {
    MergingQueue(MergeHead, 4, Vector(1, 2, 3, 4)).enqueue(5).vector shouldBe Vector(3, 3, 4, 5)
    MergingQueue(MergeHead, 5, Vector(1, 2, 3, 4, 5)).enqueue(6).vector shouldBe Vector(3, 3, 4, 5, 6)
  }

  it should "respect the MergePairs overflowStrategy" in {
    MergingQueue(MergePairs, 4, Vector(1, 2, 3, 4)).enqueue(5).vector shouldBe Vector(3, 7, 5)
    MergingQueue(MergePairs, 5, Vector(1, 2, 3, 4, 5)).enqueue(6).vector shouldBe Vector(3, 7, 5, 6)
  }

  it should "respect the MergeAll overflowStrategy" in {
    MergingQueue(MergeAll, 4, Vector(1, 2, 3, 4)).enqueue(5).vector shouldBe Vector(10, 5)
    MergingQueue(MergeAll, 5, Vector(1, 2, 3, 4, 5)).enqueue(6).vector shouldBe Vector(15, 6)
  }
}
