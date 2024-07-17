/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.matrix.impl.timeline

import io.element.android.libraries.core.coroutine.childScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.matrix.rustcomponents.sdk.Timeline
import org.matrix.rustcomponents.sdk.TimelineChange
import org.matrix.rustcomponents.sdk.TimelineDiff
import org.matrix.rustcomponents.sdk.TimelineItem
import uniffi.matrix_sdk_ui.EventItemOrigin
import java.util.concurrent.atomic.AtomicBoolean

private const val INITIAL_MAX_SIZE = 50

/**
 * This class is responsible for subscribing to a timeline and post the items/diffs to the timelineDiffProcessor.
 * It will also trigger a callback when a new synced event is received.
 * It will also handle the initial items and make sure they are posted before any diff.
 */
internal class TimelineItemsSubscriber(
    timelineCoroutineScope: CoroutineScope,
    dispatcher: CoroutineDispatcher,
    private val timeline: Timeline,
    private val timelineDiffProcessor: MatrixTimelineDiffProcessor,
    private val initLatch: CompletableDeferred<Unit>,
    private val isInit: AtomicBoolean,
    private val onNewSyncedEvent: () -> Unit,
) {
    private var subscriptionCount = 0
    private val mutex = Mutex()

    private val coroutineScope = timelineCoroutineScope.childScope(dispatcher, "TimelineItemsSubscriber")

    /**
     * Add a subscription to the timeline and start posting items/diffs to the timelineDiffProcessor.
     * It will also trigger a callback when a new synced event is received.
     */
    suspend fun subscribeIfNeeded() = mutex.withLock {
        if (subscriptionCount == 0) {
            timeline.timelineDiffFlow()
                .onEach { diffs ->
                    if (diffs.any { diff -> diff.eventOrigin() == EventItemOrigin.SYNC }) {
                        onNewSyncedEvent()
                    }
                    postDiffs(diffs)
                }
                .launchIn(coroutineScope)
        }
        subscriptionCount++
    }

    /**
     * Remove a subscription to the timeline and unsubscribe if needed.
     * The timeline will be unsubscribed when the last subscription is removed.
     * If the timelineCoroutineScope is cancelled, the timeline will be unsubscribed automatically.
     */
    suspend fun unsubscribeIfNeeded() = mutex.withLock {
        when (subscriptionCount) {
            0 -> return@withLock
            1 -> {
                coroutineScope.coroutineContext.cancelChildren()
            }
        }
        subscriptionCount--
    }

    private suspend fun postItems(items: List<TimelineItem>) = coroutineScope {
        // Split the initial items in multiple list as there is no pagination in the cached data, so we can post timelineItems asap.
        items.chunked(INITIAL_MAX_SIZE).reversed().forEach {
            ensureActive()
            timelineDiffProcessor.postItems(it)
        }
        isInit.set(true)
        initLatch.complete(Unit)
    }

    private suspend fun postDiffs(diffs: List<TimelineDiff>) {
        val diffsToProcess = diffs.toMutableList()
        if (!isInit.get()) {
            val resetDiff = diffsToProcess.firstOrNull { it.change() == TimelineChange.RESET }
            if (resetDiff != null) {
                // Keep using the postItems logic so we can post the timelineItems asap.
                postItems(resetDiff.reset() ?: emptyList())
                diffsToProcess.remove(resetDiff)
            }
        }
        initLatch.await()
        if (diffsToProcess.isNotEmpty()) {
            timelineDiffProcessor.postDiffs(diffsToProcess)
        }
    }
}
