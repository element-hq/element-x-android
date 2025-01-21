/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.rolesandpermissions.permissions

import io.element.android.libraries.matrix.api.room.RoomMember

interface ChangeRoomPermissionsEvent {
    data class ChangeMinimumRoleForAction(val action: RoomPermissionType, val role: RoomMember.Role) : ChangeRoomPermissionsEvent
    data object Save : ChangeRoomPermissionsEvent
    data object Exit : ChangeRoomPermissionsEvent
    data object ResetPendingActions : ChangeRoomPermissionsEvent
}
