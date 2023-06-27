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

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun LeaveRoomView(
    state: LeaveRoomState
) {
    LeaveRoomConfirmationDialog(state)
    LeaveRoomProgressDialog(state)
    LeaveRoomErrorDialog(state)
}

@Composable
private fun LeaveRoomConfirmationDialog(
    state: LeaveRoomState,
) {
    when (state.confirmation) {
        is LeaveRoomState.Confirmation.Hidden -> {}
        is LeaveRoomState.Confirmation.PrivateRoom -> LeaveRoomConfirmationDialog(
            text = CommonStrings.leave_room_alert_private_subtitle,
            roomId = state.confirmation.roomId,
            eventSink = state.eventSink,
        )

        is LeaveRoomState.Confirmation.LastUserInRoom -> LeaveRoomConfirmationDialog(
            text = CommonStrings.leave_room_alert_empty_subtitle,
            roomId = state.confirmation.roomId,
            eventSink = state.eventSink,
        )

        is LeaveRoomState.Confirmation.Generic -> LeaveRoomConfirmationDialog(
            text = CommonStrings.leave_room_alert_subtitle,
            roomId = state.confirmation.roomId,
            eventSink = state.eventSink,
        )
    }
}

@Composable
private fun LeaveRoomConfirmationDialog(
    @StringRes text: Int,
    roomId: RoomId,
    eventSink: (LeaveRoomEvent) -> Unit,
) {
    ConfirmationDialog(
        content = stringResource(text),
        submitText = stringResource(CommonStrings.action_leave),
        onSubmitClicked = { eventSink(LeaveRoomEvent.LeaveRoom(roomId)) },
        onDismiss = { eventSink(LeaveRoomEvent.HideConfirmation) },
    )
}

@Composable
private fun LeaveRoomProgressDialog(
    state: LeaveRoomState,
) {
    when (state.progress) {
        is LeaveRoomState.Progress.Hidden -> {}
        is LeaveRoomState.Progress.Shown -> ProgressDialog(
            text = stringResource(CommonStrings.common_leaving_room),
        )
    }
}

@Composable
private fun LeaveRoomErrorDialog(
    state: LeaveRoomState,
) {
    when (state.error) {
        is LeaveRoomState.Error.Hidden -> {}
        is LeaveRoomState.Error.Shown -> ErrorDialog(
            content = stringResource(CommonStrings.error_unknown),
            onDismiss = { state.eventSink(LeaveRoomEvent.HideError) }
        )
    }
}

@Preview
@Composable
internal fun LeaveRoomViewLightPreview(
    @PreviewParameter(LeaveRoomStateProvider::class) state: LeaveRoomState
) = ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
internal fun LeaveRoomViewDarkPreview(
    @PreviewParameter(LeaveRoomStateProvider::class) state: LeaveRoomState
) = ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: LeaveRoomState) {
    Box(
        modifier = Modifier.size(300.dp, 300.dp),
        propagateMinConstraints = true,
    ) {
        LeaveRoomView(state = state)
    }
}
