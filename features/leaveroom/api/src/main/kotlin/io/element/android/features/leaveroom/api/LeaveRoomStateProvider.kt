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

package io.element.android.features.leaveroom.api

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.RoomId

class LeaveRoomStateProvider : PreviewParameterProvider<LeaveRoomState> {
    override val values: Sequence<LeaveRoomState>
        get() = sequenceOf(
            aLeaveRoomState(
                confirmation = LeaveRoomState.Confirmation.Hidden,
                progress = LeaveRoomState.Progress.Hidden,
                error = LeaveRoomState.Error.Hidden,
            ),
            aLeaveRoomState(
                confirmation = LeaveRoomState.Confirmation.Generic(roomId = A_ROOM_ID),
                progress = LeaveRoomState.Progress.Hidden,
                error = LeaveRoomState.Error.Hidden,
            ),
            aLeaveRoomState(
                confirmation = LeaveRoomState.Confirmation.PrivateRoom(roomId = A_ROOM_ID),
                progress = LeaveRoomState.Progress.Hidden,
                error = LeaveRoomState.Error.Hidden,
            ),
            aLeaveRoomState(
                confirmation = LeaveRoomState.Confirmation.LastUserInRoom(roomId = A_ROOM_ID),
                progress = LeaveRoomState.Progress.Hidden,
                error = LeaveRoomState.Error.Hidden,
            ),
            aLeaveRoomState(
                confirmation = LeaveRoomState.Confirmation.Hidden,
                progress = LeaveRoomState.Progress.Shown,
                error = LeaveRoomState.Error.Hidden,
            ),
            aLeaveRoomState(
                confirmation = LeaveRoomState.Confirmation.Hidden,
                progress = LeaveRoomState.Progress.Hidden,
                error = LeaveRoomState.Error.Shown,
            ),
            aLeaveRoomState(
                confirmation = LeaveRoomState.Confirmation.Dm(roomId = A_ROOM_ID),
                progress = LeaveRoomState.Progress.Hidden,
                error = LeaveRoomState.Error.Hidden,
            ),
        )
}

private val A_ROOM_ID = RoomId("!aRoomId:aDomain")

fun aLeaveRoomState(
    confirmation: LeaveRoomState.Confirmation = LeaveRoomState.Confirmation.Hidden,
    progress: LeaveRoomState.Progress = LeaveRoomState.Progress.Hidden,
    error: LeaveRoomState.Error = LeaveRoomState.Error.Hidden,
) = LeaveRoomState(
    confirmation = confirmation,
    progress = progress,
    error = error,
    eventSink = {},
)
