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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.invitelist.impl.components.InviteSummaryRow
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
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
    onAcceptClicked: (RoomId) -> Unit = {},
    onDeclineClicked: (RoomId) -> Unit = {},
) {
    InviteListContent(
        state = state,
        modifier = modifier,
        onBackClicked = onBackClicked,
        onAcceptClicked = onAcceptClicked,
        onDeclineClicked = onDeclineClicked,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteListContent(
    state: InviteListState,
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = {},
    onAcceptClicked: (RoomId) -> Unit = {},
    onDeclineClicked: (RoomId) -> Unit = {},
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
                modifier = Modifier.padding(padding)
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
                        items(
                            items = state.inviteList,
                        ) { invite ->
                            InviteSummaryRow(
                                invite = invite,
                                onAcceptClicked = onAcceptClicked,
                                onDeclineClicked = onDeclineClicked,
                            )
                        }
                    }
                }
            }
        }
    )
}

@Preview
@Composable
internal fun RoomListViewLightPreview(@PreviewParameter(InviteListStateProvider::class) state: InviteListState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
internal fun RoomListViewDarkPreview(@PreviewParameter(InviteListStateProvider::class) state: InviteListState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: InviteListState) {
    InviteListView(state)
}
