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

import algebra.instances.set._
import io.demograph.monotonic.mvar._

import scala.reflect.runtime.universe._
/**
 *
 */
class InMemMonotonicMapSpec extends ActorTestBase {

  behavior of "InMemMonotonicMap"

  implicit val ec: ExecutionContext = InMemExecutionContext()

  it should "return a new MVar if the key was not bound prior to retrieving it" in {
    val newMMap = mmap[String](Map.empty)
    val mvar: UpdatableMVar[Set[String]] = newMMap.get[Set[String]]("new-key")
    mvar.sample shouldBe 'empty

    withClue("The returned MVar should now be bound to 'new-key'") {
      mvar.update(Set("some-element"))
      val mvar2: UpdatableMVar[Set[String]] = newMMap.get[Set[String]]("new-key")
      mvar2.sample shouldBe Set("some-element")
    }
  }

  it should "return the MVar if one was previously assigned to a retrieved key" in {
    val newMMap = mmap[String](Map("existing-key" → Map(implicitly[TypeTag[Set[String]]] → ec.mvar[Set[String]](Set("some-value")))))
    val mvar: MVar[Set[String]] = newMMap.get[Set[String]]("existing-key")
    mvar.sample should contain only "some-value"
  }

  it should "support binding an MVar to a key, allowing later retrieval" in {
    val newMMap = mmap[String](Map.empty)
    val mvar = ec.mvar[Set[String]](Set("initial"))
    newMMap.put("key", mvar)

    val mvar2 = newMMap.get[Set[String]]("key")
    mvar2.sample shouldBe Set("initial")
  }

  it should "allow binding an MVar to multiple keys" in {
    val newMMap = mmap[String](Map.empty)
    val mvar = ec.mvar[Set[String]](Set.empty[String])
    newMMap.put("key1", mvar)
    newMMap.put("key2", mvar)

    val mvar1 = newMMap.get[Set[String]]("key1")
    val mvar2 = newMMap.get[Set[String]]("key2")

    mvar1.update(Set("shared-value"))
    eventually(mvar2.sample shouldBe Set("shared-value"))
  }

  it should "allow binding two MVars of different type to the same key" in {
    val newMMap = mmap[String](Map.empty)
    val mvarStringSet = ec.mvar[Set[String]](Set("1337"))
    val mvarIntSet = ec.mvar[Set[Int]](Set(1337))
    newMMap.put("key", mvarStringSet)
    newMMap.put("key", mvarIntSet)

    newMMap.get[Set[String]]("key").sample shouldBe Set("1337")
    newMMap.get[Set[Int]]("key").sample shouldBe Set(1337)
  }

  ignore should "support concurrent binding two MVars of the same type to the same key" in {
    // TODO: This we cannot currently support, it requires some redesign. We have to make sure that all MVar instances
    // pointing to the same key are merged into a single MVar.
  }

  def mmap[K](content: Map[K, Map[TypeTag[_], UpdatableMVar[_]]] = Map.empty): MonotonicMap[K] = new InMemMonotonicMap[K] {
    override private[monotonic] val map: AtomicReference[Map[K, Map[TypeTag[_], UpdatableMVar[_]]]] = new AtomicReference(content)
  }

}
