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

package io.element.android.features.roomdetails.impl.rolesandpermissions.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.powerlevels.MatrixRoomPowerLevels
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ChangeRoomPermissionsPresenter @AssistedInject constructor(
    @Assisted private val section: ChangeRoomPermissionsSection,
    private val room: MatrixRoom,
) : Presenter<ChangeRoomPermissionsState> {
    @AssistedFactory
    interface Factory {
        fun create(section: ChangeRoomPermissionsSection): ChangeRoomPermissionsPresenter
    }

    private val items: ImmutableList<RoomPermissionsItem> =
        when (section) {
            ChangeRoomPermissionsSection.RoomDetails -> persistentListOf(
                RoomPermissionsItem.ROOM_NAME,
                RoomPermissionsItem.ROOM_AVATAR,
                RoomPermissionsItem.ROOM_TOPIC,
            )
            ChangeRoomPermissionsSection.MessagesAndContent -> persistentListOf(
                RoomPermissionsItem.SEND_EVENTS,
                RoomPermissionsItem.REDACT_EVENTS,
            )
            ChangeRoomPermissionsSection.MembershipModeration -> persistentListOf(
                RoomPermissionsItem.INVITE,
                RoomPermissionsItem.KICK,
                RoomPermissionsItem.BAN,
            )
        }

    private var initialPermissions by mutableStateOf(persistentMapOf<RoomPermissionsItem, RoomMember.Role>())
    private var currentPermissions by mutableStateOf(persistentMapOf<RoomPermissionsItem, RoomMember.Role>())
    private var saveAction by mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized)
    private var confirmExitAction by mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized)

    @Composable
    override fun present(): ChangeRoomPermissionsState {
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            updatePermissions()
        }

        val hasChanges by remember {
            derivedStateOf { initialPermissions != currentPermissions }
        }

        fun handleEvent(event: ChangeRoomPermissionsEvent) {
            when (event) {
                is ChangeRoomPermissionsEvent.ChangeRole -> {
                    currentPermissions = currentPermissions.put(event.item, event.role)
                }
                is ChangeRoomPermissionsEvent.Save -> coroutineScope.save()
                is ChangeRoomPermissionsEvent.Exit -> {
                    confirmExitAction = if (!hasChanges || confirmExitAction.isConfirming()) {
                        AsyncAction.Success(Unit)
                    } else {
                        AsyncAction.Confirming
                    }
                }
                is ChangeRoomPermissionsEvent.ResetPendingActions -> {
                    saveAction = AsyncAction.Uninitialized
                    confirmExitAction = AsyncAction.Uninitialized
                }
            }
        }
        return ChangeRoomPermissionsState(
            section = section,
            currentPermissions = currentPermissions,
            items = items,
            hasChanges = hasChanges,
            saveAction = saveAction,
            confirmExitAction = confirmExitAction,
            eventSink = { handleEvent(it) }
        )
    }

    private suspend fun updatePermissions() {
        val powerLevels = room.powerLevels().getOrNull() ?: return
        initialPermissions = persistentMapOf(
            RoomPermissionsItem.BAN to RoomMember.Role.forPowerLevel(powerLevels.ban),
            RoomPermissionsItem.INVITE to RoomMember.Role.forPowerLevel(powerLevels.invite),
            RoomPermissionsItem.KICK to RoomMember.Role.forPowerLevel(powerLevels.kick),
            RoomPermissionsItem.SEND_EVENTS to RoomMember.Role.forPowerLevel(powerLevels.sendEvents),
            RoomPermissionsItem.REDACT_EVENTS to RoomMember.Role.forPowerLevel(powerLevels.redactEvents),
            RoomPermissionsItem.ROOM_NAME to RoomMember.Role.forPowerLevel(powerLevels.roomName),
            RoomPermissionsItem.ROOM_AVATAR to RoomMember.Role.forPowerLevel(powerLevels.roomAvatar),
            RoomPermissionsItem.ROOM_TOPIC to RoomMember.Role.forPowerLevel(powerLevels.roomTopic),
        )
        currentPermissions = initialPermissions
    }

    private fun CoroutineScope.save() = launch {
        saveAction = AsyncAction.Loading
        val updatedRoomPowerLevels = MatrixRoomPowerLevels(
            ban = currentPermissions[RoomPermissionsItem.BAN]?.powerLevel ?: 0,
            invite = currentPermissions[RoomPermissionsItem.INVITE]?.powerLevel ?: 0,
            kick = currentPermissions[RoomPermissionsItem.KICK]?.powerLevel ?: 0,
            sendEvents = currentPermissions[RoomPermissionsItem.SEND_EVENTS]?.powerLevel ?: 0,
            redactEvents = currentPermissions[RoomPermissionsItem.REDACT_EVENTS]?.powerLevel ?: 0,
            roomName = currentPermissions[RoomPermissionsItem.ROOM_NAME]?.powerLevel ?: 0,
            roomAvatar = currentPermissions[RoomPermissionsItem.ROOM_AVATAR]?.powerLevel ?: 0,
            roomTopic = currentPermissions[RoomPermissionsItem.ROOM_TOPIC]?.powerLevel ?: 0,
        )
        room.updatePowerLevels(updatedRoomPowerLevels)
            .onSuccess {
                initialPermissions = currentPermissions
                saveAction = AsyncAction.Success(Unit)
            }
            .onFailure {
                saveAction = AsyncAction.Failure(it)
            }
    }
}
