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

package io.element.android.libraries.roomselect.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.RadioButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.SearchBar
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomlist.RoomSummaryDetails
import io.element.android.libraries.matrix.ui.components.SelectedRoom
import io.element.android.libraries.roomselect.api.RoomSelectMode
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomSelectView(
    state: RoomSelectState,
    onDismiss: () -> Unit,
    onSubmit: (List<RoomId>) -> Unit,
    modifier: Modifier = Modifier,
) {
    @Suppress("UNUSED_PARAMETER")
    fun onRoomRemoved(roomSummaryDetails: RoomSummaryDetails) {
        // TODO toggle selection when multi-selection is enabled
        state.eventSink(RoomSelectEvents.RemoveSelectedRoom)
    }

    @Composable
    fun SelectedRoomsHelper(isForwarding: Boolean, selectedRooms: ImmutableList<RoomSummaryDetails>) {
        if (isForwarding) return
        SelectedRooms(
            selectedRooms = selectedRooms,
            onRemoveRoom = ::onRoomRemoved,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }

    fun onBackButton(state: RoomSelectState) {
        if (state.isSearchActive) {
            state.eventSink(RoomSelectEvents.ToggleSearchActive)
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
                        text = when (state.mode) {
                            RoomSelectMode.Forward -> stringResource(CommonStrings.common_forward_message)
                            RoomSelectMode.Share -> stringResource(CommonStrings.common_send_to)
                        },
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
                        onClick = { onSubmit(state.selectedRooms.map { it.roomId }) }
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
                modifier = Modifier.fillMaxWidth(),
                placeHolderTitle = stringResource(CommonStrings.action_search),
                query = state.query,
                onQueryChange = { state.eventSink(RoomSelectEvents.UpdateQuery(it)) },
                active = state.isSearchActive,
                onActiveChange = { state.eventSink(RoomSelectEvents.ToggleSearchActive) },
                resultState = state.resultState,
                showBackButton = false,
            ) { summaries ->
                LazyColumn {
                    item {
                        SelectedRoomsHelper(
                            // TODO state.isForwarding
                            isForwarding = false,
                            selectedRooms = state.selectedRooms
                        )
                    }
                    items(summaries, key = { it.roomId.value }) { roomSummary ->
                        Column {
                            RoomSummaryView(
                                roomSummary,
                                isSelected = state.selectedRooms.any { it.roomId == roomSummary.roomId },
                                onSelection = { roomSummary ->
                                    state.eventSink(RoomSelectEvents.SetSelectedRoom(roomSummary))
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
                                        state.eventSink(RoomSelectEvents.SetSelectedRoom(roomSummary))
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedRooms(
    selectedRooms: ImmutableList<RoomSummaryDetails>,
    onRemoveRoom: (RoomSummaryDetails) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        items(selectedRooms, key = { it.roomId.value }) { roomSummary ->
            SelectedRoom(roomSummary = roomSummary, onRemoveRoom = onRemoveRoom)
        }
    }
}

@Composable
private fun RoomSummaryView(
    summary: RoomSummaryDetails,
    isSelected: Boolean,
    onSelection: (RoomSummaryDetails) -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable { onSelection(summary) }
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp)
            .heightIn(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            avatarData = AvatarData(
                id = summary.roomId.value,
                name = summary.name,
                url = summary.avatarUrl,
                size = AvatarSize.RoomSelectRoomListItem,
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
                text = summary.name ?: stringResource(id = CommonStrings.common_no_room_name),
                fontStyle = FontStyle.Italic.takeIf { summary.name == null },
                color = ElementTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // Alias
            summary.canonicalAlias?.let { alias ->
                Text(
                    text = alias.value,
                    color = ElementTheme.colors.textSecondary,
                    style = ElementTheme.typography.fontBodySmRegular,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        RadioButton(selected = isSelected, onClick = { onSelection(summary) })
    }
}

@PreviewsDayNight
@Composable
internal fun RoomSelectViewPreview(@PreviewParameter(RoomSelectStateProvider::class) state: RoomSelectState) = ElementPreview {
    RoomSelectView(
        state = state,
        onDismiss = {},
        onSubmit = {},
    )
}
