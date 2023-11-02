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

package io.element.android.features.messages.impl.forward

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialogDefaults
import io.element.android.libraries.composeutils.annotations.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.RadioButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.SearchBar
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.theme.roomListRoomMessage
import io.element.android.libraries.designsystem.theme.roomListRoomName
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomlist.RoomSummaryDetails
import io.element.android.libraries.matrix.ui.components.SelectedRoom
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ForwardMessagesView(
    state: ForwardMessagesState,
    onDismiss: () -> Unit,
    onForwardingSucceeded: (ImmutableList<RoomId>) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.forwardingSucceeded != null) {
        onForwardingSucceeded(state.forwardingSucceeded)
        return
    }

    @Suppress("UNUSED_PARAMETER")
    fun onRoomRemoved(roomSummaryDetails: RoomSummaryDetails) {
        // TODO toggle selection when multi-selection is enabled
        state.eventSink(ForwardMessagesEvents.RemoveSelectedRoom)
    }

    @Composable
    fun SelectedRoomsHelper(isForwarding: Boolean, selectedRooms: ImmutableList<RoomSummaryDetails>) {
        if (isForwarding) return
        SelectedRooms(
            selectedRooms = selectedRooms,
            onRoomRemoved = ::onRoomRemoved,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }

    fun onBackButton(state: ForwardMessagesState) {
        if (state.isSearchActive) {
            state.eventSink(ForwardMessagesEvents.ToggleSearchActive)
        } else {
            onDismiss()
        }
    }

    BackHandler(onBack = { onBackButton(state) })

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(CommonStrings.common_forward_message),
                        style = ElementTheme.typography.aliasScreenTitle
                    )
                },
                navigationIcon = {
                    BackButton(onClick = { onBackButton(state) })
                },
                actions = {
                    TextButton(
                        text = stringResource(CommonStrings.action_send),
                        enabled = state.selectedRooms.isNotEmpty(),
                        onClick = { state.eventSink(ForwardMessagesEvents.ForwardEvent) }
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            SearchBar(
                placeHolderTitle = stringResource(CommonStrings.action_search),
                query = state.query,
                onQueryChange = { state.eventSink(ForwardMessagesEvents.UpdateQuery(it)) },
                active = state.isSearchActive,
                onActiveChange = { state.eventSink(ForwardMessagesEvents.ToggleSearchActive) },
                resultState = state.resultState,
                showBackButton = false,
            ) { summaries ->
                LazyColumn {
                    item {
                        SelectedRoomsHelper(
                            isForwarding = state.isForwarding,
                            selectedRooms = state.selectedRooms
                        )
                    }
                    items(summaries, key = { it.roomId.value }) { roomSummary ->
                        Column {
                            RoomSummaryView(
                                roomSummary,
                                isSelected = state.selectedRooms.any { it.roomId == roomSummary.roomId },
                                onSelection = { roomSummary ->
                                    state.eventSink(ForwardMessagesEvents.SetSelectedRoom(roomSummary))
                                }
                            )
                            HorizontalDivider(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            if (!state.isSearchActive) {
                // TODO restore for multi-selection
//                SelectedRoomsHelper(
//                    isForwarding = state.isForwarding,
//                    selectedRooms = state.selectedRooms
//                )
                Spacer(modifier = Modifier.height(20.dp))

                if (state.resultState is SearchBarResultState.Results) {
                    LazyColumn {
                        items(state.resultState.results, key = { it.roomId.value }) { roomSummary ->
                            Column {
                                RoomSummaryView(
                                    roomSummary,
                                    isSelected = state.selectedRooms.any { it.roomId == roomSummary.roomId },
                                    onSelection = { roomSummary ->
                                        state.eventSink(ForwardMessagesEvents.SetSelectedRoom(roomSummary))
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }

            if (state.isForwarding) {
                ProgressDialog()
            }

            if (state.error != null) {
                ForwardingErrorDialog(onDismiss = { state.eventSink(ForwardMessagesEvents.ClearError) })
            }
        }
    }
}

@Composable
private fun SelectedRooms(
    selectedRooms: ImmutableList<RoomSummaryDetails>,
    onRoomRemoved: (RoomSummaryDetails) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        items(selectedRooms, key = { it.roomId.value }) { roomSummary ->
            SelectedRoom(roomSummary = roomSummary, onRoomRemoved = onRoomRemoved)
        }
    }
}

@Composable
private fun RoomSummaryView(
    summary: RoomSummaryDetails,
    isSelected: Boolean,
    onSelection: (RoomSummaryDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { onSelection(summary) }
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp)
            .heightIn(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val roomAlias = summary.canonicalAlias ?: summary.roomId.value
        Avatar(
            avatarData = AvatarData(
                id = roomAlias,
                name = summary.name,
                url = summary.avatarURLString,
                size = AvatarSize.ForwardRoomListItem,
            ),
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                .weight(1f)
        ) {
            // Name
            Text(
                style = ElementTheme.typography.fontBodyLgRegular,
                text = summary.name,
                color = MaterialTheme.roomListRoomName(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // Id
            Text(
                text = roomAlias,
                color = MaterialTheme.roomListRoomMessage(),
                style = ElementTheme.typography.fontBodySmRegular,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        RadioButton(selected = isSelected, onClick = { onSelection(summary) })
    }
}

@Composable
private fun ForwardingErrorDialog(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    ErrorDialog(
        content = ErrorDialogDefaults.title,
        onDismiss = onDismiss,
        modifier = modifier,
    )
}

@PreviewsDayNight
@Composable
internal fun ForwardMessagesViewPreview(@PreviewParameter(ForwardMessagesStateProvider::class) state: ForwardMessagesState) = ElementPreview {
    ForwardMessagesView(
        state = state,
        onDismiss = {},
        onForwardingSucceeded = {}
    )
}
