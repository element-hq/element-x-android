package io.element.android.x.features.roomlist.model

import io.element.android.x.designsystem.components.avatar.AvatarData

object RoomListRoomSummaryPlaceholders {

    fun create(id: String): RoomListRoomSummary {
        return RoomListRoomSummary(
            id = id,
            isPlaceholder = true,
            name = "Short name",
            timestamp = "hh:mm",
            lastMessage = "Last message for placeholder",
            avatarData = AvatarData("S")
        )
    }

    fun createFakeList(size: Int): List<RoomListRoomSummary> {
        return mutableListOf<RoomListRoomSummary>().apply {
            for (i in 0..size) {
                add(create("\$fakeRoom$i"))
            }
        }
    }
}

