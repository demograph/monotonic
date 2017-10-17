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

package io.demograph.crdt.delta

import io.demograph.crdt.Session
import io.demograph.crdt.delta.causal.CausalCRDT
import io.demograph.crdt.delta.dot.{ Dot, DotStore }
import io.demograph.crdt.instances.ORMap.ORMap

/**
 *
 */
trait CRDTSpec {

  val replicaID = 0

  implicit val mockSession: Session[Int] = new Session[Int] {
    override def localhost: Int = replicaID
  }
}
