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

package io.element.android.appnav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.MatrixRoom
import timber.log.Timber

class RoomFlowPresenter(
    private val room: MatrixRoom,
) : Presenter<RoomFlowState> {

    @Composable
    override fun present(): RoomFlowState {
        // Preload room members so we can quickly detect if the room is a DM room
        LaunchedEffect(Unit) {
            room.fetchMembers()
                .onFailure {
                    Timber.e(it, "Fail to fetch members for room ${room.roomId}")
                }.onSuccess {
                    Timber.v("Success fetching members for room ${room.roomId}")
                }
        }

        return RoomFlowState
    }
}

// At first the return type was Unit, but detekt complained about it
object RoomFlowState
