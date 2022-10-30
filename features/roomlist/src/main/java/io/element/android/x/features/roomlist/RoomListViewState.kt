package io.element.android.x.features.roomlist

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import io.element.android.x.features.roomlist.model.MatrixUser
import io.element.android.x.matrix.room.RoomSummary

data class RoomListViewState(
    val user: MatrixUser = MatrixUser(),
    val rooms: Async<List<RoomSummary>> = Uninitialized,
    val canLoadMore: Boolean = false,
    val logoutAction: Async<Unit> = Uninitialized,
) : MavericksState
