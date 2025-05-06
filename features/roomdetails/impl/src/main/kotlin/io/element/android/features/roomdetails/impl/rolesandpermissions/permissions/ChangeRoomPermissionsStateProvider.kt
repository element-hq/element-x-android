/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.rolesandpermissions.permissions

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevels
import kotlinx.collections.immutable.toPersistentList

class ChangeRoomPermissionsStateProvider : PreviewParameterProvider<ChangeRoomPermissionsState> {
    override val values: Sequence<ChangeRoomPermissionsState>
        get() = sequenceOf(
            aChangeRoomPermissionsState(section = ChangeRoomPermissionsSection.RoomDetails),
            aChangeRoomPermissionsState(section = ChangeRoomPermissionsSection.MessagesAndContent),
            aChangeRoomPermissionsState(section = ChangeRoomPermissionsSection.MembershipModeration),
            aChangeRoomPermissionsState(section = ChangeRoomPermissionsSection.RoomDetails, hasChanges = true),
            aChangeRoomPermissionsState(section = ChangeRoomPermissionsSection.RoomDetails, hasChanges = true, saveAction = AsyncAction.Loading),
            aChangeRoomPermissionsState(
                section = ChangeRoomPermissionsSection.RoomDetails,
                hasChanges = true,
                saveAction = AsyncAction.Failure(IllegalStateException("Failed to save changes"))
            ),
            aChangeRoomPermissionsState(
                section = ChangeRoomPermissionsSection.RoomDetails,
                hasChanges = true,
                confirmExitAction = AsyncAction.ConfirmingNoParams,
            ),
        )
}

internal fun aChangeRoomPermissionsState(
    section: ChangeRoomPermissionsSection,
    currentPermissions: RoomPowerLevels = previewPermissions(),
    items: List<RoomPermissionType> = ChangeRoomPermissionsPresenter.itemsForSection(section),
    hasChanges: Boolean = false,
    saveAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    confirmExitAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (ChangeRoomPermissionsEvent) -> Unit = {},
) = ChangeRoomPermissionsState(
    section = section,
    currentPermissions = currentPermissions,
    items = items.toPersistentList(),
    hasChanges = hasChanges,
    saveAction = saveAction,
    confirmExitAction = confirmExitAction,
    eventSink = eventSink,
)

private fun previewPermissions(): RoomPowerLevels {
    return RoomPowerLevels(
        // MembershipModeration section
        invite = RoomMember.Role.ADMIN.powerLevel,
        kick = RoomMember.Role.MODERATOR.powerLevel,
        ban = RoomMember.Role.USER.powerLevel,
        // MessagesAndContent section
        redactEvents = RoomMember.Role.MODERATOR.powerLevel,
        sendEvents = RoomMember.Role.ADMIN.powerLevel,
        // RoomDetails section
        roomName = RoomMember.Role.ADMIN.powerLevel,
        roomAvatar = RoomMember.Role.MODERATOR.powerLevel,
        roomTopic = RoomMember.Role.USER.powerLevel,
    )
}
