package io.element.android.x.ui.screen.roomlist

sealed interface RoomListActions {
    object Logout : RoomListActions
    object LoadMore : RoomListActions
}
