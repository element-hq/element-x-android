/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
    eventSink: (LeaveRoomEvent) -> Unit = {},
) = LeaveRoomState(
    confirmation = confirmation,
    progress = progress,
    error = error,
    eventSink = eventSink,
)
