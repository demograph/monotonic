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

package com.github.mboogerd.monotonic.mvar

/**
 * An MVar can be digestable. That is, we can compute a bit of summary info from an instance using `digest` such that
 * when we `diff` with that digest, we get the bottom value for the lattice, or a(nother) delta if the MVar was updated
 * in the meantime
 */
trait Digestable[F[_], D] {

  def digest: D
  def diff[T](digest: D): F[T]

}
