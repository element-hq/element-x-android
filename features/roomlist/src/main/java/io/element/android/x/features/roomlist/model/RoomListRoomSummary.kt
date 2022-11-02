package io.element.android.x.features.roomlist.model

import io.element.android.x.matrix.core.RoomId

data class RoomListRoomSummary(
    val id: String,
    val roomId: RoomId = RoomId(id),
    val name: String = "",
    val hasUnread: Boolean = false,
    val timestamp: String? = null,
    val lastMessage: CharSequence? = null,
    val avatarData: ByteArray? = null,
    val isPlaceholder: Boolean = false,
)