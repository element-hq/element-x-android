/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rolesandpermissions.impl.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.rolesandpermissions.impl.analytics.trackPermissionChangeAnalytics
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevelsValues
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Inject
class ChangeRoomPermissionsPresenter(
    private val room: JoinedRoom,
    private val analyticsService: AnalyticsService,
) : Presenter<ChangeRoomPermissionsState> {
    companion object {
        private fun itemsForSection(section: RoomPermissionsSection) = when (section) {
            RoomPermissionsSection.SpaceDetails,
            RoomPermissionsSection.RoomDetails -> persistentListOf(
                RoomPermissionType.ROOM_NAME,
                RoomPermissionType.ROOM_AVATAR,
                RoomPermissionType.ROOM_TOPIC,
            )
            RoomPermissionsSection.MessagesAndContent -> persistentListOf(
                RoomPermissionType.SEND_EVENTS,
                RoomPermissionType.REDACT_EVENTS,
            )
            RoomPermissionsSection.MembershipModeration -> persistentListOf(
                RoomPermissionType.INVITE,
                RoomPermissionType.KICK,
                RoomPermissionType.BAN,
            )
        }

        private fun RoomPermissionsSection.shouldShow(isSpace: Boolean): Boolean {
            return when (this) {
                RoomPermissionsSection.RoomDetails -> !isSpace
                RoomPermissionsSection.MembershipModeration -> true
                RoomPermissionsSection.MessagesAndContent -> !isSpace
                RoomPermissionsSection.SpaceDetails -> isSpace
            }
        }

        internal fun buildItems(isSpace: Boolean) =
            RoomPermissionsSection.entries
                .filter { section -> section.shouldShow(isSpace) }
                .associateWith { itemsForSection(it) }
                .toImmutableMap()
    }

    private val itemsBySection = buildItems(isSpace = room.info().isSpace)

    private var initialPermissions by mutableStateOf<RoomPowerLevelsValues?>(null)
    private var currentPermissions by mutableStateOf<RoomPowerLevelsValues?>(null)
    private var saveAction by mutableStateOf<AsyncAction<Boolean>>(AsyncAction.Uninitialized)

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
                    val powerLevel = when (event.role) {
                        SelectableRole.Admin -> RoomMember.Role.Admin.powerLevel
                        SelectableRole.Moderator -> RoomMember.Role.Moderator.powerLevel
                        SelectableRole.Everyone -> RoomMember.Role.User.powerLevel
                    }
                    currentPermissions = when (event.action) {
                        RoomPermissionType.BAN -> currentPermissions?.copy(ban = powerLevel)
                        RoomPermissionType.INVITE -> currentPermissions?.copy(invite = powerLevel)
                        RoomPermissionType.KICK -> currentPermissions?.copy(kick = powerLevel)
                        RoomPermissionType.SEND_EVENTS -> currentPermissions?.copy(sendEvents = powerLevel)
                        RoomPermissionType.REDACT_EVENTS -> currentPermissions?.copy(redactEvents = powerLevel)
                        RoomPermissionType.ROOM_NAME -> currentPermissions?.copy(roomName = powerLevel)
                        RoomPermissionType.ROOM_AVATAR -> currentPermissions?.copy(roomAvatar = powerLevel)
                        RoomPermissionType.ROOM_TOPIC -> currentPermissions?.copy(roomTopic = powerLevel)
                    }
                }
                is ChangeRoomPermissionsEvent.Save -> coroutineScope.save()
                is ChangeRoomPermissionsEvent.Exit -> {
                    saveAction = if (!hasChanges || saveAction == AsyncAction.ConfirmingCancellation) {
                        AsyncAction.Success(false)
                    } else {
                        AsyncAction.ConfirmingCancellation
                    }
                }
                is ChangeRoomPermissionsEvent.ResetPendingActions -> {
                    saveAction = AsyncAction.Uninitialized
                }
            }
        }
        return ChangeRoomPermissionsState(
            currentPermissions = currentPermissions,
            itemsBySection = itemsBySection,
            hasChanges = hasChanges,
            saveAction = saveAction,
            eventSink = ::handleEvent,
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
                saveAction = AsyncAction.Success(true)
            }
            .onFailure {
                saveAction = AsyncAction.Failure(it)
            }
    }
}
