/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.impl.timeline.item.event.EventTimelineItemMapper
import io.element.android.libraries.matrix.impl.timeline.item.virtual.VirtualTimelineItemMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.TimelineItem

class MatrixTimelineItemMapper(
    private val fetchDetailsForEvent: suspend (EventId) -> Result<Unit>,
    private val coroutineScope: CoroutineScope,
    private val virtualTimelineItemMapper: VirtualTimelineItemMapper,
    private val eventTimelineItemMapper: EventTimelineItemMapper,
) {
    fun map(timelineItem: TimelineItem): MatrixTimelineItem = timelineItem.use {
        val uniqueId = UniqueId(timelineItem.uniqueId().id)
        val asEvent = it.asEvent()
        if (asEvent != null) {
            val eventTimelineItem = eventTimelineItemMapper.map(asEvent)
            if (eventTimelineItem.hasNotLoadedInReplyTo() && eventTimelineItem.eventId != null) {
                fetchEventDetails(eventTimelineItem.eventId!!)
            }

            return MatrixTimelineItem.Event(uniqueId, eventTimelineItem)
        }
        val asVirtual = it.asVirtual()
        if (asVirtual != null) {
            val virtualTimelineItem = virtualTimelineItemMapper.map(asVirtual)
            return MatrixTimelineItem.Virtual(uniqueId, virtualTimelineItem)
        }
        return MatrixTimelineItem.Other
    }

    private fun fetchEventDetails(eventId: EventId) = coroutineScope.launch {
        fetchDetailsForEvent(eventId)
    }
}
