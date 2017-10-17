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
import org.scalatest.enablers.{ Aggregating, Containing, Size }

import scala.collection.GenTraversable
/**
 *
 */
trait ScalaTestImplicits {

  implicit val contravariantSize: Contravariant[Size] = new Contravariant[Size] {
    override def contramap[A, B](fa: Size[A])(f: (B) ⇒ A): Size[B] = (obj: B) => fa.sizeOf(f(obj))
  }

  implicit val contravariantContaining: Contravariant[Containing] = new Contravariant[Containing] {
    override def contramap[A, B](fa: Containing[A])(f: (B) ⇒ A): Containing[B] = new Containing[B] {
      override def containsOneOf(container: B, elements: Seq[Any]): Boolean = fa.containsOneOf(f(container), elements)

      override def containsNoneOf(container: B, elements: Seq[Any]): Boolean = fa.containsNoneOf(f(container), elements)

      override def contains(container: B, element: Any): Boolean = fa.contains(f(container), element)
    }
  }

  implicit val contravariantAggregating: Contravariant[Aggregating] = new Contravariant[Aggregating] {
    override def contramap[A, B](fa: Aggregating[A])(f: (B) ⇒ A): Aggregating[B] = new Aggregating[B] {
      override def containsAllOf(aggregation: B, eles: Seq[Any]): Boolean = fa.containsAllOf(f(aggregation), eles)

      override def containsAtMostOneOf(aggregation: B, eles: Seq[Any]): Boolean = fa.containsAtMostOneOf(f(aggregation), eles)

      override def containsAtLeastOneOf(aggregation: B, eles: Seq[Any]): Boolean = fa.containsAtLeastOneOf(f(aggregation), eles)

      override def containsOnly(aggregation: B, eles: Seq[Any]): Boolean = fa.containsAtLeastOneOf(f(aggregation), eles)

      override def containsTheSameElementsAs(leftAggregation: B, rightAggregation: GenTraversable[Any]): Boolean =
        fa.containsTheSameElementsAs(f(leftAggregation), rightAggregation)
    }
  }
}