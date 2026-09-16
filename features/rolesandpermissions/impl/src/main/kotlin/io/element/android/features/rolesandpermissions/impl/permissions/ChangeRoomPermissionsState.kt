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
import kotlinx.collections.immutable.persistentListOf

data class ChangeRoomPermissionsState(
    private val ownPowerLevel: Long,
    val currentPermissions: RoomPowerLevelsValues?,
    val itemsBySection: ImmutableMap<RoomPermissionsSection, ImmutableList<RoomPermissionType>>,
    val hasChanges: Boolean,
    val saveAction: AsyncAction<Boolean>,
    val eventSink: (ChangeRoomPermissionsEvent) -> Unit,
) {
    private val ownRole = RoomMember.Role.forPowerLevel(ownPowerLevel)

    // Roles that the user can select based on their own role
    val selectableRoles: ImmutableList<SelectableRole> = when (ownRole) {
        is RoomMember.Role.Owner,
        RoomMember.Role.Admin -> persistentListOf(SelectableRole.Admin, SelectableRole.Moderator, SelectableRole.Everyone)
        RoomMember.Role.Moderator -> persistentListOf(SelectableRole.Moderator, SelectableRole.Everyone)
        RoomMember.Role.User -> persistentListOf(SelectableRole.Everyone)
    }

    fun selectedRoleForType(type: RoomPermissionType): SelectableRole? {
        val powerLevel = currentPowerLevelForType(type = type) ?: return null
        return when (RoomMember.Role.forPowerLevel(powerLevel)) {
            is RoomMember.Role.Owner,
            RoomMember.Role.Admin -> SelectableRole.Admin
            RoomMember.Role.Moderator -> SelectableRole.Moderator
            RoomMember.Role.User -> SelectableRole.Everyone
        }
    }

    fun canChangePermission(type: RoomPermissionType): Boolean {
        val currentPowerLevel = currentPowerLevelForType(type) ?: return false
        return ownPowerLevel >= currentPowerLevel
    }

    private fun currentPowerLevelForType(type: RoomPermissionType): Long? {
        if (currentPermissions == null) return null
        return when (type) {
            RoomPermissionType.BAN -> currentPermissions.ban
            RoomPermissionType.INVITE -> currentPermissions.invite
            RoomPermissionType.KICK -> currentPermissions.kick
            RoomPermissionType.SEND_EVENTS -> currentPermissions.eventsDefault
            RoomPermissionType.REDACT_EVENTS -> currentPermissions.redactEvents
            RoomPermissionType.ROOM_NAME -> currentPermissions.roomName
            RoomPermissionType.ROOM_AVATAR -> currentPermissions.roomAvatar
            RoomPermissionType.ROOM_TOPIC -> currentPermissions.roomTopic
            RoomPermissionType.SPACE_MANAGE_ROOMS -> currentPermissions.spaceChild
            RoomPermissionType.SHARE_LIVE_LOCATION -> maxOf(currentPermissions.beacon, currentPermissions.beaconInfo)
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
    SHARE_LIVE_LOCATION,
}
