package io.element.android.x.matrix.room

import io.element.android.x.matrix.core.RoomId


sealed interface RoomSummary {
    data class Empty(val identifier: String) : RoomSummary
    data class Filled(val details: RoomSummaryDetails) : RoomSummary

    fun identifier(): String {
        return when (this) {
            is Empty -> identifier
            is Filled -> details.roomId.value
        }
    }

}

data class RoomSummaryDetails(
    val roomId: RoomId,
    val name: String?,
    val isDirect: Boolean,
    val avatarURLString: String?,
    val lastMessage: CharSequence?,
    val lastMessageTimestamp: Long?,
    val unreadNotificationCount: UInt,
)
