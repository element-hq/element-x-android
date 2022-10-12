package io.element.android.x.ui.screen.roomlist

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.UpdateSummary

data class RoomListViewState(
    val user: MatrixUser = MatrixUser(),
    val rooms: Async<List<Room>> = Uninitialized,
    val summary: Async<UpdateSummary> = Uninitialized,
    val canLoadMore: Boolean = false,
    val logoutAction: Async<Unit> = Uninitialized,
) : MavericksState
