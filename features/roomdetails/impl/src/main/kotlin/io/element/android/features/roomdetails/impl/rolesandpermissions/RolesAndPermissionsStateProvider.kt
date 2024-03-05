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
                changeOwnRoleAction = AsyncAction.Confirming,
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
        )
}

internal fun aRolesAndPermissionsState(
    adminCount: Int = 0,
    moderatorCount: Int = 0,
    changeOwnRoleAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (RolesAndPermissionsEvents) -> Unit = {},
) = RolesAndPermissionsState(
    adminCount = adminCount,
    moderatorCount = moderatorCount,
    changeOwnRoleAction = changeOwnRoleAction,
    eventSink = eventSink,
)
