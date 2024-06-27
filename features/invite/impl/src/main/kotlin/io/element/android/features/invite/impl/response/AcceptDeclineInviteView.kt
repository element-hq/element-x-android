/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.invite.impl.response

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.invite.api.response.AcceptDeclineInviteState
import io.element.android.features.invite.api.response.AcceptDeclineInviteStateProvider
import io.element.android.features.invite.api.response.InviteData
import io.element.android.features.invite.impl.R
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.CommonStrings
import kotlin.jvm.optionals.getOrNull

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
            confirmationDialog = {
                val invite = state.invite.getOrNull()
                if (invite != null) {
                    DeclineConfirmationDialog(
                        invite = invite,
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
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentResource = if (invite.isDm) {
        R.string.screen_invites_decline_direct_chat_message
    } else {
        R.string.screen_invites_decline_chat_message
    }

    val titleResource = if (invite.isDm) {
        R.string.screen_invites_decline_direct_chat_title
    } else {
        R.string.screen_invites_decline_chat_title
    }

    ConfirmationDialog(
        modifier = modifier,
        content = stringResource(contentResource, invite.roomName),
        title = stringResource(titleResource),
        submitText = stringResource(CommonStrings.action_decline),
        cancelText = stringResource(CommonStrings.action_cancel),
        onSubmitClick = onConfirmClick,
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
