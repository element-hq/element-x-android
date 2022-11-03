package io.element.android.x.features.roomlist.model

import io.element.android.x.designsystem.components.avatar.AvatarData

internal fun stubbedRoomSummaries(): List<RoomListRoomSummary> {
    return listOf(
        RoomListRoomSummary(
            name = "Room",
            hasUnread = true,
            timestamp = "14:18",
            lastMessage = "A very very very very long message which suites on two lines",
            avatarData = AvatarData("R"),
            id = "roomId"
        ),
        RoomListRoomSummary(
            name = "Room#2",
            hasUnread = false,
            timestamp = "14:16",
            lastMessage = "A short message",
            avatarData = AvatarData("Z"),
            id = "roomId2"
        ),
        RoomListRoomSummary.placeholder("roomId2")
    )
}