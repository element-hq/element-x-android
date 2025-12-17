/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rolesandpermissions.impl.root

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction

class RolesAndPermissionsStateProvider : PreviewParameterProvider<RolesAndPermissionsState> {
    override val values: Sequence<RolesAndPermissionsState>
        get() = sequenceOf(
            aRolesAndPermissionsState(roomSupportsOwners = false),
            aRolesAndPermissionsState(adminCount = 1, moderatorCount = 2),
            aRolesAndPermissionsState(
                adminCount = 1,
                moderatorCount = 2,
                changeOwnRoleAction = AsyncAction.ConfirmingNoParams,
            ),
            aRolesAndPermissionsState(
                adminCount = 1,
                moderatorCount = 2,
                changeOwnRoleAction = AsyncAction.Loading,
            ),
            aRolesAndPermissionsState(
                adminCount = 1,
                moderatorCount = 2,
                changeOwnRoleAction = AsyncAction.Failure(IllegalStateException("Failed to change role")),
            ),
            aRolesAndPermissionsState(
                adminCount = 1,
                moderatorCount = 2,
                resetPermissionsAction = AsyncAction.ConfirmingNoParams,
            ),
            aRolesAndPermissionsState(
                adminCount = 1,
                moderatorCount = 2,
                resetPermissionsAction = AsyncAction.Loading,
            ),
            aRolesAndPermissionsState(
                adminCount = 1,
                moderatorCount = 2,
                resetPermissionsAction = AsyncAction.Failure(IllegalStateException("Failed to reset permissions")),
            ),
            aRolesAndPermissionsState(canDemoteSelf = false),
        )
}

internal fun aRolesAndPermissionsState(
    roomSupportsOwners: Boolean = true,
    adminCount: Int = 0,
    moderatorCount: Int = 0,
    canDemoteSelf: Boolean = true,
    changeOwnRoleAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    resetPermissionsAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (RolesAndPermissionsEvents) -> Unit = {},
) = RolesAndPermissionsState(
    roomSupportsOwnerRole = roomSupportsOwners,
    adminCount = adminCount,
    canDemoteSelf = canDemoteSelf,
    moderatorCount = moderatorCount,
    changeOwnRoleAction = changeOwnRoleAction,
    resetPermissionsAction = resetPermissionsAction,
    eventSink = eventSink,
)
