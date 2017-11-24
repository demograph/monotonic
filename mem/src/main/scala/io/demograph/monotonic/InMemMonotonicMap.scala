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

package io.demograph.monotonic

import java.util.concurrent.atomic.AtomicReference
import java.util.function.UnaryOperator

import akka.actor.ActorRefFactory
import algebra.lattice.BoundedJoinSemilattice
import io.demograph.monotonic.`var`.{ ExecutionContext, UpdatableMVar }

import scala.reflect.runtime.universe._
/**
 * Constructs an in-memory monotonic map
 * @tparam K The key type
 */
class InMemMonotonicMap[K](implicit ec: ExecutionContext) extends MonotonicMap[K] {

  private[monotonic] val map: AtomicReference[Map[K, Map[TypeTag[_], UpdatableMVar[_]]]] = new AtomicReference(Map.empty)

  /**
   * Retrieves the monotonic variable that supposedly is stored under `Key`. If the key does not exist, it will
   * instantiate a new variable with initialValue, and register it for the given key
   */
  override def get[V: BoundedJoinSemilattice: TypeTag](key: K, initialValue: V): UpdatableMVar[V] = {
    map.get().get(key)
      .flatMap(_.get(implicitly[TypeTag[V]]).map(_.asInstanceOf[UpdatableMVar[V]]))
      .getOrElse {
        val mvar = ec.mvar(initialValue)
        put(key, mvar)
        mvar
      }
  }

  /**
   * Binds the given mvar to the given key.
   */
  override def put[V: BoundedJoinSemilattice: TypeTag](key: K, mvar: UpdatableMVar[V]): Unit = {
    val tpe = implicitly[TypeTag[V]]

    map.getAndUpdate {
      new UnaryOperator[Map[K, Map[TypeTag[_], UpdatableMVar[_]]]] {
        override def apply(t: Map[K, Map[TypeTag[_], UpdatableMVar[_]]]): Map[K, Map[TypeTag[_], UpdatableMVar[_]]] = {
          val typeTaggedMap: Map[TypeTag[_], UpdatableMVar[_]] = t.get(key) match {
            case Some(inner) ⇒
              inner.get(tpe) match {
                case Some(mvar2) ⇒ inner + (tpe → mvar)
                // TODO: This case describes concurrent assignment of an instance of the same variable type to the same key
                // The correct way of dealing with it is to join both UpdatableMVars. Note that this may often not be desirable
                // For example, if one were to assign an intersection and a union of two sets to the same key, the
                // net effect would be that the intersection variable also becomes a union. However, we cannot prevent
                // concurrent key assignments in general, so throwing/failing seems to be no option.
                // For now, we accept not implementing this merge as a known bug... sorry!
                case None ⇒ inner + (tpe → mvar)
              }
            case None ⇒ Map(tpe → mvar)
          }
          t.updated(key, typeTaggedMap)
        }
      }
    }
  }
}

object InMemMonotonicMap {
  def apply[K](implicit ec: ExecutionContext): InMemMonotonicMap[K] = new InMemMonotonicMap
}
