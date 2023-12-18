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

package io.element.android.features.roomdetails.impl.invite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.roomdetails.impl.R
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.SearchBar
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.CheckableUnresolvedUserRow
import io.element.android.libraries.matrix.ui.components.CheckableUserRow
import io.element.android.libraries.matrix.ui.components.SelectedUsersList
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.matrix.ui.model.getBestName
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@Composable
fun RoomInviteMembersView(
    state: RoomInviteMembersState,
    onBackPressed: () -> Unit,
    onSubmitPressed: (List<MatrixUser>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            RoomInviteMembersTopBar(
                onBackPressed = {
                    if (state.isSearchActive) {
                        state.eventSink(RoomInviteMembersEvents.OnSearchActiveChanged(false))
                    } else {
                        onBackPressed()
                    }
                },
                onSubmitPressed = { onSubmitPressed(state.selectedUsers) },
                canSend = state.canInvite,
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .consumeWindowInsets(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RoomInviteMembersSearchBar(
                modifier = Modifier.fillMaxWidth(),
                query = state.searchQuery,
                selectedUsers = state.selectedUsers,
                state = state.searchResults,
                active = state.isSearchActive,
                onActiveChanged = { state.eventSink(RoomInviteMembersEvents.OnSearchActiveChanged(it)) },
                onTextChanged = { state.eventSink(RoomInviteMembersEvents.UpdateSearchQuery(it)) },
                onUserToggled = { state.eventSink(RoomInviteMembersEvents.ToggleUser(it)) },
            )

            if (!state.isSearchActive) {
                SelectedUsersList(
                    modifier = Modifier.fillMaxWidth(),
                    selectedUsers = state.selectedUsers,
                    autoScroll = true,
                    onUserRemoved = { state.eventSink(RoomInviteMembersEvents.ToggleUser(it)) },
                    contentPadding = PaddingValues(16.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomInviteMembersTopBar(
    canSend: Boolean,
    onBackPressed: () -> Unit,
    onSubmitPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.screen_room_details_invite_people_title),
                style = ElementTheme.typography.aliasScreenTitle,
            )
        },
        navigationIcon = { BackButton(onClick = onBackPressed) },
        actions = {
            TextButton(
                text = stringResource(CommonStrings.action_invite),
                onClick = onSubmitPressed,
                enabled = canSend,
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomInviteMembersSearchBar(
    query: String,
    state: SearchBarResultState<ImmutableList<InvitableUser>>,
    selectedUsers: ImmutableList<MatrixUser>,
    active: Boolean,
    onActiveChanged: (Boolean) -> Unit,
    onTextChanged: (String) -> Unit,
    onUserToggled: (MatrixUser) -> Unit,
    modifier: Modifier = Modifier,
    placeHolderTitle: String = stringResource(CommonStrings.common_search_for_someone),
) {
    SearchBar(
        query = query,
        onQueryChange = onTextChanged,
        active = active,
        onActiveChange = onActiveChanged,
        modifier = modifier,
        placeHolderTitle = placeHolderTitle,
        contentPrefix = {
            if (selectedUsers.isNotEmpty()) {
                SelectedUsersList(
                    modifier = Modifier.fillMaxWidth(),
                    selectedUsers = selectedUsers,
                    autoScroll = true,
                    onUserRemoved = onUserToggled,
                    contentPadding = PaddingValues(16.dp),
                )
            }
        },
        showBackButton = false,
        resultState = state,
        resultHandler = { results ->
            Text(
                text = stringResource(id = CommonStrings.common_search_results),
                style = ElementTheme.typography.fontBodyLgMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 8.dp)
            )

            LazyColumn {
                itemsIndexed(results) { index, invitableUser ->
                    if (invitableUser.isUnresolved && !invitableUser.isAlreadyInvited && !invitableUser.isAlreadyJoined) {
                        CheckableUnresolvedUserRow(
                            checked = invitableUser.isSelected,
                            avatarData = invitableUser.matrixUser.getAvatarData(AvatarSize.UserListItem),
                            id = invitableUser.matrixUser.userId.value,
                            onCheckedChange = { onUserToggled(invitableUser.matrixUser) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        CheckableUserRow(
                            checked = invitableUser.isSelected,
                            enabled = !invitableUser.isAlreadyInvited && !invitableUser.isAlreadyJoined,
                            avatarData = invitableUser.matrixUser.getAvatarData(AvatarSize.UserListItem),
                            name = invitableUser.matrixUser.getBestName(),
                            subtext = when {
                                // If they're already invited or joined we show that information
                                invitableUser.isAlreadyJoined -> stringResource(R.string.screen_room_details_already_a_member)
                                invitableUser.isAlreadyInvited -> stringResource(R.string.screen_room_details_already_invited)
                                // Otherwise show the ID, unless that's already used for their name
                                invitableUser.matrixUser.displayName.isNullOrEmpty().not() -> invitableUser.matrixUser.userId.value
                                else -> null
                            },
                            onCheckedChange = { onUserToggled(invitableUser.matrixUser) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (index < results.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }
        },
    )
}

@PreviewsDayNight
@Composable
internal fun RoomInviteMembersPreview(@PreviewParameter(RoomInviteMembersStateProvider::class) state: RoomInviteMembersState) = ElementPreview {
    RoomInviteMembersView(
        state = state,
        onBackPressed = {},
        onSubmitPressed = {},
    )
}
