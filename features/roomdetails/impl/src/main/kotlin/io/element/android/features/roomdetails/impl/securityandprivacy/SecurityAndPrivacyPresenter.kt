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
import androidx.compose.runtime.State
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
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.roomdirectory.RoomVisibility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class SecurityAndPrivacyPresenter @AssistedInject constructor(
    @Assisted private val navigator: SecurityAndPrivacyNavigator,
    private val matrixClient: MatrixClient,
    private val room: MatrixRoom,
) : Presenter<SecurityAndPrivacyState> {
    @AssistedFactory
    interface Factory {
        fun create(navigator: SecurityAndPrivacyNavigator): SecurityAndPrivacyPresenter
    }

    @Composable
    override fun present(): SecurityAndPrivacyState {
        val coroutineScope = rememberCoroutineScope()
        val homeserverName = remember { matrixClient.userIdServerName() }
        val roomInfo by room.roomInfoFlow.collectAsState(initial = null)
        val isVisibleInRoomDirectory by isRoomVisibleInRoomDirectory()

        val savedSettings by remember {
            derivedStateOf {
                SecurityAndPrivacySettings(
                    roomAccess = roomInfo?.joinRule.map(),
                    isEncrypted = room.isEncrypted,
                    isVisibleInRoomDirectory = isVisibleInRoomDirectory,
                    historyVisibility = roomInfo?.historyVisibility?.map(),
                    addressName = roomInfo?.firstDisplayableAlias(homeserverName)?.value
                )
            }
        }

        var editedRoomAccess by remember(savedSettings.roomAccess) {
            mutableStateOf(savedSettings.roomAccess)
        }
        var editedHistoryVisibility by remember(savedSettings.historyVisibility) {
            mutableStateOf(savedSettings.historyVisibility)
        }
        var editedVisibleInRoomDirectory by remember(savedSettings.isVisibleInRoomDirectory) {
            mutableStateOf(savedSettings.isVisibleInRoomDirectory)
        }
        var editedIsEncrypted by remember(savedSettings.isEncrypted) {
            mutableStateOf(savedSettings.isEncrypted)
        }

        var showEncryptionConfirmation by remember(savedSettings.isEncrypted) { mutableStateOf(false) }

        val editedSettings = SecurityAndPrivacySettings(
            roomAccess = editedRoomAccess,
            isEncrypted = editedIsEncrypted,
            isVisibleInRoomDirectory = editedVisibleInRoomDirectory,
            historyVisibility = editedHistoryVisibility,
            addressName = savedSettings.addressName,
        )

        val saveAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }

        fun handleEvents(event: SecurityAndPrivacyEvents) {
            when (event) {
                SecurityAndPrivacyEvents.Save -> {
                    coroutineScope.save(saveAction, savedSettings, editedSettings)
                }
                is SecurityAndPrivacyEvents.ChangeRoomAccess -> {
                    editedRoomAccess = event.roomAccess
                }
                is SecurityAndPrivacyEvents.ToggleEncryptionState -> {
                    if (editedSettings.isEncrypted) {
                        editedIsEncrypted = false
                    } else {
                        showEncryptionConfirmation = true
                    }
                }
                is SecurityAndPrivacyEvents.ChangeHistoryVisibility -> {
                    editedHistoryVisibility = event.historyVisibility
                }
                is SecurityAndPrivacyEvents.ChangeRoomVisibility -> {
                    editedVisibleInRoomDirectory = AsyncData.Success(event.isVisibleInRoomDirectory)
                }
                SecurityAndPrivacyEvents.EditRoomAddress -> navigator.openEditRoomAddress()
                SecurityAndPrivacyEvents.CancelEnableEncryption -> {
                    showEncryptionConfirmation = false
                }
                SecurityAndPrivacyEvents.ConfirmEnableEncryption -> {
                    showEncryptionConfirmation = false
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
            showEncryptionConfirmation = showEncryptionConfirmation,
            saveAction = saveAction.value,
            eventSink = ::handleEvents
        )
        LaunchedEffect(state.availableHistoryVisibilities) {
            editedSettings.historyVisibility?.also {
                if (it !in state.availableHistoryVisibilities) {
                    editedHistoryVisibility = it.fallback()
                }
            }
        }
        return state
    }

    @Composable
    private fun isRoomVisibleInRoomDirectory(): State<AsyncData<Boolean>> {
        val result = remember { mutableStateOf<AsyncData<Boolean>>(AsyncData.Uninitialized) }
        LaunchedEffect(Unit) {
            result.runUpdatingState {
                room.getRoomVisibility().map { it == RoomVisibility.Public }
            }
        }
        return result
    }

    private fun CoroutineScope.save(
        saveAction: MutableState<AsyncAction<Unit>>,
        savedSettings: SecurityAndPrivacySettings,
        editedSettings: SecurityAndPrivacySettings,
    ) = launch {
        suspend {
            var somethingWentWrong = false
            if (editedSettings.isEncrypted && !savedSettings.isEncrypted) {
                room
                    .enableEncryption()
                    .onFailure {
                        Timber.d("Failed to enable encryption")
                        somethingWentWrong = true
                    }
            }
            if (editedSettings.historyVisibility != null && editedSettings.historyVisibility != savedSettings.historyVisibility) {
                room
                    .updateHistoryVisibility(editedSettings.historyVisibility.map())
                    .onFailure {
                        Timber.d("Failed to update history visibility")
                        somethingWentWrong = true
                    }
            }
            if (editedSettings.roomAccess != savedSettings.roomAccess) {
                room
                    .updateJoinRule(editedSettings.roomAccess.map())
                    .onFailure {
                        Timber.d("Failed to update join rule")
                        somethingWentWrong = true
                    }
            }

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
                    .onFailure {
                        Timber.d("Failed to update room visibility")
                        somethingWentWrong = true
                    }
            }
            if (somethingWentWrong) {
                error("")
            }
        }.runCatchingUpdatingState(saveAction)
    }

    private fun JoinRule?.map(): SecurityAndPrivacyRoomAccess {
        return when (this) {
            JoinRule.Public -> SecurityAndPrivacyRoomAccess.Anyone
            JoinRule.Knock, is JoinRule.KnockRestricted -> SecurityAndPrivacyRoomAccess.AskToJoin
            is JoinRule.Restricted -> SecurityAndPrivacyRoomAccess.SpaceMember
            is JoinRule.Custom,
            JoinRule.Invite,
            JoinRule.Private,
            null -> SecurityAndPrivacyRoomAccess.InviteOnly
        }
    }

    private fun SecurityAndPrivacyRoomAccess.map(): JoinRule {
        return when (this) {
            SecurityAndPrivacyRoomAccess.Anyone -> JoinRule.Public
            SecurityAndPrivacyRoomAccess.AskToJoin -> JoinRule.Knock
            SecurityAndPrivacyRoomAccess.InviteOnly -> JoinRule.Private
            SecurityAndPrivacyRoomAccess.SpaceMember -> error("Unsupported")
        }
    }

    private fun RoomHistoryVisibility.map(): SecurityAndPrivacyHistoryVisibility {
        return when (this) {
            RoomHistoryVisibility.Joined,
            RoomHistoryVisibility.Invited -> SecurityAndPrivacyHistoryVisibility.SinceInvite
            RoomHistoryVisibility.Shared,
            is RoomHistoryVisibility.Custom -> SecurityAndPrivacyHistoryVisibility.SinceSelection
            RoomHistoryVisibility.WorldReadable -> SecurityAndPrivacyHistoryVisibility.Anyone
        }
    }

    private fun SecurityAndPrivacyHistoryVisibility.map(): RoomHistoryVisibility {
        return when (this) {
            SecurityAndPrivacyHistoryVisibility.SinceSelection -> RoomHistoryVisibility.Shared
            SecurityAndPrivacyHistoryVisibility.SinceInvite -> RoomHistoryVisibility.Invited
            SecurityAndPrivacyHistoryVisibility.Anyone -> RoomHistoryVisibility.WorldReadable
        }
    }

    private fun MatrixRoomInfo.firstDisplayableAlias(serverName: String): RoomAlias? {
        return aliases.firstOrNull { it.matchesServer(serverName) } ?: aliases.firstOrNull()
    }
}

