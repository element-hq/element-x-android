/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.matrix.api.core.EventId
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject

class TimelineItemIndexer @Inject constructor() {
    // This is a latch to wait for the first process call
    private val firstProcessLatch = CompletableDeferred<Unit>()
    private val timelineEventsIndexes = mutableMapOf<EventId, Int>()

    private val mutex = Mutex()

    suspend fun isKnown(eventId: EventId): Boolean {
        firstProcessLatch.await()
        return mutex.withLock {
            timelineEventsIndexes.containsKey(eventId).also {
                Timber.d("$eventId isKnown = $it")
            }
        }
    }

    suspend fun indexOf(eventId: EventId): Int {
        firstProcessLatch.await()
        return mutex.withLock {
            (timelineEventsIndexes[eventId] ?: -1).also {
                Timber.d("indexOf $eventId= $it")
            }
        }
    }

    suspend fun process(timelineItems: List<TimelineItem>) = mutex.withLock {
        Timber.d("process ${timelineItems.size} items")
        timelineEventsIndexes.clear()
        timelineItems.forEachIndexed { index, timelineItem ->
            when (timelineItem) {
                is TimelineItem.Event -> {
                    processEvent(timelineItem, index)
                }
                is TimelineItem.GroupedEvents -> {
                    timelineItem.events.forEach { event ->
                        processEvent(event, index)
                    }
                }
                else -> Unit
            }
        }
        firstProcessLatch.complete(Unit)
    }

    private fun processEvent(event: TimelineItem.Event, index: Int) {
        if (event.eventId == null) return
        timelineEventsIndexes[event.eventId] = index
    }
}
