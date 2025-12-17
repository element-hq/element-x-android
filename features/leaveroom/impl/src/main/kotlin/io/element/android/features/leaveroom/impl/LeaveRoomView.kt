/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.leaveroom.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.leaveroom.api.LeaveRoomEvent
import io.element.android.features.leaveroom.api.R
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.CommonStrings

@Suppress("LambdaParameterEventTrailing")
@Composable
fun LeaveRoomView(
    state: InternalLeaveRoomState,
    onSelectNewOwners: (RoomId) -> Unit,
) {
    AsyncActionView(
        state.leaveAction,
        onSuccess = {
            state.eventSink(InternalLeaveRoomEvent.ResetState)
        },
        onErrorDismiss = {
            state.eventSink(InternalLeaveRoomEvent.ResetState)
        },
        confirmationDialog = { confirmation ->
            if (confirmation is Confirmation) {
                LeaveRoomConfirmationDialog(
                    confirmation = confirmation,
                    eventSink = state.eventSink,
                    onSelectNewOwners = onSelectNewOwners,
                )
            }
        },
        errorTitle = { stringResource(CommonStrings.common_something_went_wrong) },
        errorMessage = { stringResource(CommonStrings.error_network_or_server_issue) },
        progressDialog = { LeaveRoomProgressDialog() },
    )
}

@Composable
private fun LeaveRoomConfirmationDialog(
    confirmation: Confirmation,
    eventSink: (LeaveRoomEvent) -> Unit,
    onSelectNewOwners: (RoomId) -> Unit,
) {
    val defaultOnSubmitClick = { roomId: RoomId -> { eventSink(LeaveRoomEvent.LeaveRoom(roomId, needsConfirmation = false)) } }
    val defaultDismissAction = { eventSink(InternalLeaveRoomEvent.ResetState) }
    when (confirmation) {
        is Confirmation.Dm -> LeaveRoomConfirmationDialog(
            text = stringResource(R.string.leave_room_alert_private_subtitle),
            isDm = false,
            onSubmitClick = defaultOnSubmitClick(confirmation.roomId),
            onDismiss = defaultDismissAction,
        )

        is Confirmation.PrivateRoom -> LeaveRoomConfirmationDialog(
            text = stringResource(R.string.leave_room_alert_private_subtitle),
            isDm = false,
            onSubmitClick = defaultOnSubmitClick(confirmation.roomId),
            onDismiss = defaultDismissAction,
        )

        is Confirmation.LastUserInRoom -> LeaveRoomConfirmationDialog(
            text = stringResource(R.string.leave_room_alert_empty_subtitle),
            isDm = false,
            onSubmitClick = defaultOnSubmitClick(confirmation.roomId),
            onDismiss = defaultDismissAction,
        )

        is Confirmation.LastOwnerInRoom -> LeaveRoomConfirmationDialog(
            title = stringResource(R.string.leave_room_alert_select_new_owner_title),
            text = stringResource(R.string.leave_room_alert_select_new_owner_subtitle),
            isDm = false,
            submitText = stringResource(R.string.leave_room_alert_select_new_owner_action),
            destructiveSubmit = true,
            onSubmitClick = {
                onSelectNewOwners(confirmation.roomId)
                eventSink(InternalLeaveRoomEvent.ResetState)
            },
            onDismiss = defaultDismissAction,
        )

        is Confirmation.Generic -> LeaveRoomConfirmationDialog(
            text = stringResource(R.string.leave_room_alert_subtitle),
            isDm = false,
            onSubmitClick = defaultOnSubmitClick(confirmation.roomId),
            onDismiss = defaultDismissAction,
        )
    }
}

@Composable
private fun LeaveRoomConfirmationDialog(
    isDm: Boolean,
    text: String,
    onSubmitClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(if (isDm) CommonStrings.action_leave_conversation else CommonStrings.action_leave_room),
    submitText: String = stringResource(CommonStrings.action_leave),
    destructiveSubmit: Boolean = false,
) {
    ConfirmationDialog(
        title = title,
        content = text,
        submitText = submitText,
        onSubmitClick = onSubmitClick,
        onDismiss = onDismiss,
        destructiveSubmit = destructiveSubmit,
        modifier = modifier,
    )
}

@Composable
private fun LeaveRoomProgressDialog(modifier: Modifier = Modifier) {
    ProgressDialog(
        text = stringResource(CommonStrings.common_leaving_room),
        modifier = modifier,
    )
}

@PreviewsDayNight
@Composable
internal fun LeaveRoomViewPreview(
    @PreviewParameter(InternalLeaveRoomStateProvider::class) state: InternalLeaveRoomState
) = ElementPreview {
    Box(
        modifier = Modifier.size(300.dp, 300.dp),
        propagateMinConstraints = true,
    ) {
        LeaveRoomView(state = state, onSelectNewOwners = {})
    }
}
