/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.core.EventId
import timber.log.Timber
import javax.inject.Inject

@SingleIn(RoomScope::class)
class TimelineItemIndexer @Inject constructor() {
    private val timelineEventsIndexes = mutableMapOf<EventId, Int>()

    fun isKnown(eventId: EventId): Boolean {
        return timelineEventsIndexes.containsKey(eventId).also {
            Timber.d("$eventId isKnown = $it")
        }
    }

    fun indexOf(eventId: EventId): Int {
        return (timelineEventsIndexes[eventId] ?: -1).also {
            Timber.d("indexOf $eventId= $it")
        }
    }

    fun process(timelineItems: List<TimelineItem>) {
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
    }

    private fun processEvent(event: TimelineItem.Event, index: Int) {
        if (event.eventId == null) return
        timelineEventsIndexes[event.eventId] = index
    }
}
