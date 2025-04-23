/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.securityandprivacy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.roomdetails.impl.securityandprivacy.editroomaddress.matchesServer
import io.element.android.features.roomdetails.impl.securityandprivacy.permissions.securityAndPrivacyPermissionsAsState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.roomdirectory.RoomVisibility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SecurityAndPrivacyPresenter @AssistedInject constructor(
    @Assisted private val navigator: SecurityAndPrivacyNavigator,
    private val matrixClient: MatrixClient,
    private val room: JoinedRoom,
) : Presenter<SecurityAndPrivacyState> {
    @AssistedFactory
    interface Factory {
        fun create(navigator: SecurityAndPrivacyNavigator): SecurityAndPrivacyPresenter
    }

    @Composable
    override fun present(): SecurityAndPrivacyState {
        val coroutineScope = rememberCoroutineScope()

        val saveAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }
        val homeserverName = remember { matrixClient.userIdServerName() }
        val syncUpdateFlow = room.syncUpdateFlow.collectAsState()
        val roomInfo by room.roomInfoFlow.collectAsState()

        val savedIsVisibleInRoomDirectory = remember { mutableStateOf<AsyncData<Boolean>>(AsyncData.Uninitialized) }
        LaunchedEffect(Unit) {
            isRoomVisibleInRoomDirectory(savedIsVisibleInRoomDirectory)
        }

        val savedSettings by remember {
            derivedStateOf {
                val historyVisibility = roomInfo.historyVisibility.map()
                SecurityAndPrivacySettings(
                    roomAccess = roomInfo.joinRule.map(),
                    isEncrypted = roomInfo.isEncrypted == true,
                    isVisibleInRoomDirectory = savedIsVisibleInRoomDirectory.value,
                    historyVisibility = historyVisibility,
                    address = roomInfo.firstDisplayableAlias(homeserverName)?.value,
                )
            }
        }

        var editedRoomAccess by remember(savedSettings.roomAccess) {
            mutableStateOf(savedSettings.roomAccess)
        }
        var editedHistoryVisibility by remember(savedSettings.historyVisibility) {
            mutableStateOf(savedSettings.historyVisibility)
        }
        var editedIsEncrypted by remember(savedSettings.isEncrypted) {
            mutableStateOf(savedSettings.isEncrypted)
        }
        var editedVisibleInRoomDirectory by remember(savedIsVisibleInRoomDirectory.value) {
            mutableStateOf(savedIsVisibleInRoomDirectory.value)
        }
        val editedSettings = SecurityAndPrivacySettings(
            roomAccess = editedRoomAccess,
            isEncrypted = editedIsEncrypted,
            isVisibleInRoomDirectory = editedVisibleInRoomDirectory,
            historyVisibility = editedHistoryVisibility,
            address = savedSettings.address,
        )

        var showEnableEncryptionConfirmation by remember(savedSettings.isEncrypted) { mutableStateOf(false) }
        val permissions by room.securityAndPrivacyPermissionsAsState(syncUpdateFlow.value)

        fun handleEvents(event: SecurityAndPrivacyEvents) {
            when (event) {
                SecurityAndPrivacyEvents.Save -> {
                    coroutineScope.save(
                        saveAction = saveAction,
                        isVisibleInRoomDirectory = savedIsVisibleInRoomDirectory,
                        savedSettings = savedSettings,
                        editedSettings = editedSettings
                    )
                }
                is SecurityAndPrivacyEvents.ChangeRoomAccess -> {
                    editedRoomAccess = event.roomAccess
                }
                is SecurityAndPrivacyEvents.ToggleEncryptionState -> {
                    if (editedIsEncrypted) {
                        editedIsEncrypted = false
                    } else {
                        showEnableEncryptionConfirmation = true
                    }
                }
                is SecurityAndPrivacyEvents.ChangeHistoryVisibility -> {
                    editedHistoryVisibility = event.historyVisibility
                }
                SecurityAndPrivacyEvents.ToggleRoomVisibility -> {
                    editedVisibleInRoomDirectory = when (val edited = editedVisibleInRoomDirectory) {
                        is AsyncData.Success -> AsyncData.Success(!edited.data)
                        else -> edited
                    }
                }
                SecurityAndPrivacyEvents.EditRoomAddress -> navigator.openEditRoomAddress()
                SecurityAndPrivacyEvents.CancelEnableEncryption -> {
                    showEnableEncryptionConfirmation = false
                }
                SecurityAndPrivacyEvents.ConfirmEnableEncryption -> {
                    showEnableEncryptionConfirmation = false
                    editedIsEncrypted = true
                }
                SecurityAndPrivacyEvents.DismissSaveError -> {
                    saveAction.value = AsyncAction.Uninitialized
                }
            }
        }

        val state = SecurityAndPrivacyState(
            savedSettings = savedSettings,
            editedSettings = editedSettings,
            homeserverName = homeserverName,
            showEnableEncryptionConfirmation = showEnableEncryptionConfirmation,
            saveAction = saveAction.value,
            permissions = permissions,
            eventSink = ::handleEvents
        )

        // If the history visibility is not available for the current access, use the fallback.
        LaunchedEffect(state.availableHistoryVisibilities) {
            if (editedSettings.historyVisibility !in state.availableHistoryVisibilities) {
                editedHistoryVisibility = editedSettings.historyVisibility.fallback()
            }
        }
        return state
    }

    private fun CoroutineScope.isRoomVisibleInRoomDirectory(isRoomVisible: MutableState<AsyncData<Boolean>>) = launch {
        isRoomVisible.runUpdatingState {
            room.getRoomVisibility().map { it == RoomVisibility.Public }
        }
    }

    private fun CoroutineScope.save(
        saveAction: MutableState<AsyncAction<Unit>>,
        isVisibleInRoomDirectory: MutableState<AsyncData<Boolean>>,
        savedSettings: SecurityAndPrivacySettings,
        editedSettings: SecurityAndPrivacySettings,
    ) = launch {
        suspend {
            val enableEncryption = async {
                if (editedSettings.isEncrypted && !savedSettings.isEncrypted) {
                    room.enableEncryption()
                } else {
                    Result.success(Unit)
                }
            }
            val updateHistoryVisibility = async {
                if (editedSettings.historyVisibility != savedSettings.historyVisibility) {
                    room.updateHistoryVisibility(editedSettings.historyVisibility.map())
                } else {
                    Result.success(Unit)
                }
            }
            val updateJoinRule = async {
                val joinRule = editedSettings.roomAccess.map()
                if (editedSettings.roomAccess != savedSettings.roomAccess && joinRule != null) {
                    room.updateJoinRule(joinRule)
                } else {
                    Result.success(Unit)
                }
            }
            val updateRoomVisibility = async {
                // When a user changes join rules to something other than knock or public,
                // the room should be automatically made invisible (private) in the room directory.
                val editedIsVisibleInRoomDirectory = when (editedSettings.roomAccess) {
                    SecurityAndPrivacyRoomAccess.AskToJoin,
                    SecurityAndPrivacyRoomAccess.Anyone -> editedSettings.isVisibleInRoomDirectory.dataOrNull()
                    else -> false
                }
                val savedIsVisibleInRoomDirectory = savedSettings.isVisibleInRoomDirectory.dataOrNull()
                if (editedIsVisibleInRoomDirectory != null && editedIsVisibleInRoomDirectory != savedIsVisibleInRoomDirectory) {
                    val roomVisibility = if (editedIsVisibleInRoomDirectory) RoomVisibility.Public else RoomVisibility.Private
                    room
                        .updateRoomVisibility(roomVisibility)
                        .onSuccess {
                            isVisibleInRoomDirectory.value = AsyncData.Success(editedIsVisibleInRoomDirectory)
                        }
                } else {
                    Result.success(Unit)
                }
            }
            val artificialDelay = async {
                // Artificial delay to make sure the user sees the loading state
                delay(500)
                Result.success(Unit)
            }
            val results = awaitAll(
                enableEncryption,
                updateHistoryVisibility,
                updateJoinRule,
                updateRoomVisibility,
                artificialDelay
            )
            if (results.any { it.isFailure }) {
                throw SecurityAndPrivacyFailures.SaveFailed
            }
        }.runCatchingUpdatingState(saveAction)
    }
}

