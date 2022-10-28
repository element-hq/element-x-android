package io.element.android.x.matrix.room.message

import io.element.android.x.matrix.core.EventId
import io.element.android.x.matrix.core.UserId
import org.matrix.rustcomponents.sdk.AnyMessage

class RoomMessageFactory {

    fun create(anyMessage: AnyMessage): RoomMessage? {
        val textMessage = anyMessage.textMessage()?.baseMessage() ?: return null
        return RoomMessage(
            eventId = EventId(textMessage.id()),
            body = textMessage.body(),
            sender = UserId(textMessage.sender()),
            originServerTs = textMessage.originServerTs().toLong()
        )
    }

}