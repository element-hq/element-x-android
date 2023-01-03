package io.element.android.x.features.roomlist.model

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList

object RoomListScreen {

    @Stable
    data class State(
        val matrixUser: MatrixUser?,
        val roomList: ImmutableList<RoomListRoomSummary>,
        val filter: String,
        val isLoginOut: Boolean,
    )

    sealed interface Event {
        object Logout : Event
        data class UpdateFilter(val newFilter: String) : Event
        data class UpdateVisibleRange(val range: IntRange): Event
    }
}
