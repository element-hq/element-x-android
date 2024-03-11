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

package io.element.android.features.roomdetails.impl.rolesandpermissions.permissions

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.powerlevels.MatrixRoomPowerLevels
import kotlinx.collections.immutable.toPersistentList

class ChangeRoomPermissionsStatePreviewProvider : PreviewParameterProvider<ChangeRoomPermissionsState> {
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
            aChangeRoomPermissionsState(section = ChangeRoomPermissionsSection.RoomDetails, hasChanges = true, confirmExitAction = AsyncAction.Confirming),
        )
}

internal fun aChangeRoomPermissionsState(
    section: ChangeRoomPermissionsSection,
    currentPermissions: MatrixRoomPowerLevels = previewPermissions(),
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

private fun previewPermissions(): MatrixRoomPowerLevels {
    return MatrixRoomPowerLevels(
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