private fun JoinRule?.map(): SecurityAndPrivacyRoomAccess {
    return when (this) {
        JoinRule.Public -> SecurityAndPrivacyRoomAccess.Anyone
        JoinRule.Knock, is JoinRule.KnockRestricted -> SecurityAndPrivacyRoomAccess.AskToJoin
        is JoinRule.Restricted -> SecurityAndPrivacyRoomAccess.SpaceMember
        JoinRule.Invite -> SecurityAndPrivacyRoomAccess.InviteOnly
        // All other cases are not supported so we default to InviteOnly
        is JoinRule.Custom,
        JoinRule.Private,
        null -> SecurityAndPrivacyRoomAccess.InviteOnly
    }
}

private fun SecurityAndPrivacyRoomAccess.map(): JoinRule? {
    return when (this) {
        SecurityAndPrivacyRoomAccess.Anyone -> JoinRule.Public
        SecurityAndPrivacyRoomAccess.AskToJoin -> JoinRule.Knock
        SecurityAndPrivacyRoomAccess.InviteOnly -> JoinRule.Private
        // SpaceMember can't be selected in the ui
        SecurityAndPrivacyRoomAccess.SpaceMember -> null
    }
}

private fun RoomHistoryVisibility?.map(): SecurityAndPrivacyHistoryVisibility {
    return when (this) {
        RoomHistoryVisibility.WorldReadable -> SecurityAndPrivacyHistoryVisibility.Anyone
        RoomHistoryVisibility.Joined,
        RoomHistoryVisibility.Invited -> SecurityAndPrivacyHistoryVisibility.SinceInvite
        RoomHistoryVisibility.Shared -> SecurityAndPrivacyHistoryVisibility.SinceSelection
        // All other cases are not supported so we default to SinceSelection
        is RoomHistoryVisibility.Custom,
        null -> SecurityAndPrivacyHistoryVisibility.SinceSelection
    }
}

private fun SecurityAndPrivacyHistoryVisibility.map(): RoomHistoryVisibility {
    return when (this) {
        SecurityAndPrivacyHistoryVisibility.SinceSelection -> RoomHistoryVisibility.Shared
        SecurityAndPrivacyHistoryVisibility.SinceInvite -> RoomHistoryVisibility.Invited
        SecurityAndPrivacyHistoryVisibility.Anyone -> RoomHistoryVisibility.WorldReadable
    }
}

private fun RoomInfo.firstDisplayableAlias(serverName: String): RoomAlias? {
    return aliases.firstOrNull { it.matchesServer(serverName) } ?: aliases.firstOrNull()
}
