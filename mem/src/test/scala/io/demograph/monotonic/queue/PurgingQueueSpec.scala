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

import io.demograph.monotonic.TestBase
import io.demograph.monotonic.queue.OverflowStrategies.{ DropBuffer, DropHead, DropNew, DropTail }

/**
 *
 */
class PurgingQueueSpec extends TestBase {

  behavior of "PurgingQueue"

  it should "respect the DropHead overflowStrategy" in {
    PurgingQueue(DropHead, 3, Vector(1, 2, 3)).enqueue(4).vector shouldBe Vector(2, 3, 4)
  }

  it should "respect the DropTail overflowStrategy" in {
    PurgingQueue(DropTail, 3, Vector(1, 2, 3)).enqueue(4).vector shouldBe Vector(1, 4)
  }

  it should "respect the DropBuffer overflowStrategy" in {
    PurgingQueue(DropBuffer, 3, Vector(1, 2, 3)).enqueue(4).vector shouldBe Vector(4)
  }

  it should "respect the DropNew overflowStrategy" in {
    PurgingQueue(DropNew, 3, Vector(1, 2, 3)).enqueue(4).vector shouldBe Vector(1, 2, 3)
  }
}
