package io.element.android.x.features.roomlist.model

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList

@Stable
data class RoomListState(
    val matrixUser: MatrixUser?,
    val roomList: ImmutableList<RoomListRoomSummary>,
    val filter: String,
    val isLoginOut: Boolean,
)
