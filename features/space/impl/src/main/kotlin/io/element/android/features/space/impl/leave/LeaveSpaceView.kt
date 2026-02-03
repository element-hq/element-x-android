/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.space.impl.leave

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.space.impl.R
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.async.AsyncFailure
import io.element.android.libraries.designsystem.components.async.AsyncLoading
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Checkbox
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.ui.strings.CommonPlurals
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * https://www.figma.com/design/kcnHxunG1LDWXsJhaNuiHz/ER-145--Spaces-on-Element-X?node-id=3947-68767&t=GTf1cLkAf6UCQDan-0
 */
@Composable
fun LeaveSpaceView(
    state: LeaveSpaceState,
    onCancel: () -> Unit,
    onRolesAndPermissionsClick: () -> Unit,
    onChooseOwnersClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HeaderFooterPage(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 14.dp),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(onClick = onCancel)
                },
                title = {},
            )
        },
        header = {
            LeaveSpaceHeader(state = state)
        },
        footer = {
            LeaveSpaceButtons(
                showLeaveButton = state.showLeaveButton,
                selectedRoomsCount = state.selectedRoomsCount,
                onLeaveSpace = {
                    state.eventSink(LeaveSpaceEvents.LeaveSpace)
                },
                onCancel = onCancel,
                showRolesAndPermissionsButton = state.needsOwnerChange && !state.areCreatorsPrivileged,
                showChooseOwnersButton = state.needsOwnerChange && state.areCreatorsPrivileged,
                onChooseOwnersButtonClick = onChooseOwnersClick,
                onRolesAndPermissionsClick = onRolesAndPermissionsClick,
            )
        },
        content = {
            if (state.needsOwnerChange.not()) {
                LazyColumn(
                    modifier = Modifier.padding(top = 20.dp),
                ) {
                    when (state.selectableSpaceRooms) {
                        is AsyncData.Success -> {
                            // List rooms where the user is the only admin
                            state.selectableSpaceRooms.data.forEach { selectableSpaceRoom ->
                                item {
                                    SpaceItem(
                                        selectableSpaceRoom = selectableSpaceRoom,
                                        showCheckBox = state.hasOnlyLastAdminRoom.not(),
                                        onClick = {
                                            state.eventSink(LeaveSpaceEvents.ToggleRoomSelection(selectableSpaceRoom.spaceRoom.roomId))
                                        }
                                    )
                                }
                            }
                        }
                        is AsyncData.Failure -> item {
                            AsyncFailure(
                                throwable = state.selectableSpaceRooms.error,
                                onRetry = {
                                    state.eventSink(LeaveSpaceEvents.Retry)
                                },
                            )
                        }
                        is AsyncData.Loading,
                        AsyncData.Uninitialized -> item {
                            AsyncLoading()
                        }
                    }
                }
            }
        }
    )

    AsyncActionView(
        async = state.leaveSpaceAction,
        onSuccess = { /* Nothing to do, the screen will be dismissed automatically */ },
        errorMessage = { stringResource(CommonStrings.error_unknown) },
        onErrorDismiss = { state.eventSink(LeaveSpaceEvents.CloseError) },
    )
}

