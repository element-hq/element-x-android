package io.element.android.x.features.roomlist.model

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import io.element.android.x.matrix.core.RoomId

data class RoomListViewState(
    val user: Async<MatrixUser> = Uninitialized,
    // Will contain the filtered rooms, using ::filter (if filter is not empty)
    val rooms: Async<List<RoomListRoomSummary>> = Uninitialized,
    val filter: String = "",
    val canLoadMore: Boolean = false,
    val logoutAction: Async<Unit> = Uninitialized,
    val roomsById: Map<RoomId, RoomListRoomSummary> = emptyMap()
) : MavericksState
