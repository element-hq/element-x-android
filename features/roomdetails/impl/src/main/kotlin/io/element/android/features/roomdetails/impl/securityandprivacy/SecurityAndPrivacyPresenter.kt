/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.securityandprivacy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.roomdetails.impl.securityandprivacy.editroomaddress.matchesServer
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import io.element.android.libraries.matrix.api.room.join.JoinRule
import java.util.Optional

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
        val homeserverName = remember { matrixClient.userIdServerName() }
        val roomInfo by room.roomInfoFlow.collectAsState(initial = null)

        val isVisibleInRoomDirectory = remember {
            mutableStateOf<AsyncData<Boolean>>(AsyncData.Uninitialized)
        }

        val savedSettings by remember {
            derivedStateOf {
                SecurityAndPrivacySettings(
                    roomAccess = roomInfo?.joinRule.map(),
                    isEncrypted = room.isEncrypted,
                    isVisibleInRoomDirectory = Optional.ofNullable(isVisibleInRoomDirectory.value),
                    historyVisibility = Optional.ofNullable(roomInfo?.historyVisibility?.map()),
                    addressName = Optional.ofNullable(roomInfo?.firstDisplayableAlias(homeserverName)?.value),
                )
            }
        }

        var currentRoomAccess by remember(savedSettings.roomAccess) {
            mutableStateOf(savedSettings.roomAccess)
        }
        var currentHistoryVisibility by remember(savedSettings.historyVisibility) {
            mutableStateOf(savedSettings.historyVisibility)
        }
        var currentVisibleInRoomDirectory by remember(savedSettings.isVisibleInRoomDirectory) {
            mutableStateOf(savedSettings.isVisibleInRoomDirectory)
        }
        var currentIsEncrypted by remember(savedSettings.isEncrypted) {
            mutableStateOf(savedSettings.isEncrypted)
        }

        var showEncryptionConfirmation by remember { mutableStateOf(false) }

        val currentSettings = SecurityAndPrivacySettings(
            roomAccess = currentRoomAccess,
            isEncrypted = currentIsEncrypted,
            isVisibleInRoomDirectory = currentVisibleInRoomDirectory,
            historyVisibility = currentHistoryVisibility,
            addressName = savedSettings.addressName,
        )

        fun handleEvents(event: SecurityAndPrivacyEvents) {
            when (event) {
                SecurityAndPrivacyEvents.Save -> {
                }
                is SecurityAndPrivacyEvents.ChangeRoomAccess -> {
                    currentRoomAccess = event.roomAccess
                }
                is SecurityAndPrivacyEvents.ToggleEncryptionState -> {
                    if(currentSettings.isEncrypted) {
                        currentIsEncrypted = false
                    } else {
                        showEncryptionConfirmation = true
                    }
                }
                is SecurityAndPrivacyEvents.ChangeHistoryVisibility -> {
                    currentHistoryVisibility = Optional.of(event.historyVisibility)
                }
                is SecurityAndPrivacyEvents.ChangeRoomVisibility -> {
                    currentVisibleInRoomDirectory = Optional.of(AsyncData.Success(event.isVisibleInRoomDirectory))
                }
                SecurityAndPrivacyEvents.EditRoomAddress -> navigator.openEditRoomAddress()
                SecurityAndPrivacyEvents.CancelEnableEncryption -> {
                    showEncryptionConfirmation = false
                }
                SecurityAndPrivacyEvents.ConfirmEnableEncryption -> {
                    showEncryptionConfirmation = false
                    currentIsEncrypted = true
                }
            }
        }
        return SecurityAndPrivacyState(
            savedSettings = savedSettings,
            currentSettings = currentSettings,
            homeserverName = homeserverName,
            showEncryptionConfirmation = showEncryptionConfirmation,
            eventSink = ::handleEvents
        )
    }
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
    return aliases.firstOrNull { it.matchesServer(serverName)} ?: aliases.firstOrNull()
}

