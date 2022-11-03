package io.element.android.x.features.roomlist.model

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import io.element.android.x.features.roomlist.model.MatrixUser
import io.element.android.x.matrix.room.RoomSummary

data class RoomListViewState(
    val user: Async<MatrixUser> = Uninitialized,
    val rooms: Async<List<RoomListRoomSummary>> = Uninitialized,
    val canLoadMore: Boolean = false,
    val logoutAction: Async<Unit> = Uninitialized,
) : MavericksState
