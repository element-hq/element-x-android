package io.element.android.x.ui.screen.roomlist

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized

data class RoomListViewState(
    val list: List<String> = emptyList(),
    val canLoadMore: Boolean = false,
    val logoutAction: Async<Unit> = Uninitialized,
) : MavericksState
