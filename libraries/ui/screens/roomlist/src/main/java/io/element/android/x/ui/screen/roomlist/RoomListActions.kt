package io.element.android.x.ui.screen.roomlist

sealed interface RoomListActions {
    object Init : RoomListActions
    object LoadMore : RoomListActions
    object Logout : RoomListActions
}
