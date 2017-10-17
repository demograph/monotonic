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

package io.demograph.crdt.delta.causal

import algebra.instances.int._
import io.demograph.crdt.Session
import io.demograph.crdt.delta.dot.{ CompactDotSet, Dot, Dotted }

import scala.collection.{ GenSet, GenTraversableOnce, SetLike }

/**
 *
 */
sealed trait CausalContext[E] extends Set[E] with SetLike[E, CausalContext[E]] {
  self ⇒
  override def empty: CausalContext[E] = ??? // There is no such thing as a concrete empty `CausalContext`, but subclasses should define it
}

object CausalContext {
  def empty[E: Dotted]: CausalContext[E] = Dotted[E].causalContext

  def apply[H: Session](dots: Dot[H]*): CausalContext[Dot[H]] = DotCC(CompactDotSet(dots))

  def from[H: Session](dots: Set[Dot[H]]): CausalContext[Dot[H]] = DotCC(CompactDotSet(dots))
}

/**
 * The default causal context implementation, which wraps a CompactDotSet for efficiency.
 */
case class DotCC[H](wrapped: CompactDotSet[H]) extends CausalContext[Dot[H]] {

  override def contains(elem: Dot[H]): Boolean = wrapped.contains(elem)

  override def +(elem: Dot[H]): CausalContext[Dot[H]] = DotCC(wrapped + elem)

  override def ++(elems: GenTraversableOnce[Dot[H]]): CausalContext[Dot[H]] = DotCC(wrapped ++ elems)

  override def -(elem: Dot[H]): CausalContext[Dot[H]] = DotCC(wrapped - elem)

  override def --(xs: GenTraversableOnce[Dot[H]]): CausalContext[Dot[H]] = DotCC(wrapped -- xs)

  override def intersect(that: GenSet[Dot[H]]): CausalContext[Dot[H]] = DotCC(wrapped.intersect(that))

  override def iterator: Iterator[Dot[H]] = wrapped.iterator

  override def seq: Set[Dot[H]] = wrapped.seq

  override def empty: CausalContext[Dot[H]] = DotCC(CompactDotSet.empty)

  def nextDot(host: H): Dot[H] = Dot(host, wrapped.elems.get(host).flatMap(_.hull.top(0)).getOrElse(0))
}

/**
 * We take the product of causal contexts when performing the product/intersection of two causal CRDTs.
 * This allows us to retain the efficient representation of `CompactDotSet`/`DotCC` and without having to store the
 * actual product in memory.
 */
case class ProCC[L, R](ls: Set[L], rs: Set[R]) extends CausalContext[(L, R)] {
  override def contains(elem: (L, R)): Boolean = ls.contains(elem._1) && rs.contains(elem._2)

  override def +(elem: (L, R)): ProCC[L, R] = ProCC(ls + elem._1, rs + elem._2)

  override def -(elem: (L, R)): ProCC[L, R] = ProCC(ls - elem._1, rs - elem._2)

  override def iterator: Iterator[(L, R)] = for {
    l ← ls.iterator
    r ← rs.iterator
  } yield (l, r)

  override def empty: CausalContext[(L, R)] = ProCC(Set.empty, Set.empty)
}

/**
 * We take the co-product of causal contexts when performing the union of two causal CRDTs
 * This allows us to retain the efficient representation of `CompactDotSet`/`DotCC` and without having to store the
 * actual sum in memory (separately).
 */
case class CopCC[L, R](ls: Set[L], rs: Set[R]) extends CausalContext[Either[L, R]] {
  override def contains(elem: Either[L, R]): Boolean = elem match {
    case Left(l) ⇒ ls.contains(l)
    case Right(r) ⇒ rs.contains(r)
  }

  override def +(elem: Either[L, R]): CopCC[L, R] = elem match {
    case Left(l) ⇒ copy(ls + l)
    case Right(r) ⇒ copy(rs = rs + r)
  }

  override def -(elem: Either[L, R]): CopCC[L, R] = elem match {
    case Left(l) ⇒ copy(ls - l)
    case Right(r) ⇒ copy(rs = rs - r)
  }

  override def iterator: Iterator[Either[L, R]] = ls.iterator.map(Left(_)) ++ rs.iterator.map(Right(_))

  override def empty: CausalContext[Either[L, R]] = CopCC(Set.empty, Set.empty)
}