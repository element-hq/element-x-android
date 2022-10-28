package io.element.android.x.matrix.room.message

import io.element.android.x.matrix.core.EventId
import io.element.android.x.matrix.core.UserId

data class RoomMessage(
    val eventId: EventId,
    val body: String,
    val sender: UserId,
    val originServerTs: Long,
)
