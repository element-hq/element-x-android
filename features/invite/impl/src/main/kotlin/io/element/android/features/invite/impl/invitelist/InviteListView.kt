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

package io.element.android.features.invite.impl.invitelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.invite.impl.R
import io.element.android.features.invite.impl.components.InviteSummaryRow
import io.element.android.features.invite.impl.response.AcceptDeclineInviteView
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun InviteListView(
    state: InviteListState,
    onBackClicked: () -> Unit,
    onInviteAccepted: (RoomId) -> Unit,
    onInviteDeclined: (RoomId) -> Unit,
    onInviteClicked: (RoomId) -> Unit,
    modifier: Modifier = Modifier,
) {
    InviteListContent(
        state = state,
        modifier = modifier,
        onInviteClicked = onInviteClicked,
        onBackClicked = onBackClicked,
    )
    AcceptDeclineInviteView(
        state = state.acceptDeclineInviteState,
        onInviteAccepted = onInviteAccepted,
        onInviteDeclined = onInviteDeclined,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InviteListContent(
    state: InviteListState,
    onBackClicked: () -> Unit,
    onInviteClicked: (RoomId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(onClick = onBackClicked)
                },
                title = {
                    Text(
                        text = stringResource(CommonStrings.action_invites_list),
                        style = ElementTheme.typography.aliasScreenTitle,
                    )
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
                                modifier = Modifier.clickable(
                                    onClick = { onInviteClicked(invite.roomId) }
                                ),
                                invite = invite,
                                onAcceptClicked = { state.eventSink(InviteListEvents.AcceptInvite(invite)) },
                                onDeclineClicked = { state.eventSink(InviteListEvents.DeclineInvite(invite)) },
                            )

                            if (index != state.inviteList.lastIndex) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    )
}

@PreviewsDayNight
@Composable
internal fun InviteListViewPreview(@PreviewParameter(InviteListStateProvider::class) state: InviteListState) = ElementPreview {
    InviteListView(
        state = state,
        onBackClicked = {},
        onInviteAccepted = {},
        onInviteDeclined = {},
        onInviteClicked = {},
    )
}
