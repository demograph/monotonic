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

package com.github.mboogerd.crdt

import com.github.mboogerd.crdt.util.ScalaTestImplicits
import org.scalatest.concurrent.{ Eventually, ScalaFutures }
import org.scalatest.enablers.Containing
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{ FlatSpecLike, Matchers, OptionValues }

import scala.collection.JavaConverters._

/**
 *
 */
trait TestSpec extends FlatSpecLike with Matchers with GeneratorDrivenPropertyChecks with Eventually with ScalaFutures
  with OptionValues with ScalaTestImplicits {

  implicit def containingIterator[T]: Containing[java.lang.Iterable[T]] = new Containing[java.lang.Iterable[T]] {
    override def contains(container: java.lang.Iterable[T], element: Any): Boolean =
      container.asScala.toIterator.contains(element)

    override def containsOneOf(container: java.lang.Iterable[T], elements: Seq[Any]): Boolean =
      container.asScala.toSet.intersect(elements.toSet).size == 1 // <- Does not exit early once |intersection| > 1 is established

    override def containsNoneOf(container: java.lang.Iterable[T], elements: Seq[Any]): Boolean =
      container.asScala.toIterator.collectFirst { case elem if elements.contains(elem) => true }.isEmpty
  }
}
