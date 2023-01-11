package io.element.android.x.features.roomlist.model

sealed interface RoomListEvents {
    data class UpdateFilter(val newFilter: String) : RoomListEvents
    data class UpdateVisibleRange(val range: IntRange): RoomListEvents
}
