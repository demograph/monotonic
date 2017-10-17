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

package io.demograph.crdt.delta.dot

import io.demograph.crdt.delta.causal.CausalContext
import simulacrum.typeclass

/**
 * Instance of `Dotted` prove that X is dot-based, i.e. it is either a Dot, or a product or co-product of Dot, recursively.
 *
 * Note that we choose a typeclass implementation, rather than an inheritance one. The latter would have been possible,
 * but would require re-implementation of product and co-product such that it would inherit some new upper `Dot` type.
 * Ad-hoc polymorphism seemed to be the shorter path here.
 */
// $COVERAGE-OFF$ Generated typeclasses do not require test-coverage
@typeclass trait Dotted[X] {

  /**
   * @return an empty `CausalContext` for the given (dot-based) `X`
   */
  def causalContext: CausalContext[X]
}
// $COVERAGE-ON$