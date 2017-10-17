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

import _root_.algebra.lattice.GenBool
import algebra.BoundedSemilattice
import cats.instances.list._
import cats.instances.map._
import cats.syntax.foldable._
import cats.syntax.monoid._
import io.demograph.crdt.delta.dot.CompactDotSet._
import spire.math.extras.interval.IntervalTrie
import spire.math.extras.interval.IntervalTrie._

import scala.collection.{ GenSet, GenTraversableOnce, SetLike, immutable }

/**
 * This is an implementation of a lax version vector. A normal version vector has the constraint that it can only
 * represent causally-consistent histories, by storing the maximum version observed per host, with the semantics that
 * all eventIDs up to this maximum version have been observed. `CompactDotSet` generalizes this by allowing gaps, while
 * retaining optimal efficiency for the causally consistent case, and good efficiency for near-causally-consistent
 * histories. Note that `CompactDotSet` is optimized for replicas performing _multiple_ updates. If only one update is
 * performed on each replica, performance will be close to that of a `Set` of `Dot`s (in fact, slightly worse because
 * of the overhead of Map and IntervalTrie, but this should be negligible)
 */
object CompactDotSet {
  private[dot] implicit val bslIntervalTrie: BoundedSemilattice[IntervalTrie[Int]] = algebra[Int].joinSemilattice
  private val MIN_VERSION = 0

  def empty[H]: CompactDotSet[H] = new CompactDotSet[H](Map.empty)

  def apply[H](host: H, version: Int): CompactDotSet[H] = new CompactDotSet(Map(host → (above(MIN_VERSION) & below(version))))

  def apply[H](dots: Dot[H]*): CompactDotSet[H] = {
    val replicaDots = dots.groupBy(_.replica)
      .mapValues(_.toList.map(_.version).filter(_ >= MIN_VERSION))
      .mapValues(_.map(point[Int]).combineAll)

    new CompactDotSet(replicaDots)
  }

  // TODO: Should be moved elsewhere
  implicit def genBoolMap[K, V: GenBool]: GenBool[Map[K, V]] = new GenBool[Map[K, V]] {
    private final val genBoolV = GenBool[V]
    private implicit val semilatticeV: BoundedSemilattice[V] = genBoolV.joinSemilattice

    override def and(a: Map[K, V], b: Map[K, V]): Map[K, V] = a.keySet.intersect(b.keySet).foldLeft(Map.empty[K, V]) {
      case (m, k) ⇒ m + (k → genBoolV.and(a(k), b(k)))
    }

    override def or(a: Map[K, V], b: Map[K, V]): Map[K, V] = a |+| b

    override def without(a: Map[K, V], b: Map[K, V]): Map[K, V] = b.foldLeft(a) {
      case (m, (k, bv)) ⇒ m.get(k) match {
        case None ⇒ m
        case Some(av) ⇒ m.updated(k, genBoolV.without(av, bv))
      }
    }

    override def zero: Map[K, V] = Map.empty
  }

  implicit def genBoolCompactDotSet[H]: GenBool[CompactDotSet[H]] = new GenBool[CompactDotSet[H]] {
    override def or(a: CompactDotSet[H], b: CompactDotSet[H]): CompactDotSet[H] = new CompactDotSet[H](GenBool.or(a.elems, b.elems))

    override def without(a: CompactDotSet[H], b: CompactDotSet[H]): CompactDotSet[H] = new CompactDotSet[H](GenBool.without(a.elems, b.elems))

    override def and(a: CompactDotSet[H], b: CompactDotSet[H]): CompactDotSet[H] = new CompactDotSet[H](GenBool.and(a.elems, b.elems))

    override def zero: CompactDotSet[H] = new CompactDotSet[H](Map.empty)
  }
}

/**
 * @param elems The mapping of replica's to versions observed (by the current host)
 * @tparam H Host type
 */
final case class CompactDotSet[H] private (elems: Map[H, IntervalTrie[Int]]) extends Set[Dot[H]] with SetLike[Dot[H], CompactDotSet[H]] {

  override def empty: CompactDotSet[H] = CompactDotSet()

  override def contains(elem: Dot[H]): Boolean = elems.get(elem.replica).exists(_.at(elem.version))

  override def +(elem: Dot[H]): CompactDotSet[H] = new CompactDotSet[H](elems |+| Map(elem.replica → point(elem.version)))

  override def ++(xs: GenTraversableOnce[Dot[H]]): CompactDotSet[H] = xs match {
    case that @ CompactDotSet(_) ⇒ GenBool[CompactDotSet[H]].or(this, that)
    case _ ⇒ super.++(xs)
  }

  override def -(elem: Dot[H]): CompactDotSet[H] = this -- CompactDotSet(elem)

  override def --(xs: GenTraversableOnce[Dot[H]]): CompactDotSet[H] = xs match {
    case that @ CompactDotSet(_) ⇒ GenBool[CompactDotSet[H]].without(this, that)
    case _ ⇒ super.--(xs)
  }

  override def intersect(xs: GenSet[Dot[H]]): CompactDotSet[H] = xs match {
    case that @ CompactDotSet(_) ⇒ GenBool[CompactDotSet[H]].and(this, that)
    case _ ⇒ super.--(xs)
  }

  override lazy val toIterable: immutable.Iterable[Dot[H]] = for {
    (replica, versions) ← elems
    version ← versions.edges
  } yield Dot(replica, version)

  override def iterator: Iterator[Dot[H]] = toIterable.toIterator

  override lazy val size: Int = iterator.length
}