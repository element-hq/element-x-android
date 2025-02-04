/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.message

import io.element.android.libraries.matrix.api.room.message.RoomMessage
import io.element.android.libraries.matrix.impl.timeline.item.event.EventTimelineItemMapper
import org.matrix.rustcomponents.sdk.EventTimelineItem as RustEventTimelineItem

class RoomMessageFactory(
    private val eventTimelineItemMapper: EventTimelineItemMapper = EventTimelineItemMapper(),
) {
    fun create(eventTimelineItem: RustEventTimelineItem?): RoomMessage? {
        eventTimelineItem ?: return null
        val mappedTimelineItem = eventTimelineItemMapper.map(eventTimelineItem)
        return RoomMessage(
            eventId = mappedTimelineItem.eventId ?: return null,
            event = mappedTimelineItem,
            sender = mappedTimelineItem.sender,
            originServerTs = mappedTimelineItem.timestamp,
        )
    }
}
