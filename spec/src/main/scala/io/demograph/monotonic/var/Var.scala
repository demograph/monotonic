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

import org.reactivestreams.Publisher

/**
 *
 */
trait Var[A] {

  /**
   * @return A snapshot of the current state of this variable
   */
  def sample: A

  /**
   * Allows overwriting the current value of the `Var`
   *
   * @param value The value to make current
   * @return The previous state of this `Var`
   */
  protected[`var`] def _set(value: A): A

  /**
   * Allows updating the current value of the `Var` by applying the supplied function to it
   *
   * @param f The function to apply
   * @return The previous state of this `Var`
   */
  protected[`var`] def _update(f: A ⇒ A): A

  /**
   * Exposes the reactive-streams Publisher interface so that derived MVars can subscribe. This Publisher will send
   * the state of this MVar, and all future evolutions, to the Subscriber. Dependent on the type of `S`, this may be
   * a stream of deltas or a stream of full states.
   */
  def publisher: Publisher[A]
}
