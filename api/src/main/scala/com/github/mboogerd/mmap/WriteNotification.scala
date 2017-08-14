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

package com.github.mboogerd.mmap

/**
 * Marker trait to signal write-progression. Actual notifications are application specific. Example uses include
 * LocalWriteFailed, LocalWriteSuccess, RemoteWriteSuccess(remote), etc.
 */
trait WriteNotification

/**
 * Marker trait to signal complete failure of the write. Extend this to add implementation-specific information
 */
trait FatalFailure extends WriteNotification

/**
 * Marker trait to signal partial failure of the write (such as one of N intended remotes failing). Extend this to add
 * implementation-specific information
 */
trait PartialFailure extends WriteNotification