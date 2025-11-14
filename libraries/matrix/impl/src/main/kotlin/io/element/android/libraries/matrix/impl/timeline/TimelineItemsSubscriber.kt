/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline

import io.element.android.libraries.core.coroutine.childScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.matrix.rustcomponents.sdk.Timeline

/**
 * This class is responsible for subscribing to a timeline and post the items/diffs to the timelineDiffProcessor.
 * It will also trigger a callback when a new synced event is received.
 */
internal class TimelineItemsSubscriber(
    timelineCoroutineScope: CoroutineScope,
    dispatcher: CoroutineDispatcher,
    private val timeline: Timeline,
    private val timelineDiffProcessor: MatrixTimelineDiffProcessor,
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
                    timelineDiffProcessor.postDiffs(diffs)
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
}
