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

package io.element.android.features.roomdetails.impl.rolesandpermissions.changeroles

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.roomdetails.impl.R
import io.element.android.features.roomdetails.impl.members.aRoomMember
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.async.AsyncIndicator
import io.element.android.libraries.designsystem.components.async.AsyncIndicatorHost
import io.element.android.libraries.designsystem.components.async.rememberAsyncIndicatorState
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.Checkbox
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.SearchBar
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.getBestName
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.SelectedUsersRowList
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeRolesView(
    state: ChangeRolesState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val updatedOnBackPressed by rememberUpdatedState(newValue = onBackPressed)
    BackHandler {
        if (state.isSearchActive) {
            state.eventSink(ChangeRolesEvent.ToggleSearchActive)
        } else {
            state.eventSink(ChangeRolesEvent.Exit)
        }
    }

    Box(modifier = modifier) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            topBar = {
                AnimatedVisibility(visible = !state.isSearchActive) {
                    TopAppBar(
                        title = {
                            val title = when (state.role) {
                                RoomMember.Role.ADMIN -> stringResource(R.string.screen_room_change_role_administrators_title)
                                RoomMember.Role.MODERATOR -> stringResource(R.string.screen_room_change_role_moderators_title)
                                RoomMember.Role.USER -> error("This should never be reached")
                            }
                            Text(
                                text = title,
                                style = ElementTheme.typography.aliasScreenTitle,
                            )
                        },
                        navigationIcon = {
                            BackButton(onClick = { state.eventSink(ChangeRolesEvent.Exit) })
                        },
                        actions = {
                            TextButton(
                                text = stringResource(CommonStrings.action_save),
                                enabled = state.hasPendingChanges,
                                onClick = { state.eventSink(ChangeRolesEvent.Save) }
                            )
                        }
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues),
            ) {
                val lazyListState = rememberLazyListState()
                SearchBar(
                    modifier = Modifier.padding(bottom = 16.dp),
                    placeHolderTitle = stringResource(CommonStrings.common_search_for_someone),
                    query = state.query.orEmpty(),
                    onQueryChange = { state.eventSink(ChangeRolesEvent.QueryChanged(it)) },
                    active = state.isSearchActive,
                    onActiveChange = { state.eventSink(ChangeRolesEvent.ToggleSearchActive) },
                    resultState = state.searchResults,
                ) { members ->
                    SearchResultsList(
                        lazyListState = lazyListState,
                        searchResults = members,
                        selectedUsers = state.selectedUsers,
                        canRemoveMember = state.canChangeMemberRole,
                        onSelectionToggled = { state.eventSink(ChangeRolesEvent.UserSelectionToggled(it)) },
                        selectedUsersList = {},
                    )
                }
                AnimatedVisibility(
                    visible = !state.isSearchActive,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column {
                        SearchResultsList(
                            lazyListState = lazyListState,
                            searchResults = (state.searchResults as? SearchBarResultState.Results)?.results ?: MembersByRole(emptyList()),
                            selectedUsers = state.selectedUsers,
                            canRemoveMember = state.canChangeMemberRole,
                            onSelectionToggled = { state.eventSink(ChangeRolesEvent.UserSelectionToggled(it)) },
                            selectedUsersList = { users ->
                                SelectedUsersRowList(
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                    selectedUsers = users,
                                    onUserRemoved = {
                                        state.eventSink(ChangeRolesEvent.UserSelectionToggled(aRoomMember(it.userId)))
                                    },
                                    canDeselect = { state.canChangeMemberRole(it.userId) },
                                )
                            }
                        )
                    }
                }
            }
        }

        val asyncIndicatorState = rememberAsyncIndicatorState()
        AsyncIndicatorHost(modifier = Modifier.statusBarsPadding(), asyncIndicatorState)

        AsyncActionView(
            async = state.exitState,
            onSuccess = { updatedOnBackPressed() },
            confirmationDialog = {
                ConfirmationDialog(
                    title = stringResource(CommonStrings.dialog_unsaved_changes_title),
                    content = stringResource(CommonStrings.dialog_unsaved_changes_description_android),
                    onSubmitClicked = { state.eventSink(ChangeRolesEvent.Exit) },
                    onDismiss = { state.eventSink(ChangeRolesEvent.CancelExit) }
                )
            },
            onErrorDismiss = { /* Cannot happen */ },
        )

        when (state.savingState) {
            is AsyncAction.Confirming -> {
                if (state.role == RoomMember.Role.ADMIN) {
                    // Confirm adding new admins dialogs
                    ConfirmationDialog(
                        title = stringResource(R.string.screen_room_change_role_confirm_add_admin_title),
                        content = stringResource(R.string.screen_room_change_role_confirm_add_admin_description),
                        onSubmitClicked = { state.eventSink(ChangeRolesEvent.Save) },
                        onDismiss = { state.eventSink(ChangeRolesEvent.ClearError) }
                    )
                }
            }
            is AsyncAction.Loading -> {
                ProgressDialog()
            }
            is AsyncAction.Failure -> {
                ErrorDialog(
                    content = stringResource(CommonStrings.error_unknown),
                    onDismiss = { state.eventSink(ChangeRolesEvent.ClearError) }
                )
            }
            is AsyncAction.Success -> {
                LaunchedEffect(state.savingState) {
                    asyncIndicatorState.enqueue(durationMs = AsyncIndicator.DURATION_SHORT) {
                        AsyncIndicator.Custom(text = stringResource(CommonStrings.common_saved_changes))
                    }
                }
            }
            else -> Unit
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchResultsList(
    searchResults: MembersByRole,
    selectedUsers: ImmutableList<MatrixUser>,
    canRemoveMember: (UserId) -> Boolean,
    onSelectionToggled: (RoomMember) -> Unit,
    lazyListState: LazyListState,
    selectedUsersList: @Composable (ImmutableList<MatrixUser>) -> Unit,
) {
    LazyColumn(
        state = lazyListState,
    ) {
        item {
            selectedUsersList(selectedUsers)
        }
        if (searchResults.admins.isNotEmpty()) {
            stickyHeader { ListSectionHeader(text = stringResource(R.string.screen_room_roles_and_permissions_admins)) }
            items(searchResults.admins, key = { it.userId }) { roomMember ->
                ListMemberItem(
                    roomMember = roomMember,
                    canRemoveMember = canRemoveMember,
                    onSelectionToggled = onSelectionToggled,
                    selectedUsers = selectedUsers
                )
            }
        }
        if (searchResults.moderators.isNotEmpty()) {
            stickyHeader { ListSectionHeader(text = stringResource(R.string.screen_room_roles_and_permissions_moderators)) }
            items(searchResults.moderators, key = { it.userId }) { roomMember ->
                ListMemberItem(
                    roomMember = roomMember,
                    canRemoveMember = canRemoveMember,
                    onSelectionToggled = onSelectionToggled,
                    selectedUsers = selectedUsers
                )
            }
        }
        if (searchResults.admins.isNotEmpty()) {
            stickyHeader { ListSectionHeader(text = stringResource(R.string.screen_room_member_list_mode_members)) }
            items(searchResults.members, key = { it.userId }) { roomMember ->
                ListMemberItem(
                    roomMember = roomMember,
                    canRemoveMember = canRemoveMember,
                    onSelectionToggled = onSelectionToggled,
                    selectedUsers = selectedUsers
                )
            }
        }
    }
}

@Composable
private fun ListSectionHeader(text: String) {
    Text(
        modifier = Modifier
            .background(ElementTheme.colors.bgCanvasDefault)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        text = text,
        style = ElementTheme.typography.fontBodyLgMedium,
    )
}

@Composable
private fun ListMemberItem(
    roomMember: RoomMember,
    canRemoveMember: (UserId) -> Boolean,
    onSelectionToggled: (RoomMember) -> Unit,
    selectedUsers: ImmutableList<MatrixUser>,
) {
    val canToggle = canRemoveMember(roomMember.userId)
    val trailingContent: @Composable (() -> Unit) = {
        Checkbox(
            checked = selectedUsers.any { it.userId == roomMember.userId },
            onCheckedChange = { onSelectionToggled(roomMember) },
            enabled = canToggle,
        )
    }
    MemberRow(
        modifier = Modifier.clickable(enabled = canToggle, onClick = { onSelectionToggled(roomMember) }),
        avatarData = AvatarData(roomMember.userId.value, roomMember.displayName, roomMember.avatarUrl, AvatarSize.UserListItem),
        name = roomMember.getBestName(),
        userId = roomMember.userId.value.takeIf { roomMember.displayName?.isNotBlank() == true },
        isPending = roomMember.membership == RoomMembershipState.INVITE,
        trailingContent = trailingContent,
    )
}

@Composable
internal fun MemberRow(
    avatarData: AvatarData,
    name: String,
    userId: String?,
    isPending: Boolean,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(avatarData)
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f),
        ) {
            Row (verticalAlignment = Alignment.CenterVertically) {
                // Name
                Text(
                    modifier = Modifier.clipToBounds(),
                    text = name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary,
                    style = ElementTheme.typography.fontBodyLgRegular,
                )
                if (isPending) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = " - Pending",
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            // Id
            userId?.let {
                Text(
                    text = userId,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = ElementTheme.typography.fontBodySmRegular,
                )
            }
        }
        trailingContent?.invoke()
    }
}

@PreviewsDayNight
@Composable
internal fun ChangeRolesViewPreview(@PreviewParameter(ChangeRolesStateProvider::class) state: ChangeRolesState) {
    ElementPreview {
        ChangeRolesView(
            state = state,
            onBackPressed = {},
        )
    }
}
