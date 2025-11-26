/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rolesandpermissions.impl.roles

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.rolesandpermissions.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.async.AsyncIndicatorHost
import io.element.android.libraries.designsystem.components.async.rememberAsyncIndicatorState
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.dialogs.SaveChangesDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Checkbox
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.SearchBar
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.getBestName
import io.element.android.libraries.matrix.api.room.toMatrixUser
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.SelectedUsersRowList
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeRolesView(
    state: ChangeRolesState,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = !state.isSearchActive) {
        state.eventSink(ChangeRolesEvent.Exit)
    }
    Box(modifier = modifier) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            topBar = {
                AnimatedVisibility(visible = !state.isSearchActive) {
                    TopAppBar(
                        titleStr = when (state.role) {
                            is RoomMember.Role.Owner -> stringResource(R.string.screen_room_change_role_owners_title)
                            RoomMember.Role.Admin -> stringResource(R.string.screen_room_change_role_administrators_title)
                            RoomMember.Role.Moderator -> stringResource(R.string.screen_room_change_role_moderators_title)
                            RoomMember.Role.User -> error("This should never be reached")
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    placeHolderTitle = stringResource(CommonStrings.common_search_for_someone),
                    query = state.query.orEmpty(),
                    onQueryChange = { state.eventSink(ChangeRolesEvent.QueryChanged(it)) },
                    active = state.isSearchActive,
                    onActiveChange = { state.eventSink(ChangeRolesEvent.ToggleSearchActive) },
                    resultState = state.searchResults,
                ) { members ->
                    SearchResultsList(
                        currentRole = state.role,
                        lazyListState = lazyListState,
                        searchResults = members,
                        selectedUsers = state.selectedUsers,
                        canRemoveMember = state.canChangeMemberRole,
                        onToggleSelection = { state.eventSink(ChangeRolesEvent.UserSelectionToggled(it.toMatrixUser())) },
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
                            currentRole = state.role,
                            lazyListState = lazyListState,
                            searchResults = (state.searchResults as? SearchBarResultState.Results)?.results ?: MembersByRole(),
                            selectedUsers = state.selectedUsers,
                            canRemoveMember = state.canChangeMemberRole,
                            onToggleSelection = { state.eventSink(ChangeRolesEvent.UserSelectionToggled(it.toMatrixUser())) },
                            selectedUsersList = { users ->
                                SelectedUsersRowList(
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                    selectedUsers = users,
                                    onUserRemove = {
                                        state.eventSink(ChangeRolesEvent.UserSelectionToggled(it))
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
            async = state.savingState,
            onSuccess = {},
            confirmationDialog = { confirming ->
                when (confirming) {
                    is AsyncAction.ConfirmingCancellation -> {
                        SaveChangesDialog(
                            onSubmitClick = { state.eventSink(ChangeRolesEvent.Exit) },
                            onDismiss = { state.eventSink(ChangeRolesEvent.CloseDialog) }
                        )
                    }
                    is ConfirmingModifyingOwners -> {
                        ConfirmationDialog(
                            title = stringResource(R.string.screen_room_change_role_confirm_change_owners_title),
                            content = stringResource(R.string.screen_room_change_role_confirm_change_owners_description),
                            submitText = stringResource(CommonStrings.action_continue),
                            onSubmitClick = { state.eventSink(ChangeRolesEvent.Save) },
                            onDismiss = { state.eventSink(ChangeRolesEvent.CloseDialog) },
                            destructiveSubmit = true,
                        )
                    }
                    is ConfirmingModifyingAdmins -> {
                        ConfirmationDialog(
                            title = stringResource(R.string.screen_room_change_role_confirm_add_admin_title),
                            content = stringResource(R.string.screen_room_change_role_confirm_add_admin_description),
                            onSubmitClick = { state.eventSink(ChangeRolesEvent.Save) },
                            onDismiss = { state.eventSink(ChangeRolesEvent.CloseDialog) }
                        )
                    }
                }
            },
            errorMessage = {
                stringResource(CommonStrings.error_unknown)
            },
            onErrorDismiss = {
                state.eventSink(ChangeRolesEvent.CloseDialog)
            },
        )
    }
}

@Composable
private fun SearchResultsList(
    currentRole: RoomMember.Role,
    searchResults: MembersByRole,
    selectedUsers: ImmutableList<MatrixUser>,
    canRemoveMember: (UserId) -> Boolean,
    onToggleSelection: (RoomMember) -> Unit,
    lazyListState: LazyListState,
    selectedUsersList: @Composable (ImmutableList<MatrixUser>) -> Unit,
) {
    LazyColumn(
        state = lazyListState,
    ) {
        item {
            selectedUsersList(selectedUsers)
        }
        if (searchResults.owners.isNotEmpty()) {
            stickyHeader { ListSectionHeader(text = stringResource(R.string.screen_room_roles_and_permissions_owners)) }
            item {
                Text(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    text = stringResource(R.string.screen_room_change_role_moderators_owner_section_footer),
                    color = ElementTheme.colors.textSecondary,
                    style = ElementTheme.typography.fontBodySmRegular,
                )
            }
            items(searchResults.owners, key = { it.userId }) { roomMember ->
                ListMemberItem(
                    roomMember = roomMember,
                    canRemoveMember = canRemoveMember,
                    onToggleSelection = onToggleSelection,
                    selectedUsers = selectedUsers
                )
            }
        }
        if (searchResults.admins.isNotEmpty()) {
            stickyHeader { ListSectionHeader(text = stringResource(R.string.screen_room_roles_and_permissions_admins)) }
            // Add a footer for the admin section in change role to moderator screen
            if (currentRole == RoomMember.Role.Moderator) {
                item {
                    Text(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                        text = stringResource(R.string.screen_room_change_role_moderators_admin_section_footer),
                        color = ElementTheme.colors.textSecondary,
                        style = ElementTheme.typography.fontBodySmRegular,
                    )
                }
            }
            items(searchResults.admins, key = { it.userId }) { roomMember ->
                ListMemberItem(
                    roomMember = roomMember,
                    canRemoveMember = canRemoveMember,
                    onToggleSelection = onToggleSelection,
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
                    onToggleSelection = onToggleSelection,
                    selectedUsers = selectedUsers
                )
            }
        }
        if (searchResults.members.isNotEmpty()) {
            stickyHeader { ListSectionHeader(text = stringResource(R.string.screen_room_member_list_mode_members)) }
            items(searchResults.members, key = { it.userId }) { roomMember ->
                ListMemberItem(
                    roomMember = roomMember,
                    canRemoveMember = canRemoveMember,
                    onToggleSelection = onToggleSelection,
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
    onToggleSelection: (RoomMember) -> Unit,
    selectedUsers: ImmutableList<MatrixUser>,
) {
    val canToggle = canRemoveMember(roomMember.userId)
    val trailingContent: @Composable (() -> Unit) = {
        if (canToggle) {
            Checkbox(
                checked = selectedUsers.any { it.userId == roomMember.userId },
                onCheckedChange = { onToggleSelection(roomMember) },
            )
        }
    }
    Column {
        MemberRow(
            modifier = Modifier.clickable(enabled = canToggle, onClick = { onToggleSelection(roomMember) }),
            avatarData = roomMember.getAvatarData(size = AvatarSize.UserListItem),
            name = roomMember.getBestName(),
            userId = roomMember.userId.value.takeIf { roomMember.displayName?.isNotBlank() == true },
            isPending = roomMember.membership == RoomMembershipState.INVITE,
            trailingContent = trailingContent,
        )
        HorizontalDivider()
    }
}

@Composable
private fun MemberRow(
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
        Avatar(
            avatarData = avatarData,
            avatarType = AvatarType.User,
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Name
                Text(
                    modifier = Modifier.weight(1f, fill = false),
                    text = name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = ElementTheme.colors.textPrimary,
                    style = ElementTheme.typography.fontBodyLgRegular,
                )
                // Invitation pending marker
                if (isPending) {
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(id = R.string.screen_room_member_list_pending_status),
                        style = ElementTheme.typography.fontBodySmRegular.copy(fontStyle = FontStyle.Italic),
                        color = ElementTheme.colors.textSecondary
                    )
                }
            }
            // Id
            userId?.let {
                Text(
                    text = userId,
                    color = ElementTheme.colors.textSecondary,
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
            state = state
        )
    }
}

@PreviewsDayNight
@Composable
internal fun PendingMemberRowWithLongNamePreview() {
    ElementPreview(
        drawableFallbackForImages = CommonDrawables.sample_avatar,
    ) {
        MemberRow(
            avatarData = AvatarData("userId", "A very long name that should be truncated", "https://example.com/avatar.png", AvatarSize.UserListItem),
            name = "A very long name that should be truncated",
            userId = "@alice:matrix.org",
            isPending = true,
            trailingContent = {
                Checkbox(
                    checked = true,
                    onCheckedChange = {},
                    enabled = true,
                )
            }
        )
    }
}
