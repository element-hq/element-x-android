/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline

import io.element.android.libraries.matrix.impl.util.cancelAndDestroy
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import org.matrix.rustcomponents.sdk.PaginationStatusListener
import org.matrix.rustcomponents.sdk.TimelineDiff
import org.matrix.rustcomponents.sdk.TimelineInterface
import org.matrix.rustcomponents.sdk.TimelineListener
import timber.log.Timber
import uniffi.matrix_sdk.RoomPaginationStatus

internal fun TimelineInterface.liveBackPaginationStatus(): Flow<RoomPaginationStatus> = callbackFlow {
    val listener = object : PaginationStatusListener {
        override fun onUpdate(status: RoomPaginationStatus) {
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

internal fun TimelineInterface.timelineDiffFlow(): Flow<List<TimelineDiff>> =
    callbackFlow {
        val listener = object : TimelineListener {
            override fun onUpdate(diff: List<TimelineDiff>) {
                trySendBlocking(diff)
            }
        }
        Timber.d("Open timelineDiffFlow for TimelineInterface ${this@timelineDiffFlow}")
        val taskHandle = addListener(listener)
        awaitClose {
            Timber.d("Close timelineDiffFlow for TimelineInterface ${this@timelineDiffFlow}")
            taskHandle.cancelAndDestroy()
        }
    }.catch {
        Timber.d(it, "timelineDiffFlow() failed")
    }.buffer(Channel.UNLIMITED)

internal suspend fun TimelineInterface.runWithTimelineListenerRegistered(action: suspend () -> Unit) {
    val result = addListener(NoOpTimelineListener)
    try {
        action()
    } finally {
        result.cancelAndDestroy()
    }
}

private object NoOpTimelineListener : TimelineListener {
    override fun onUpdate(diff: List<TimelineDiff>) = Unit
}
