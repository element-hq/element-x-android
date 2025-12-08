/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rolesandpermissions.impl.root

import io.element.android.libraries.architecture.AsyncAction

data class RolesAndPermissionsState(
    val roomSupportsOwnerRole: Boolean,
    val adminCount: Int?,
    val moderatorCount: Int?,
    val canDemoteSelf: Boolean,
    val changeOwnRoleAction: AsyncAction<Unit>,
    val resetPermissionsAction: AsyncAction<Unit>,
    val eventSink: (RolesAndPermissionsEvents) -> Unit,
)
