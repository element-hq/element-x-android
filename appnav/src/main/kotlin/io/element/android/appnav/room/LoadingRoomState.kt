/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.appnav.room

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface LoadingRoomState {
    data object Loading : LoadingRoomState
    data object Error : LoadingRoomState
    data class Loaded(val room: MatrixRoom) : LoadingRoomState
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
        getRoomFlow(roomId)
            .map { room ->
                if (room != null) {
                    LoadingRoomState.Loaded(room)
                } else {
                    LoadingRoomState.Error
                }
            }
            .stateIn(lifecycleScope, SharingStarted.Eagerly, LoadingRoomState.Loading)

    private fun getRoomFlow(roomId: RoomId): Flow<MatrixRoom?> = suspend {
        matrixClient.getRoom(roomId = roomId)
    }
        .asFlow()
}
