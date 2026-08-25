/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rolesandpermissions.impl.root

import io.element.android.libraries.matrix.api.room.RoomMember

sealed interface RolesAndPermissionsEvent {
    data object ChangeOwnRole : RolesAndPermissionsEvent
    data class DemoteSelfTo(val role: RoomMember.Role) : RolesAndPermissionsEvent
    data object ResetPermissions : RolesAndPermissionsEvent
    data object CancelPendingAction : RolesAndPermissionsEvent
}
