/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.space.impl.leave

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
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
import io.element.android.libraries.designsystem.theme.components.Scaffold
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
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = ElementTheme.colors.bgCanvasDefault,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .consumeWindowInsets(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LeaveSpaceHeader(
                state = state,
                onBackClick = onCancel,
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f),
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
                            onRetry = null,
                        )
                    }
                    is AsyncData.Loading,
                    AsyncData.Uninitialized -> item {
                        AsyncLoading()
                    }
                }
            }
            LeaveSpaceButtons(
                showLeaveButton = state.selectableSpaceRooms is AsyncData.Success,
                selectedRoomsCount = state.selectedRoomsCount,
                onLeaveSpace = {
                    state.eventSink(LeaveSpaceEvents.LeaveSpace)
                },
                onCancel = onCancel,
            )
        }
    }

    AsyncActionView(
        async = state.leaveSpaceAction,
        onSuccess = { /* Nothing to do, the screen will be dismissed automatically */ },
        onErrorDismiss = { state.eventSink(LeaveSpaceEvents.CloseError) },
    )
}

@Composable
private fun LeaveSpaceHeader(
    state: LeaveSpaceState,
    onBackClick: () -> Unit,
) {
    Column {
        TopAppBar(
            navigationIcon = {
                BackButton(onClick = onBackClick)
            },
            title = {},
        )
        IconTitleSubtitleMolecule(
            modifier = Modifier.padding(top = 0.dp, bottom = 8.dp, start = 24.dp, end = 24.dp),
            iconStyle = BigIcon.Style.AlertSolid,
            title = stringResource(
                R.string.screen_leave_space_title,
                state.spaceName ?: stringResource(CommonStrings.common_space)
            ),
            subTitle =
                if (state.selectableSpaceRooms is AsyncData.Success && state.selectableSpaceRooms.data.isNotEmpty()) {
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
                Text(
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable {
                            state.eventSink(LeaveSpaceEvents.DeselectAllRooms)
                        }
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    text = stringResource(CommonStrings.common_deselect_all),
                    color = ElementTheme.colors.textActionPrimary,
                    style = ElementTheme.typography.fontBodyMdMedium,
                )
            } else {
                Text(
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable {
                            state.eventSink(LeaveSpaceEvents.SelectAllRooms)
                        }
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    text = stringResource(CommonStrings.common_select_all),
                    color = ElementTheme.colors.textActionPrimary,
                    style = ElementTheme.typography.fontBodyMdMedium,
                )
            }
        }
    }
}

@Composable
private fun LeaveSpaceButtons(
    showLeaveButton: Boolean,
    selectedRoomsCount: Int,
    onLeaveSpace: () -> Unit,
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
            .toggleable(
                value = selectableSpaceRoom.isSelected,
                role = Role.Checkbox,
                enabled = selectableSpaceRoom.isLastAdmin.not(),
                onValueChange = { onClick() }
            )
            .clickable(
                enabled = selectableSpaceRoom.isLastAdmin.not(),
                // TODO
                onClickLabel = null,
                role = Role.Checkbox,
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            modifier = Modifier.padding(horizontal = 16.dp),
            avatarData = room.getAvatarData(AvatarSize.LeaveSpaceRoom),
            avatarType = if (room.isSpace) AvatarType.Space() else AvatarType.Room(),
        )
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                modifier = Modifier
                    .padding(end = 16.dp),
                text = room.name ?: stringResource(
                    if (room.isSpace) {
                        CommonStrings.common_no_space_name
                    } else {
                        CommonStrings.common_no_room_name
                    },
                ),
                color = ElementTheme.colors.textPrimary,
                style = ElementTheme.typography.fontBodyLgMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (room.joinRule == JoinRule.Private) {
                    // Picto for private
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
                val subTitle = buildString {
                    append(
                        pluralStringResource(
                            CommonPlurals.common_member_count,
                            room.numJoinedMembers,
                            room.numJoinedMembers
                        )
                    )
                    if (selectableSpaceRoom.isLastAdmin) {
                        append(" ")
                        append(stringResource(R.string.screen_leave_space_last_admin_info))
                    }
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
                enabled = selectableSpaceRoom.isLastAdmin.not(),
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
    )
}
