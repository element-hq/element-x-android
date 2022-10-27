package io.element.android.x.features.roomlist

sealed interface RoomListActions {
    object LoadMore : RoomListActions
    object Logout : RoomListActions
}
