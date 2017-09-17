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

package com.github.mboogerd.mmap.mvar

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import algebra.lattice.JoinSemilattice

/**
 *
 */
case class MapMVar[S: JoinSemilattice, T: JoinSemilattice](source: MVar[S], f: S ⇒ T, initialValue: T)(implicit mat: Materializer) extends AtomicMVar[T](initialValue) {

  val _ = Source.fromPublisher(source.publisher)
    .map(f)
    .runForeach(onUpdate)
}
