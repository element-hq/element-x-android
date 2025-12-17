/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invitepeople.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.async.AsyncFailure
import io.element.android.libraries.designsystem.components.async.AsyncLoading
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.SearchBar
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.CheckableUserRow
import io.element.android.libraries.matrix.ui.components.CheckableUserRowData
import io.element.android.libraries.matrix.ui.components.SelectedUsersRowList
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.matrix.ui.model.getBestName
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@Composable
fun InvitePeopleView(
    state: DefaultInvitePeopleState,
    modifier: Modifier = Modifier,
) {
    when (state.room) {
        is AsyncData.Failure -> InvitePeopleViewError(state.room.error, modifier)
        AsyncData.Uninitialized,
        is AsyncData.Loading,
        is AsyncData.Success -> InvitePeopleContentView(state, modifier)
    }
}

@Composable
private fun InvitePeopleViewError(
    error: Throwable,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AsyncFailure(
            throwable = error,
            onRetry = null,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Composable
private fun InvitePeopleContentView(
    state: DefaultInvitePeopleState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        InvitePeopleSearchBar(
            modifier = Modifier.fillMaxWidth(),
            query = state.searchQuery,
            showLoader = state.showSearchLoader,
            selectedUsers = state.selectedUsers,
            state = state.searchResults,
            active = state.isSearchActive,
            onActiveChange = {
                state.eventSink(
                    DefaultInvitePeopleEvents.OnSearchActiveChanged(
                        it
                    )
                )
            },
            onTextChange = { state.eventSink(DefaultInvitePeopleEvents.UpdateSearchQuery(it)) },
            onToggleUser = { state.eventSink(DefaultInvitePeopleEvents.ToggleUser(it)) },
        )

        if (!state.isSearchActive) {
            SelectedUsersRowList(
                modifier = Modifier.fillMaxWidth(),
                selectedUsers = state.selectedUsers,
                autoScroll = true,
                onUserRemove = { state.eventSink(DefaultInvitePeopleEvents.ToggleUser(it)) },
                contentPadding = PaddingValues(16.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvitePeopleSearchBar(
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
                    val invitedOrJoined = invitableUser.isAlreadyInvited || invitableUser.isAlreadyJoined
                    val isUnresolved = invitableUser.isUnresolved && !invitedOrJoined
                    val enabled = isUnresolved || !invitedOrJoined
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
                                invitableUser.isAlreadyJoined -> stringResource(R.string.screen_invite_users_already_a_member)
                                invitableUser.isAlreadyInvited -> stringResource(R.string.screen_invite_users_already_invited)
                                // Otherwise show the ID, unless that's already used for their name
                                invitableUser.matrixUser.displayName.isNullOrEmpty()
                                    .not() -> invitableUser.matrixUser.userId.value
                                else -> null
                            }
                        )
                    }
                    CheckableUserRow(
                        checked = invitableUser.isSelected || invitedOrJoined,
                        enabled = enabled,
                        data = data,
                        onCheckedChange = { onToggleUser(invitableUser.matrixUser) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
    )
}

@PreviewsDayNight
@Composable
internal fun InvitePeopleViewPreview(@PreviewParameter(DefaultInvitePeopleStateProvider::class) state: DefaultInvitePeopleState) =
    ElementPreview {
        InvitePeopleView(state = state)
    }
