/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import io.element.android.features.roomdetails.impl.analytics.trackPermissionChangeAnalytics
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.powerlevels.MatrixRoomPowerLevels
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ChangeRoomPermissionsPresenter @AssistedInject constructor(
    @Assisted private val section: ChangeRoomPermissionsSection,
    private val room: MatrixRoom,
    private val analyticsService: AnalyticsService,
) : Presenter<ChangeRoomPermissionsState> {
    companion object {
        internal fun itemsForSection(section: ChangeRoomPermissionsSection) = when (section) {
            ChangeRoomPermissionsSection.RoomDetails -> persistentListOf(
                RoomPermissionType.ROOM_NAME,
                RoomPermissionType.ROOM_AVATAR,
                RoomPermissionType.ROOM_TOPIC,
            )
            ChangeRoomPermissionsSection.MessagesAndContent -> persistentListOf(
                RoomPermissionType.SEND_EVENTS,
                RoomPermissionType.REDACT_EVENTS,
            )
            ChangeRoomPermissionsSection.MembershipModeration -> persistentListOf(
                RoomPermissionType.INVITE,
                RoomPermissionType.KICK,
                RoomPermissionType.BAN,
            )
        }
    }
    @AssistedFactory
    interface Factory {
        fun create(section: ChangeRoomPermissionsSection): ChangeRoomPermissionsPresenter
    }

    private val items: ImmutableList<RoomPermissionType> = itemsForSection(section)

    private var initialPermissions by mutableStateOf<MatrixRoomPowerLevels?>(null)
    private var currentPermissions by mutableStateOf<MatrixRoomPowerLevels?>(null)
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
                is ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction -> {
                    currentPermissions = when (event.action) {
                        RoomPermissionType.BAN -> currentPermissions?.copy(ban = event.role.powerLevel)
                        RoomPermissionType.INVITE -> currentPermissions?.copy(invite = event.role.powerLevel)
                        RoomPermissionType.KICK -> currentPermissions?.copy(kick = event.role.powerLevel)
                        RoomPermissionType.SEND_EVENTS -> currentPermissions?.copy(sendEvents = event.role.powerLevel)
                        RoomPermissionType.REDACT_EVENTS -> currentPermissions?.copy(redactEvents = event.role.powerLevel)
                        RoomPermissionType.ROOM_NAME -> currentPermissions?.copy(roomName = event.role.powerLevel)
                        RoomPermissionType.ROOM_AVATAR -> currentPermissions?.copy(roomAvatar = event.role.powerLevel)
                        RoomPermissionType.ROOM_TOPIC -> currentPermissions?.copy(roomTopic = event.role.powerLevel)
                    }
                }
                is ChangeRoomPermissionsEvent.Save -> coroutineScope.save()
                is ChangeRoomPermissionsEvent.Exit -> {
                    confirmExitAction = if (!hasChanges || confirmExitAction.isConfirming()) {
                        AsyncAction.Success(Unit)
                    } else {
                        AsyncAction.ConfirmingNoParams
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
        initialPermissions = powerLevels
        currentPermissions = initialPermissions
    }

    private fun CoroutineScope.save() = launch {
        saveAction = AsyncAction.Loading
        val updatedRoomPowerLevels = currentPermissions ?: run {
            saveAction = AsyncAction.Failure(IllegalStateException("Failed to set room power levels"))
            return@launch
        }
        room.updatePowerLevels(updatedRoomPowerLevels)
            .onSuccess {
                analyticsService.trackPermissionChangeAnalytics(initialPermissions, updatedRoomPowerLevels)
                initialPermissions = currentPermissions
                saveAction = AsyncAction.Success(Unit)
            }
            .onFailure {
                saveAction = AsyncAction.Failure(it)
            }
    }
}
