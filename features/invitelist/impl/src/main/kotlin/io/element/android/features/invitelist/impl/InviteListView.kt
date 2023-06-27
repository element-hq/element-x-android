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

package io.element.android.features.invitelist.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.invitelist.impl.components.InviteSummaryRow
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Divider
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.R as StringR

@Composable
fun InviteListView(
    state: InviteListState,
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = {},
    onInviteAccepted: (RoomId) -> Unit = {},
) {
    if (state.acceptedAction is Async.Success) {
        LaunchedEffect(state.acceptedAction) {
            onInviteAccepted(state.acceptedAction.data)
        }
    }

    InviteListContent(
        state = state,
        modifier = modifier,
        onBackClicked = onBackClicked,
    )

    if (state.declineConfirmationDialog is InviteDeclineConfirmationDialog.Visible) {
        val contentResource = if (state.declineConfirmationDialog.isDirect)
            R.string.screen_invites_decline_direct_chat_message
        else
            R.string.screen_invites_decline_chat_message

        val titleResource = if (state.declineConfirmationDialog.isDirect)
            R.string.screen_invites_decline_direct_chat_title
        else
            R.string.screen_invites_decline_chat_title

        ConfirmationDialog(
            content = stringResource(contentResource, state.declineConfirmationDialog.name),
            title = stringResource(titleResource),
            submitText = stringResource(StringR.string.action_decline),
            cancelText = stringResource(StringR.string.action_cancel),
            emphasizeSubmitButton = true,
            onSubmitClicked = { state.eventSink(InviteListEvents.ConfirmDeclineInvite) },
            onDismiss = { state.eventSink(InviteListEvents.CancelDeclineInvite) }
        )
    }

    if (state.acceptedAction is Async.Failure) {
        ErrorDialog(
            content = stringResource(StringR.string.error_unknown),
            title = stringResource(StringR.string.common_error),
            submitText = stringResource(StringR.string.action_ok),
            onDismiss = { state.eventSink(InviteListEvents.DismissAcceptError) }
        )
    }

    if (state.declinedAction is Async.Failure) {
        ErrorDialog(
            content = stringResource(StringR.string.error_unknown),
            title = stringResource(StringR.string.common_error),
            submitText = stringResource(StringR.string.action_ok),
            onDismiss = { state.eventSink(InviteListEvents.DismissDeclineError) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InviteListContent(
    state: InviteListState,
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(onClick = onBackClicked)
                },
                title = {
                    Text(text = stringResource(StringR.string.action_invites_list))
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
            ) {
                if (state.inviteList.isEmpty()) {
                    Spacer(Modifier.size(80.dp))

                    Text(
                        text = stringResource(R.string.screen_invites_empty_list),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        itemsIndexed(
                            items = state.inviteList,
                        ) { index, invite ->
                            InviteSummaryRow(
                                invite = invite,
                                onAcceptClicked = { state.eventSink(InviteListEvents.AcceptInvite(invite)) },
                                onDeclineClicked = { state.eventSink(InviteListEvents.DeclineInvite(invite)) },
                            )

                            if (index != state.inviteList.lastIndex) {
                                Divider()
                            }
                        }
                    }
                }
            }
        }
    )
}

@Preview
@Composable
internal fun InviteListViewLightPreview(@PreviewParameter(InviteListStateProvider::class) state: InviteListState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
internal fun InviteListViewDarkPreview(@PreviewParameter(InviteListStateProvider::class) state: InviteListState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: InviteListState) {
    InviteListView(state)
}
