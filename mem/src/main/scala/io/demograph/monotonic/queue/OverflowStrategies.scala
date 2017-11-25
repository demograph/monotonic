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

package io.demograph.monotonic.queue

/**
 *
 */
object OverflowStrategies {

  sealed trait OverflowStrategy

  sealed trait PurgingOverflowStrategy extends OverflowStrategy

  sealed trait MergingOverflowStrategy extends OverflowStrategy

  private[monotonic] case object DropHead extends PurgingOverflowStrategy

  private[monotonic] case object DropTail extends PurgingOverflowStrategy

  private[monotonic] case object DropBuffer extends PurgingOverflowStrategy

  private[monotonic] case object DropNew extends PurgingOverflowStrategy

  private[monotonic] case object MergeHead extends MergingOverflowStrategy

  private[monotonic] case object MergePairs extends MergingOverflowStrategy

  private[monotonic] case object MergeAll extends MergingOverflowStrategy

  private[monotonic] case object BalancedMerge extends MergingOverflowStrategy

  private[monotonic] case class BoundedBalancedMerge(maxMerges: Int, fallback: PurgingOverflowStrategy) extends MergingOverflowStrategy

  // TODO: Decide whether to keep or not, and if so, whether to allow it as fallback for merging
  //    private[monotonic] case object Fail extends OverflowStrategy
}