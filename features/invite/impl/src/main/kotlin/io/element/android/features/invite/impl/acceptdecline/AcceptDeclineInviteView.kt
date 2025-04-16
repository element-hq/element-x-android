/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl.acceptdecline

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteState
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteStateProvider
import io.element.android.features.invite.api.acceptdecline.ConfirmingDeclineInvite
import io.element.android.features.invite.api.acceptdecline.InviteData
import io.element.android.features.invite.impl.R
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun AcceptDeclineInviteView(
    state: AcceptDeclineInviteState,
    onAcceptInvite: (RoomId) -> Unit,
    onDeclineInvite: (RoomId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        AsyncActionView(
            async = state.acceptAction,
            onSuccess = onAcceptInvite,
            onErrorDismiss = {
                state.eventSink(InternalAcceptDeclineInviteEvents.DismissAcceptError)
            },
        )
        AsyncActionView(
            async = state.declineAction,
            onSuccess = onDeclineInvite,
            onErrorDismiss = {
                state.eventSink(InternalAcceptDeclineInviteEvents.DismissDeclineError)
            },
            confirmationDialog = { confirming ->
                // Note: confirming will always be of type ConfirmingDeclineInvite.
                if (confirming is ConfirmingDeclineInvite) {
                    DeclineConfirmationDialog(
                        invite = confirming.inviteData,
                        blockUser = confirming.blockUser,
                        onConfirmClick = {
                            state.eventSink(InternalAcceptDeclineInviteEvents.ConfirmDeclineInvite)
                        },
                        onDismissClick = {
                            state.eventSink(InternalAcceptDeclineInviteEvents.CancelDeclineInvite)
                        }
                    )
                }
            }
        )
    }
}

@Composable
private fun DeclineConfirmationDialog(
    invite: InviteData,
    blockUser: Boolean,
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val senderId = invite.senderId.value
    val content = when {
        blockUser -> stringResource(R.string.screen_join_room_decline_and_block_alert_message, senderId)
        invite.isDm -> stringResource(R.string.screen_invites_decline_direct_chat_message, invite.roomName)
        else -> stringResource(R.string.screen_invites_decline_chat_message, invite.roomName)
    }
    val title = when {
        blockUser -> stringResource(R.string.screen_join_room_decline_and_block_alert_title)
        invite.isDm -> stringResource(R.string.screen_invites_decline_direct_chat_title)
        else -> stringResource(R.string.screen_invites_decline_chat_title)
    }
    val submitText = if (blockUser) {
        stringResource(R.string.screen_join_room_decline_and_block_alert_confirmation)
    } else {
        stringResource(CommonStrings.action_decline)
    }
    ConfirmationDialog(
        modifier = modifier,
        content = content,
        title = title,
        submitText = submitText,
        cancelText = stringResource(CommonStrings.action_cancel),
        onSubmitClick = onConfirmClick,
        destructiveSubmit = blockUser,
        onDismiss = onDismissClick,
    )
}

@PreviewsDayNight
@Composable
internal fun AcceptDeclineInviteViewPreview(@PreviewParameter(AcceptDeclineInviteStateProvider::class) state: AcceptDeclineInviteState) =
    ElementPreview {
        AcceptDeclineInviteView(
            state = state,
            onAcceptInvite = {},
            onDeclineInvite = {},
        )
    }
