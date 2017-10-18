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

import cats.functor.Contravariant
import io.demograph.crdt.delta.causal.CausalCRDT
import io.demograph.crdt.delta.dot.{ CompactDotSet, Dot, DotStore }
import io.demograph.crdt.delta.map.ORMap
import io.demograph.crdt.delta.set.AWSet
import org.scalatest.enablers.Aggregating.aggregatingNatureOfGenTraversable
import org.scalatest.enablers.{ Aggregating, Containing }

import scala.collection.GenTraversable
import cats.syntax.contravariant._
/**
 *
 */
trait ScalaTestImplicits {

  implicit def awSetContaining[I, E]: Containing[AWSet[I, E]] = new Containing[AWSet[I, E]] {
    override def contains(container: AWSet[I, E], element: Any): Boolean =
      container.contains(element)

    override def containsOneOf(container: AWSet[I, E], elements: Seq[Any]): Boolean =
      elements.count(container.contains) == 1

    override def containsNoneOf(container: AWSet[I, E], elements: Seq[Any]): Boolean =
      !elements.exists(container.contains)
  }

  implicit def orMapContaining[I, K, V, C](implicit ds: DotStore[V, I], causal: CausalCRDT[I, V, C]): Containing[ORMap[I, K, V, C]] =
    new Containing[ORMap[I, K, V, C]] {
      override def contains(container: ORMap[I, K, V, C], element: Any): Boolean = element match {
        case k: K @unchecked ⇒ container.contains(k)
        case v: V @unchecked ⇒ container.dotStore.dotMap.valuesIterator.contains(v)
        case kv: (K, V) @unchecked ⇒ container.dotStore.dotMap.exists(_ == kv)
        case _ ⇒ false
      }

      override def containsOneOf(container: ORMap[I, K, V, C], elements: Seq[Any]): Boolean =
        elements.count(e ⇒ contains(container, e)) == 1

      override def containsNoneOf(container: ORMap[I, K, V, C], elements: Seq[Any]): Boolean =
        elements.count(e ⇒ contains(container, e)) == 0
    }

  implicit val covariantAggregating: Contravariant[Aggregating] = new Contravariant[Aggregating] {
    override def contramap[A, B](fa: Aggregating[A])(f: (B) ⇒ A): Aggregating[B] = new Aggregating[B] {
      override def containsAllOf(aggregation: B, eles: Seq[Any]): Boolean = fa.containsAllOf(f(aggregation), eles)

      override def containsAtMostOneOf(aggregation: B, eles: Seq[Any]): Boolean = fa.containsAtMostOneOf(f(aggregation), eles)

      override def containsAtLeastOneOf(aggregation: B, eles: Seq[Any]): Boolean = fa.containsAtLeastOneOf(f(aggregation), eles)

      override def containsOnly(aggregation: B, eles: Seq[Any]): Boolean = fa.containsAtLeastOneOf(f(aggregation), eles)

      override def containsTheSameElementsAs(leftAggregation: B, rightAggregation: GenTraversable[Any]): Boolean =
        fa.containsTheSameElementsAs(f(leftAggregation), rightAggregation)
    }
  }

  implicit def aggrCDS[T]: Aggregating[CompactDotSet[T]] =
    aggregatingNatureOfGenTraversable[Dot[T], Traversable].contramap[CompactDotSet[T]](_.toTraversable)

}
