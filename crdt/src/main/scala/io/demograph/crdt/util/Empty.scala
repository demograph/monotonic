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

package io.demograph.crdt.util

import simulacrum.typeclass

/**
 * This could be an upper type for Monoid[T], it is intended to represent the same concept of emptiness, i.e. it should
 * satisfy the same laws, if it were to be used in conjunction with an `append`. However, we have a use case for
 * emptiness without needing `append`
 */
// $COVERAGE-OFF$ Generated typeclasses do not require test-coverage
@typeclass trait Empty[T] {
  def empty: T
}
// $COVERAGE-ON$