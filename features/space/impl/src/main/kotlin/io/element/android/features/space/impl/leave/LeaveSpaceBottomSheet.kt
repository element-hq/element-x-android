/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.leave

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.space.impl.R
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.async.AsyncFailure
import io.element.android.libraries.designsystem.components.async.AsyncLoading
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.ui.strings.CommonPlurals
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveSpaceBottomSheet(
    state: LeaveSpaceBottomSheetState.Shown,
    onLeaveSpace: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
    ) {
        LazyColumn {
            item {
                LeaveSpaceBottomSheetHeader(state)
            }
            when (state.roomsWhereUserIsTheOnlyAdmin) {
                is AsyncData.Success -> {
                    // List rooms where the user is the only admin
                    state.roomsWhereUserIsTheOnlyAdmin.data.forEach { spaceRoom ->
                        item {
                            SpaceItem(room = spaceRoom)
                        }
                    }
                }
                is AsyncData.Failure -> item {
                    AsyncFailure(
                        throwable = state.roomsWhereUserIsTheOnlyAdmin.error,
                        onRetry = null,
                    )
                }
                is AsyncData.Loading,
                AsyncData.Uninitialized ->
                    item {
                        AsyncLoading()
                    }
            }
            item {
                LeaveSpaceBottomSheetButtons(
                    showLeaveButton = state.roomsWhereUserIsTheOnlyAdmin is AsyncData.Success,
                    onLeaveSpace = onLeaveSpace,
                    onCancel = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun LeaveSpaceBottomSheetHeader(state: LeaveSpaceBottomSheetState.Shown) {
    IconTitleSubtitleMolecule(
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp, start = 24.dp, end = 24.dp),
        iconStyle = BigIcon.Style.AlertSolid,
        title = stringResource(
            R.string.screen_bottom_sheet_leave_space_title,
            state.spaceName ?: stringResource(CommonStrings.common_space)
        ),
        subTitle =
            if (state.roomsWhereUserIsTheOnlyAdmin is AsyncData.Success) {
                if (state.roomsWhereUserIsTheOnlyAdmin.data.isEmpty()) {
                    stringResource(R.string.screen_bottom_sheet_leave_space_subtitle)
                } else {
                    stringResource(R.string.screen_bottom_sheet_leave_space_subtitle_admin)
                }
            } else {
                null
            },
    )
}

@Composable
private fun LeaveSpaceBottomSheetButtons(
    showLeaveButton: Boolean,
    onLeaveSpace: () -> Unit,
    onCancel: () -> Unit,
) {
    ButtonColumnMolecule(
        modifier = Modifier.padding(top = 32.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        if (showLeaveButton) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(CommonStrings.action_leave),
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
private fun SpaceItem(room: SpaceRoom) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 66.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(
                modifier = Modifier.padding(horizontal = 16.dp),
                avatarData = room.getAvatarData(AvatarSize.LeaveSpaceRoom),
                avatarType = if (room.isSpace) AvatarType.Space() else AvatarType.Room(),
            )
            Column {
                Text(
                    modifier = Modifier.padding(end = 16.dp),
                    text = room.name ?: stringResource(CommonStrings.common_no_room_name),
                    color = ElementTheme.colors.textPrimary,
                    style = ElementTheme.typography.fontBodyLgMedium,
                    maxLines = 1,
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
                    Text(
                        modifier = Modifier.padding(end = 16.dp),
                        text = pluralStringResource(
                            CommonPlurals.common_member_count,
                            room.numJoinedMembers,
                            room.numJoinedMembers
                        ),
                        color = ElementTheme.colors.textSecondary,
                        style = ElementTheme.typography.fontBodyMdRegular,
                    )
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 32.dp + AvatarSize.LeaveSpaceRoom.dp)
        )
    }
}

@PreviewsDayNight
@Composable
internal fun LeaveSpaceBottomSheetPreview(
    @PreviewParameter(LeaveSpaceBottomSheetStateShownProvider::class) state: LeaveSpaceBottomSheetState.Shown,
) = ElementPreview {
    LeaveSpaceBottomSheet(
        state = state,
        onLeaveSpace = {},
        onDismiss = {},
    )
}
