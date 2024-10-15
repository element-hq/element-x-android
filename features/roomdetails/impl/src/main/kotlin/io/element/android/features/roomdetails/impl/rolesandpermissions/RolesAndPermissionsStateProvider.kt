/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.rolesandpermissions

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction

class RolesAndPermissionsStateProvider : PreviewParameterProvider<RolesAndPermissionsState> {
    override val values: Sequence<RolesAndPermissionsState>
        get() = sequenceOf(
            aRolesAndPermissionsState(),
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
        )
}

internal fun aRolesAndPermissionsState(
    adminCount: Int = 0,
    moderatorCount: Int = 0,
    changeOwnRoleAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    resetPermissionsAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (RolesAndPermissionsEvents) -> Unit = {},
) = RolesAndPermissionsState(
    adminCount = adminCount,
    moderatorCount = moderatorCount,
    changeOwnRoleAction = changeOwnRoleAction,
    resetPermissionsAction = resetPermissionsAction,
    eventSink = eventSink,
)
