/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.matrix.impl.timeline

import io.element.android.libraries.matrix.impl.util.cancelAndDestroy
import io.element.android.libraries.matrix.impl.util.destroyAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import org.matrix.rustcomponents.sdk.PaginationStatusListener
import org.matrix.rustcomponents.sdk.Timeline
import org.matrix.rustcomponents.sdk.TimelineDiff
import org.matrix.rustcomponents.sdk.TimelineItem
import org.matrix.rustcomponents.sdk.TimelineListener
import timber.log.Timber
import uniffi.matrix_sdk_ui.LiveBackPaginationStatus

internal fun Timeline.liveBackPaginationStatus(): Flow<LiveBackPaginationStatus> = callbackFlow {
    val listener = object : PaginationStatusListener {
        override fun onUpdate(status: LiveBackPaginationStatus) {
            trySend(status)
        }
    }
    val result = subscribeToBackPaginationStatus(listener)
    awaitClose {
        result.cancelAndDestroy()
    }
}.catch {
    Timber.d(it, "liveBackPaginationStatus() failed")
}.buffer(Channel.UNLIMITED)

internal fun Timeline.timelineDiffFlow(onInitialList: suspend (List<TimelineItem>) -> Unit): Flow<List<TimelineDiff>> =
    callbackFlow {
        val listener = object : TimelineListener {
            override fun onUpdate(diff: List<TimelineDiff>) {
                trySendBlocking(diff)
            }
        }
        Timber.d("Open timelineDiffFlow for TimelineInterface ${this@timelineDiffFlow}")
        val result = addListener(listener)
        try {
            onInitialList(result.items)
        } catch (exception: Exception) {
            Timber.d(exception, "Catch failure in timelineDiffFlow of TimelineInterface ${this@timelineDiffFlow}")
        }
        awaitClose {
            Timber.d("Close timelineDiffFlow for TimelineInterface ${this@timelineDiffFlow}")
            result.itemsStream.cancelAndDestroy()
            result.items.destroyAll()
        }
    }.catch {
        Timber.d(it, "timelineDiffFlow() failed")
    }.buffer(Channel.UNLIMITED)

internal suspend fun Timeline.runWithTimelineListenerRegistered(action: suspend () -> Unit) {
    val result = addListener(NoOpTimelineListener)
    try {
        action()
    } finally {
        result.itemsStream.cancelAndDestroy()
        result.items.destroyAll()
    }
}

private object NoOpTimelineListener : TimelineListener {
    override fun onUpdate(diff: List<TimelineDiff>) = Unit
}
