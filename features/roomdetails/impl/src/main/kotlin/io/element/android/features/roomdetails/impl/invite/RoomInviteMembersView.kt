/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.roomdetails.impl.R
import io.element.android.libraries.designsystem.components.async.AsyncLoading
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
import io.element.android.libraries.matrix.ui.components.CheckableUserRow
import io.element.android.libraries.matrix.ui.components.CheckableUserRowData
import io.element.android.libraries.matrix.ui.components.SelectedUsersRowList
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.matrix.ui.model.getBestName
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@Composable
fun RoomInviteMembersView(
    state: RoomInviteMembersState,
    onBackClick: () -> Unit,
    onSubmitClick: (List<MatrixUser>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            RoomInviteMembersTopBar(
                onBackClick = {
                    if (state.isSearchActive) {
                        state.eventSink(RoomInviteMembersEvents.OnSearchActiveChanged(false))
                    } else {
                        onBackClick()
                    }
                },
                onSubmitClick = { onSubmitClick(state.selectedUsers) },
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
                isDebugBuild = state.isDebugBuild,
                modifier = Modifier.fillMaxWidth(),
                query = state.searchQuery,
                showLoader = state.showSearchLoader,
                selectedUsers = state.selectedUsers,
                state = state.searchResults,
                active = state.isSearchActive,
                onActiveChange = { state.eventSink(RoomInviteMembersEvents.OnSearchActiveChanged(it)) },
                onTextChange = { state.eventSink(RoomInviteMembersEvents.UpdateSearchQuery(it)) },
                onToggleUser = { state.eventSink(RoomInviteMembersEvents.ToggleUser(it)) },
            )

            if (!state.isSearchActive) {
                SelectedUsersRowList(
                    modifier = Modifier.fillMaxWidth(),
                    selectedUsers = state.selectedUsers,
                    autoScroll = true,
                    onUserRemove = { state.eventSink(RoomInviteMembersEvents.ToggleUser(it)) },
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
    onBackClick: () -> Unit,
    onSubmitClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.screen_room_details_invite_people_title),
                style = ElementTheme.typography.aliasScreenTitle,
            )
        },
        navigationIcon = { BackButton(onClick = onBackClick) },
        actions = {
            TextButton(
                text = stringResource(CommonStrings.action_invite),
                onClick = onSubmitClick,
                enabled = canSend,
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomInviteMembersSearchBar(
    isDebugBuild: Boolean,
    query: String,
    state: SearchBarResultState<ImmutableList<InvitableUser>>,
    showLoader: Boolean,
    selectedUsers: ImmutableList<MatrixUser>,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onToggleUser: (MatrixUser) -> Unit,
    modifier: Modifier = Modifier,
    placeHolderTitle: String = stringResource(CommonStrings.common_search_for_someone),
) {
    SearchBar(
        query = query,
        onQueryChange = onTextChange,
        active = active,
        onActiveChange = onActiveChange,
        modifier = modifier,
        placeHolderTitle = placeHolderTitle,
        contentPrefix = {
            if (selectedUsers.isNotEmpty()) {
                SelectedUsersRowList(
                    modifier = Modifier.fillMaxWidth(),
                    selectedUsers = selectedUsers,
                    autoScroll = true,
                    onUserRemove = onToggleUser,
                    contentPadding = PaddingValues(16.dp),
                )
            }
        },
        showBackButton = false,
        resultState = state,
        contentSuffix = {
            if (showLoader) {
                AsyncLoading()
            }
        },
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
                    val notInvitedOrJoined = !(invitableUser.isAlreadyInvited || invitableUser.isAlreadyJoined)
                    val isUnresolved = invitableUser.isUnresolved && notInvitedOrJoined
                    val enabled = isUnresolved || notInvitedOrJoined
                    val data = if (isUnresolved) {
                        CheckableUserRowData.Unresolved(
                            avatarData = invitableUser.matrixUser.getAvatarData(AvatarSize.UserListItem),
                            id = invitableUser.matrixUser.userId.value,
                        )
                    } else {
                        CheckableUserRowData.Resolved(
                            avatarData = invitableUser.matrixUser.getAvatarData(AvatarSize.UserListItem),
                            name = invitableUser.matrixUser.getBestName(),
                            subtext = when {
                                // If they're already invited or joined we show that information
                                invitableUser.isAlreadyJoined -> stringResource(R.string.screen_room_details_already_a_member)
                                invitableUser.isAlreadyInvited -> stringResource(R.string.screen_room_details_already_invited)
                                // Otherwise show the ID, unless that's already used for their name
                                invitableUser.matrixUser.displayName.isNullOrEmpty().not() -> invitableUser.matrixUser.userId.value
                                else -> null
                            }
                        )
                    }
                    CheckableUserRow(
                        isDebugBuild = isDebugBuild,
                        checked = invitableUser.isSelected,
                        enabled = enabled,
                        data = data,
                        onCheckedChange = { onToggleUser(invitableUser.matrixUser) },
                        modifier = Modifier.fillMaxWidth()
                    )

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
internal fun RoomInviteMembersViewPreview(@PreviewParameter(RoomInviteMembersStateProvider::class) state: RoomInviteMembersState) = ElementPreview {
    RoomInviteMembersView(
        state = state,
        onBackClick = {},
        onSubmitClick = {},
    )
}
