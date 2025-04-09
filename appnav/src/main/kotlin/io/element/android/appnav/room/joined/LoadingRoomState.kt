/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.room.joined

import androidx.compose.runtime.Immutable
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.JoinedMatrixRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@Immutable
sealed interface LoadingRoomState {
    data object Loading : LoadingRoomState
    data object Error : LoadingRoomState
    data class Loaded(val room: JoinedMatrixRoom) : LoadingRoomState
}

open class LoadingRoomStateProvider : PreviewParameterProvider<LoadingRoomState> {
    override val values: Sequence<LoadingRoomState>
        get() = sequenceOf(
            LoadingRoomState.Loading,
            LoadingRoomState.Error
        )
}

@SingleIn(SessionScope::class)
class LoadingRoomStateFlowFactory @Inject constructor(private val matrixClient: MatrixClient) {
    fun create(lifecycleScope: CoroutineScope, roomId: RoomId): StateFlow<LoadingRoomState> =
        getJoinedRoomFlow(roomId)
            .map { room ->
                if (room != null) {
                    LoadingRoomState.Loaded(room)
                } else {
                    LoadingRoomState.Error
                }
            }
            .stateIn(lifecycleScope, SharingStarted.Eagerly, LoadingRoomState.Loading)

    private fun getJoinedRoomFlow(roomId: RoomId): Flow<JoinedMatrixRoom?> = suspend {
        matrixClient.getJoinedRoom(roomId = roomId)
    }
        .asFlow()
}
