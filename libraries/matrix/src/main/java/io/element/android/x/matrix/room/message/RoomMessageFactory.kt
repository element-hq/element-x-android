package io.element.android.x.matrix.room.message

import io.element.android.x.matrix.core.EventId
import io.element.android.x.matrix.core.UserId
import org.matrix.rustcomponents.sdk.EventTimelineItem

class RoomMessageFactory {
    fun create(eventTimelineItem: EventTimelineItem?): RoomMessage? {
        eventTimelineItem ?: return null
        return RoomMessage(
            eventId = EventId(eventTimelineItem.eventId() ?: ""),
            body = eventTimelineItem.content().asMessage()?.body() ?: "",
            sender = UserId(eventTimelineItem.sender()),
            originServerTs = eventTimelineItem.originServerTs()?.toLong() ?: 0L
        )
    }

}