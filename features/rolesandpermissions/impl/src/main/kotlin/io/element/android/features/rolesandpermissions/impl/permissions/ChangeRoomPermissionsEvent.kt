/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rolesandpermissions.impl.permissions

interface ChangeRoomPermissionsEvent {
    data class ChangeMinimumRoleForAction(val action: RoomPermissionType, val role: SelectableRole) : ChangeRoomPermissionsEvent
    data object Save : ChangeRoomPermissionsEvent
    data object Exit : ChangeRoomPermissionsEvent
    data object ResetPendingActions : ChangeRoomPermissionsEvent
}
