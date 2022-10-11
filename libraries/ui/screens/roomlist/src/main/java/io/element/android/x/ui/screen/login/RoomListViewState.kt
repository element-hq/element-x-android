package io.element.android.x.ui.screen.login

data class RoomListViewState(
    val list: List<String> = emptyList(),
    val canLoadMore: Boolean = false,
)
