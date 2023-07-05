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

import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface AwaitRoomState {
    object Loading : AwaitRoomState
    object Error : AwaitRoomState
    data class Loaded(val room: MatrixRoom) : AwaitRoomState
}

@SingleIn(SessionScope::class)
class AwaitRoomStateFlowFactory @Inject constructor(private val matrixClient: MatrixClient) {

    fun create(lifecycleScope: CoroutineScope, roomId: RoomId): StateFlow<AwaitRoomState> = suspend {
        matrixClient.getRoom(roomId = roomId)
    }
        .asFlow()
        .map { room ->
            if (room != null) {
                AwaitRoomState.Loaded(room)
            } else {
                AwaitRoomState.Error
            }
        }
        .stateIn(lifecycleScope, SharingStarted.Eagerly, AwaitRoomState.Loading)
}
