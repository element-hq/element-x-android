/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rolesandpermissions.impl.permissions

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevelsValues
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableMap

class ChangeRoomPermissionsStateProvider : PreviewParameterProvider<ChangeRoomPermissionsState> {
    override val values: Sequence<ChangeRoomPermissionsState>
        get() = sequenceOf(
            aChangeRoomPermissionsState(),
            aChangeRoomPermissionsState(hasChanges = true),
            aChangeRoomPermissionsState(hasChanges = true, saveAction = AsyncAction.Loading),
            aChangeRoomPermissionsState(
                hasChanges = true,
                saveAction = AsyncAction.Failure(IllegalStateException("Failed to save changes"))
            ),
            aChangeRoomPermissionsState(hasChanges = true, confirmExitAction = AsyncAction.ConfirmingNoParams),
        )
}

internal fun aChangeRoomPermissionsState(
    currentPermissions: RoomPowerLevelsValues = previewPermissions(),
    itemsBySection: Map<RoomPermissionsSection, ImmutableList<RoomPermissionType>> = ChangeRoomPermissionsPresenter.buildItems(false),
    hasChanges: Boolean = false,
    saveAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    confirmExitAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (ChangeRoomPermissionsEvent) -> Unit = {},
) = ChangeRoomPermissionsState(
    currentPermissions = currentPermissions,
    itemsBySection = itemsBySection.toImmutableMap(),
    hasChanges = hasChanges,
    saveAction = saveAction,
    confirmExitAction = confirmExitAction,
    eventSink = eventSink,
)

private fun previewPermissions(): RoomPowerLevelsValues {
    return RoomPowerLevelsValues(
        // MembershipModeration section
        invite = RoomMember.Role.Admin.powerLevel,
        kick = RoomMember.Role.Moderator.powerLevel,
        ban = RoomMember.Role.User.powerLevel,
        // MessagesAndContent section
        redactEvents = RoomMember.Role.Moderator.powerLevel,
        sendEvents = RoomMember.Role.Admin.powerLevel,
        // RoomDetails section
        roomName = RoomMember.Role.Admin.powerLevel,
        roomAvatar = RoomMember.Role.Moderator.powerLevel,
        roomTopic = RoomMember.Role.User.powerLevel,
        // SpaceManagement section
        spaceChild = RoomMember.Role.Moderator.powerLevel,
    )
}
