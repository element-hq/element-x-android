/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rolesandpermissions.impl.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import io.element.android.features.rolesandpermissions.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.components.preferences.DropdownOption
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevelsValues
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

data class ChangeRoomPermissionsState(
    val currentPermissions: RoomPowerLevelsValues?,
    val itemsBySection: ImmutableMap<RoomPermissionsSection, ImmutableList<RoomPermissionType>>,
    val hasChanges: Boolean,
    val saveAction: AsyncAction<Boolean>,
    val eventSink: (ChangeRoomPermissionsEvent) -> Unit,
) {
    fun selectedRoleForType(type: RoomPermissionType): SelectableRole? {
        if (currentPermissions == null) return null
        val role = when (type) {
            RoomPermissionType.BAN -> RoomMember.Role.forPowerLevel(currentPermissions.ban)
            RoomPermissionType.INVITE -> RoomMember.Role.forPowerLevel(currentPermissions.invite)
            RoomPermissionType.KICK -> RoomMember.Role.forPowerLevel(currentPermissions.kick)
            RoomPermissionType.SEND_EVENTS -> RoomMember.Role.forPowerLevel(currentPermissions.eventsDefault)
            RoomPermissionType.REDACT_EVENTS -> RoomMember.Role.forPowerLevel(currentPermissions.redactEvents)
            RoomPermissionType.ROOM_NAME -> RoomMember.Role.forPowerLevel(currentPermissions.roomName)
            RoomPermissionType.ROOM_AVATAR -> RoomMember.Role.forPowerLevel(currentPermissions.roomAvatar)
            RoomPermissionType.ROOM_TOPIC -> RoomMember.Role.forPowerLevel(currentPermissions.roomTopic)
            RoomPermissionType.SPACE_MANAGE_ROOMS -> RoomMember.Role.forPowerLevel(currentPermissions.spaceChild)
            RoomPermissionType.CHANGE_SETTINGS -> RoomMember.Role.forPowerLevel(currentPermissions.stateDefault)
        }
        return when (role) {
            is RoomMember.Role.Owner,
            RoomMember.Role.Admin -> SelectableRole.Admin
            RoomMember.Role.Moderator -> SelectableRole.Moderator
            RoomMember.Role.User -> SelectableRole.Everyone
        }
    }
}

enum class RoomPermissionsSection {
    ManageMembers,
    EditDetails,
    MessagesAndContent,
    ManageSpace
}

enum class SelectableRole : DropdownOption {
    Admin {
        @Composable
        @ReadOnlyComposable
        override fun getText(): String = stringResource(R.string.screen_room_member_list_role_administrator)
    },
    Moderator {
        @Composable
        @ReadOnlyComposable
        override fun getText(): String = stringResource(R.string.screen_room_member_list_role_moderator)
    },
    Everyone {
        @Composable
        @ReadOnlyComposable
        override fun getText(): String = stringResource(R.string.screen_room_change_permissions_everyone)
    }
}

enum class RoomPermissionType {
    BAN,
    INVITE,
    KICK,
    SEND_EVENTS,
    REDACT_EVENTS,
    ROOM_NAME,
    ROOM_AVATAR,
    ROOM_TOPIC,
    SPACE_MANAGE_ROOMS,
    CHANGE_SETTINGS,
}