@Composable
private fun LeaveSpaceHeader(
    state: LeaveSpaceState,
) {
    Column {
        IconTitleSubtitleMolecule(
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp, start = 24.dp, end = 24.dp),
            iconStyle = BigIcon.Style.AlertSolid,
            title = if (state.needsOwnerChange) {
                if (state.areCreatorsPrivileged) {
                    stringResource(R.string.screen_leave_space_title_last_owner)
                } else {
                    stringResource(R.string.screen_leave_space_title_last_admin, state.spaceName ?: stringResource(CommonStrings.common_space))
                }
            } else {
                stringResource(R.string.screen_leave_space_title, state.spaceName ?: stringResource(CommonStrings.common_space))
            },
            subTitle =
                if (state.needsOwnerChange) {
                    if (state.areCreatorsPrivileged) {
                        stringResource(R.string.screen_leave_space_subtitle_last_owner, state.spaceName ?: stringResource(CommonStrings.common_space))
                    } else {
                        stringResource(R.string.screen_leave_space_subtitle_last_admin)
                    }
                } else if (state.selectableSpaceRooms is AsyncData.Success && state.selectableSpaceRooms.data.isNotEmpty()) {
                    if (state.hasOnlyLastAdminRoom) {
                        stringResource(R.string.screen_leave_space_subtitle_only_last_admin)
                    } else {
                        stringResource(R.string.screen_leave_space_subtitle)
                    }
                } else {
                    null
                },
        )
        if (state.showQuickAction) {
            if (state.areAllSelected) {
                QuickActionButton(CommonStrings.action_deselect_all) {
                    state.eventSink(LeaveSpaceEvents.DeselectAllRooms)
                }
            } else {
                QuickActionButton(resId = CommonStrings.action_select_all) {
                    state.eventSink(LeaveSpaceEvents.SelectAllRooms)
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.QuickActionButton(
    @StringRes resId: Int,
    onClick: () -> Unit,
) {
    Text(
        modifier = Modifier
            .align(Alignment.End)
            .padding(end = 8.dp)
            .clickable(onClick = onClick)
            .padding(8.dp),
        text = stringResource(resId),
        color = ElementTheme.colors.textActionPrimary,
        style = ElementTheme.typography.fontBodyMdMedium,
    )
}

@Composable
private fun LeaveSpaceButtons(
    showLeaveButton: Boolean,
    selectedRoomsCount: Int,
    onLeaveSpace: () -> Unit,
    showRolesAndPermissionsButton: Boolean,
    onRolesAndPermissionsClick: () -> Unit,
    showChooseOwnersButton: Boolean,
    onChooseOwnersButtonClick: () -> Unit,
    onCancel: () -> Unit,
) {
    ButtonColumnMolecule(
        modifier = Modifier.padding(top = 16.dp)
    ) {
        if (showLeaveButton) {
            val text = if (selectedRoomsCount > 0) {
                pluralStringResource(R.plurals.screen_leave_space_submit, selectedRoomsCount, selectedRoomsCount)
            } else {
                stringResource(CommonStrings.action_leave_space)
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = text,
                leadingIcon = IconSource.Vector(CompoundIcons.Leave()),
                onClick = onLeaveSpace,
                destructive = true,
            )
        }
        if (showRolesAndPermissionsButton) {
            Button(
                text = stringResource(CommonStrings.action_go_to_roles_and_permissions),
                onClick = onRolesAndPermissionsClick,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = IconSource.Vector(CompoundIcons.Settings()),
            )
        }
        if (showChooseOwnersButton) {
            Button(
                text = stringResource(R.string.screen_leave_space_choose_owners_action),
                onClick = onChooseOwnersButtonClick,
                modifier = Modifier.fillMaxWidth(),
                destructive = true,
            )
        }
        TextButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(CommonStrings.action_cancel),
            onClick = onCancel,
        )
    }
}

@Composable
private fun SpaceItem(
    selectableSpaceRoom: SelectableSpaceRoom,
    showCheckBox: Boolean,
    onClick: () -> Unit,
) {
    val room = selectableSpaceRoom.spaceRoom
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 66.dp)
            .padding(horizontal = 16.dp)
            .toggleable(
                value = selectableSpaceRoom.isSelected,
                role = Role.Checkbox,
                enabled = selectableSpaceRoom.isLastOwner.not(),
                onValueChange = { onClick() }
            )
            .clickable(
                enabled = selectableSpaceRoom.isLastOwner.not(),
                // TODO
                onClickLabel = null,
                role = Role.Checkbox,
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Avatar(
            avatarData = room.getAvatarData(AvatarSize.LeaveSpaceRoom),
            avatarType = if (room.isSpace) AvatarType.Space() else AvatarType.Room(),
        )
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                modifier = Modifier
                    .padding(end = 16.dp),
                text = room.displayName,
                color = ElementTheme.colors.textPrimary,
                style = ElementTheme.typography.fontBodyLgMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (room.joinRule == JoinRule.Invite) {
                    // Picto for invite only
                    Icon(
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 4.dp),
                        imageVector = CompoundIcons.LockSolid(),
                        contentDescription = null,
                        tint = ElementTheme.colors.iconTertiary,
                    )
                } else if (room.worldReadable) {
                    // Picto for world readable
                    Icon(
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 4.dp),
                        imageVector = CompoundIcons.Public(),
                        contentDescription = null,
                        tint = ElementTheme.colors.iconTertiary,
                    )
                }
                // Number of members
                val membersCount = pluralStringResource(
                    CommonPlurals.common_member_count,
                    room.numJoinedMembers,
                    room.numJoinedMembers
                )
                val subTitle = if (selectableSpaceRoom.isLastOwner) {
                    stringResource(R.string.screen_leave_space_last_admin_info, membersCount)
                } else {
                    membersCount
                }
                Text(
                    modifier = Modifier.padding(end = 16.dp),
                    text = subTitle,
                    color = ElementTheme.colors.textSecondary,
                    style = ElementTheme.typography.fontBodyMdRegular,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (showCheckBox) {
            Checkbox(
                checked = selectableSpaceRoom.isSelected,
                onCheckedChange = null,
                enabled = selectableSpaceRoom.isLastOwner.not(),
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun LeaveSpaceViewPreview(
    @PreviewParameter(LeaveSpaceStateProvider::class) state: LeaveSpaceState,
) = ElementPreview {
    LeaveSpaceView(
        state = state,
        onCancel = {},
        onRolesAndPermissionsClick = {},
        onChooseOwnersClick = {},
    )
}
