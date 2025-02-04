/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.rolesandpermissions

import io.element.android.libraries.architecture.AsyncAction

data class RolesAndPermissionsState(
    val adminCount: Int,
    val moderatorCount: Int,
    val changeOwnRoleAction: AsyncAction<Unit>,
    val resetPermissionsAction: AsyncAction<Unit>,
    val eventSink: (RolesAndPermissionsEvents) -> Unit,
)
