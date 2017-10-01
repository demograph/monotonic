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

import algebra.lattice.JoinSemilattice

/**
 * A Read-Write MVar. The Updatable interface is exposed to client code.
 */
class WritableMVar[S: JoinSemilattice](initialValue: S) extends AtomicMVar[S](initialValue) with Updatable[S] {
  /**
   *
   * @param s the value to be interpreted as an update
   */
  override def update(s: S): Unit = onUpdate(s)
}
