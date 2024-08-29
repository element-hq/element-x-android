/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.createroom.impl.userlist.UserListEvents
import io.element.android.features.createroom.impl.userlist.UserListState
import io.element.android.features.createroom.impl.userlist.UserListStateProvider
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.ListSectionHeader
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.CheckableUserRow
import io.element.android.libraries.matrix.ui.components.CheckableUserRowData
import io.element.android.libraries.matrix.ui.components.SelectedUsersRowList
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.matrix.ui.model.getBestName
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun UserListView(
    state: UserListState,
    onSelectUser: (MatrixUser) -> Unit,
    onDeselectUser: (MatrixUser) -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
) {
    Column(
        modifier = modifier,
    ) {
        SearchUserBar(
            isDebugBuild = state.isDebugBuild,
            modifier = Modifier.fillMaxWidth(),
            query = state.searchQuery,
            state = state.searchResults,
            selectedUsers = state.selectedUsers,
            active = state.isSearchActive,
            showLoader = state.showSearchLoader,
            isMultiSelectionEnable = state.isMultiSelectionEnabled,
            showBackButton = showBackButton,
            onActiveChange = { state.eventSink(UserListEvents.OnSearchActiveChanged(it)) },
            onTextChange = { state.eventSink(UserListEvents.UpdateSearchQuery(it)) },
            onUserSelect = {
                state.eventSink(UserListEvents.AddToSelection(it))
                onSelectUser(it)
            },
            onUserDeselect = {
                state.eventSink(UserListEvents.RemoveFromSelection(it))
                onDeselectUser(it)
            },
        )

        if (state.isMultiSelectionEnabled && !state.isSearchActive && state.selectedUsers.isNotEmpty()) {
            SelectedUsersRowList(
                contentPadding = PaddingValues(16.dp),
                selectedUsers = state.selectedUsers,
                autoScroll = true,
                onUserRemove = {
                    state.eventSink(UserListEvents.RemoveFromSelection(it))
                    onDeselectUser(it)
                },
            )
        }
        if (!state.isSearchActive && state.recentDirectRooms.isNotEmpty()) {
            LazyColumn {
                item {
                    ListSectionHeader(
                        title = stringResource(id = CommonStrings.common_suggestions),
                        hasDivider = false,
                    )
                }
                state.recentDirectRooms.forEachIndexed { index, recentDirectRoom ->
                    item {
                        val isSelected = state.selectedUsers.any {
                            recentDirectRoom.matrixUser.userId == it.userId
                        }
                        CheckableUserRow(
                            isDebugBuild = state.isDebugBuild,
                            checked = isSelected,
                            onCheckedChange = {
                                if (isSelected) {
                                    state.eventSink(UserListEvents.RemoveFromSelection(recentDirectRoom.matrixUser))
                                    onDeselectUser(recentDirectRoom.matrixUser)
                                } else {
                                    state.eventSink(UserListEvents.AddToSelection(recentDirectRoom.matrixUser))
                                    onSelectUser(recentDirectRoom.matrixUser)
                                }
                            },
                            data = CheckableUserRowData.Resolved(
                                avatarData = recentDirectRoom.matrixUser.getAvatarData(AvatarSize.UserListItem),
                                name = recentDirectRoom.matrixUser.getBestName(),
                                subtext = recentDirectRoom.matrixUser.userId.value,
                            ),
                        )
                        if (index < state.recentDirectRooms.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun UserListViewPreview(@PreviewParameter(UserListStateProvider::class) state: UserListState) = ElementPreview {
    UserListView(
        state = state,
        onSelectUser = {},
        onDeselectUser = {},
    )
}
