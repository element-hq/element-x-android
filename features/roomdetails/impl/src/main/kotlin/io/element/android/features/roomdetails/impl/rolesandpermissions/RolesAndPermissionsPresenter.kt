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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.powerlevels.UserRoleChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class RolesAndPermissionsPresenter @Inject constructor(
    private val room: MatrixRoom,
    private val dispatchers: CoroutineDispatchers,
) : Presenter<RolesAndPermissionsState> {
    @Composable
    override fun present(): RolesAndPermissionsState {
        val coroutineScope = rememberCoroutineScope()
        val roomInfo by room.roomInfoFlow.collectAsState(initial = null)
        val moderatorCount by remember {
            derivedStateOf {
                roomInfo.userCountWithRole(RoomMember.Role.MODERATOR)
            }
        }
        val adminCount by remember {
            derivedStateOf {
                roomInfo.userCountWithRole(RoomMember.Role.ADMIN)
            }
        }
        val changeOwnRoleAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }

        fun handleEvent(event: RolesAndPermissionsEvents) {
            when (event) {
                is RolesAndPermissionsEvents.ChangeOwnRole -> {
                    changeOwnRoleAction.value = AsyncAction.Confirming
                }
                is RolesAndPermissionsEvents.CancelPendingAction -> {
                    changeOwnRoleAction.value = AsyncAction.Uninitialized
                }
                is RolesAndPermissionsEvents.DemoteSelfTo -> coroutineScope.demoteSelfTo(
                    role = event.role,
                    changeOwnRoleAction = changeOwnRoleAction,
                )
            }
        }

        return RolesAndPermissionsState(
            adminCount = adminCount,
            moderatorCount = moderatorCount,
            changeOwnRoleAction = changeOwnRoleAction.value,
            eventSink = { handleEvent(it) },
        )
    }

    private fun CoroutineScope.demoteSelfTo(
        role: RoomMember.Role,
        changeOwnRoleAction: MutableState<AsyncAction<Unit>>,
    ) = launch(dispatchers.io) {
        runUpdatingState(changeOwnRoleAction) {
            room.updateUsersRoles(listOf(UserRoleChange(room.sessionId, role)))
        }
    }

    private fun MatrixRoomInfo?.userCountWithRole(role: RoomMember.Role): Int {
        return if (this != null) {
            userPowerLevels.count { (_, level) -> RoomMember.Role.forPowerLevel(level) == role }
        } else {
            0
        }
    }
}
