package io.element.android.x.features.roomlist.model

import androidx.compose.runtime.Immutable
import io.element.android.x.matrix.ui.model.MatrixUser
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class RoomListState(
    val matrixUser: MatrixUser?,
    val roomList: ImmutableList<RoomListRoomSummary>,
    val filter: String,
    val isLoginOut: Boolean,
    val eventSink: (RoomListEvents) -> Unit = {}
)
