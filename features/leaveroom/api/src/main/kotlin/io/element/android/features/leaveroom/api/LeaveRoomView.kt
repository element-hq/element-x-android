/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.leaveroom.api

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun LeaveRoomView(
    state: LeaveRoomState,
    onSelectNewOwners: (RoomId) -> Unit,
) {
    if (state.needsSelectingNewOwners is LeaveRoomState.NeedsSelectingNewOwners.Shown) {
        LaunchedEffect(Unit) { onSelectNewOwners(state.needsSelectingNewOwners.roomId) }
    }
    LeaveRoomConfirmationDialog(state)
    LeaveRoomProgressDialog(state)
    LeaveRoomErrorDialog(state)
}

@Composable
private fun LeaveRoomConfirmationDialog(
    state: LeaveRoomState,
) {
    val defaultOnSubmitClick = { roomId: RoomId -> { state.eventSink(LeaveRoomEvent.LeaveRoom(roomId)) } }
    val defaultDismissAction = { state.eventSink(LeaveRoomEvent.HideConfirmation) }
    when (state.confirmation) {
        is LeaveRoomState.Confirmation.Hidden -> {}

        is LeaveRoomState.Confirmation.Dm -> LeaveRoomConfirmationDialog(
            text = stringResource(R.string.leave_room_alert_private_subtitle),
            isDm = false,
            onSubmitClick = defaultOnSubmitClick(state.confirmation.roomId),
            onDismiss = defaultDismissAction,
        )

        is LeaveRoomState.Confirmation.PrivateRoom -> LeaveRoomConfirmationDialog(
            text = stringResource(R.string.leave_room_alert_private_subtitle),
            isDm = false,
            onSubmitClick = defaultOnSubmitClick(state.confirmation.roomId),
            onDismiss = defaultDismissAction,
        )

        is LeaveRoomState.Confirmation.LastUserInRoom -> LeaveRoomConfirmationDialog(
            text = stringResource(R.string.leave_room_alert_empty_subtitle),
            isDm = false,
            onSubmitClick = defaultOnSubmitClick(state.confirmation.roomId),
            onDismiss = defaultDismissAction,
        )

        is LeaveRoomState.Confirmation.LastOwnerInRoom -> LeaveRoomConfirmationDialog(
            title = stringResource(R.string.leave_room_alert_select_new_owner_title),
            text = stringResource(R.string.leave_room_alert_select_new_owner_subtitle),
            isDm = false,
            submitText = stringResource(R.string.leave_room_alert_select_new_owner_action),
            destructiveSubmit = true,
            onSubmitClick = { state.eventSink(LeaveRoomEvent.SelectNewOwners(state.confirmation.roomId)) },
            onDismiss = defaultDismissAction,
        )

        is LeaveRoomState.Confirmation.Generic -> LeaveRoomConfirmationDialog(
            text = stringResource(R.string.leave_room_alert_subtitle),
            isDm = false,
            onSubmitClick = defaultOnSubmitClick(state.confirmation.roomId),
            onDismiss = defaultDismissAction,
        )
    }
}

@Composable
private fun LeaveRoomConfirmationDialog(
    isDm: Boolean,
    title: String = stringResource(if (isDm) CommonStrings.action_leave_conversation else CommonStrings.action_leave_room),
    text: String,
    submitText: String = stringResource(CommonStrings.action_leave),
    destructiveSubmit: Boolean = false,
    onSubmitClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmationDialog(
        title = title,
        content = text,
        submitText = submitText,
        onSubmitClick = onSubmitClick,
        onDismiss = onDismiss,
        destructiveSubmit = destructiveSubmit,
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
            onSubmit = { state.eventSink(LeaveRoomEvent.HideError) }
        )
    }
}

@PreviewsDayNight
@Composable
internal fun LeaveRoomViewPreview(
    @PreviewParameter(LeaveRoomStateProvider::class) state: LeaveRoomState
) = ElementPreview {
    Box(
        modifier = Modifier.size(300.dp, 300.dp),
        propagateMinConstraints = true,
    ) {
        LeaveRoomView(state = state, onSelectNewOwners = {})
    }
}
