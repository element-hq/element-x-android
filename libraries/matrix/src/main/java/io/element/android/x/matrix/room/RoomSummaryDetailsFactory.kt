package io.element.android.x.matrix.room

import io.element.android.x.matrix.core.RoomId
import io.element.android.x.matrix.room.message.RoomMessageFactory
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.SlidingSyncRoom

class RoomSummaryDetailsFactory(private val roomMessageFactory: RoomMessageFactory = RoomMessageFactory()) {

    fun create(slidingSyncRoom: SlidingSyncRoom, room: Room?): RoomSummaryDetails {
        val latestRoomMessage = slidingSyncRoom.latestRoomMessage()?.let {
            roomMessageFactory.create(it)
        }
        val computedLastMessage = when {
            latestRoomMessage == null -> null
            slidingSyncRoom.isDm() == true -> latestRoomMessage.body
            else -> "${latestRoomMessage.sender.value}: ${latestRoomMessage.body}"
        }
        return RoomSummaryDetails(
            roomId = RoomId(slidingSyncRoom.roomId()),
            name = slidingSyncRoom.name() ?: slidingSyncRoom.roomId(),
            isDirect = slidingSyncRoom.isDm() ?: false,
            avatarURLString = room?.avatarUrl(),
            unreadNotificationCount = slidingSyncRoom.unreadNotifications().notificationCount().toInt(),
            lastMessage = computedLastMessage,
            lastMessageTimestamp = latestRoomMessage?.originServerTs
        )
    }
}
