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

package com.github.mboogerd.crdt.delta.causal

import com.github.mboogerd.crdt.delta.dot.{ Dot, Dots }
/**
 *
 */

trait CausalHistory[I] {
  type Repr <: CausalHistory[I]
  def max(i: I): Int
  def maxDot(i: I): Dot[I]
  def next(i: I): Int
  def nextDot(i: I): Dot[I]
  def dots: Dots[I]
}

object CausalContext {
  def empty[I]: CausalContext[I] = CausalContext(Dots.empty)
}
case class CausalContext[I](dots: Dots[I]) extends CausalHistory[I] {
  override type Repr = CausalContext[I]

  override def max(i: I): Int = maxDot(i).version

  override def maxDot(i: I): Dot[I] = dots.filter(_.replica == i) match {
    case s if s.isEmpty ⇒ Dot(i)
    case s ⇒ s.maxBy(_.version)
  }

  override def next(i: I): Int = max(i) + 1

  override def nextDot(i: I): Dot[I] = Dot(i, next(i))
}
