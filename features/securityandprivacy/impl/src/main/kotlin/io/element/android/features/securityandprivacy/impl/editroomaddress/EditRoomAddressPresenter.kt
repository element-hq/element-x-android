/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.editroomaddress

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.securityandprivacy.impl.SecurityAndPrivacyNavigator
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.alias.RoomAliasHelper
import io.element.android.libraries.matrix.api.roomAliasFromName
import io.element.android.libraries.matrix.ui.room.address.RoomAddressValidity
import io.element.android.libraries.matrix.ui.room.address.RoomAddressValidityEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AssistedInject
class EditRoomAddressPresenter(
    @Assisted private val navigator: SecurityAndPrivacyNavigator,
    private val client: MatrixClient,
    private val room: JoinedRoom,
    private val roomAliasHelper: RoomAliasHelper,
) : Presenter<EditRoomAddressState> {
    @AssistedFactory
    interface Factory {
        fun create(navigator: SecurityAndPrivacyNavigator): EditRoomAddressPresenter
    }

    @Composable
    override fun present(): EditRoomAddressState {
        val coroutineScope = rememberCoroutineScope()
        val roomInfo by room.roomInfoFlow.collectAsState()
        val homeserverName = remember { client.userIdServerName() }
        val roomAddressValidity = remember {
            mutableStateOf<RoomAddressValidity>(RoomAddressValidity.Unknown)
        }
        val savedRoomAddress by remember { derivedStateOf { roomInfo.firstAliasMatching(homeserverName)?.addressName() } }
        val saveAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }
        var newRoomAddress by remember {
            mutableStateOf(
                savedRoomAddress ?: roomAliasHelper.roomAliasNameFromRoomDisplayName(roomInfo.name.orEmpty())
            )
        }

        fun handleEvent(event: EditRoomAddressEvents) {
            when (event) {
                EditRoomAddressEvents.Save -> coroutineScope.save(
                    saveAction = saveAction,
                    serverName = homeserverName,
                    newRoomAddress = newRoomAddress
                )
                is EditRoomAddressEvents.RoomAddressChanged -> {
                    newRoomAddress = event.roomAddress
                }
                EditRoomAddressEvents.DismissError -> {
                    saveAction.value = AsyncAction.Uninitialized
                }
            }
        }

        RoomAddressValidityEffect(
            client = client,
            roomAliasHelper = roomAliasHelper,
            newRoomAddress = newRoomAddress,
            knownRoomAddress = savedRoomAddress
        ) { newRoomAddressValidity ->
            roomAddressValidity.value = newRoomAddressValidity
        }

        return EditRoomAddressState(
            homeserverName = homeserverName,
            roomAddressValidity = roomAddressValidity.value,
            roomAddress = newRoomAddress,
            saveAction = saveAction.value,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.save(
        saveAction: MutableState<AsyncAction<Unit>>,
        serverName: String,
        newRoomAddress: String,
    ) = launch {
        suspend {
            val roomInfo = room.info()
            val savedCanonicalAlias = roomInfo.canonicalAlias
            val savedAliasFromHomeserver = roomInfo.firstAliasMatching(serverName)
            val newRoomAlias = client.roomAliasFromName(newRoomAddress) ?: throw IllegalArgumentException("Invalid room address")

            // First publish the new alias in the room directory
            room.publishRoomAliasInRoomDirectory(newRoomAlias).getOrThrow()
            // Then try remove the old alias from the room directory
            if (savedAliasFromHomeserver != null) {
                room.removeRoomAliasFromRoomDirectory(savedAliasFromHomeserver).getOrThrow()
            }

            // Finally update the canonical alias state
            when {
                // Allow to update the canonical alias only if the saved canonical alias matches the homeserver or if there is no canonical alias
                savedCanonicalAlias == null || savedCanonicalAlias.matchesServer(serverName) -> {
                    val newAlternativeAliases = roomInfo.alternativeAliases.filter { it != savedAliasFromHomeserver }
                    room.updateCanonicalAlias(newRoomAlias, newAlternativeAliases).getOrThrow()
                }
                // Otherwise, only update the alternative aliases and keep the current canonical alias
                else -> {
                    val newAlternativeAliases = buildList {
                        // New alias is added first, so we make sure we pick it first
                        add(newRoomAlias)
                        // Add all other aliases, except the one we just removed from the room directory
                        addAll(roomInfo.alternativeAliases.filter { it != savedAliasFromHomeserver })
                    }
                    room.updateCanonicalAlias(savedCanonicalAlias, newAlternativeAliases).getOrThrow()
                }
            }
            navigator.closeEditRoomAddress()
        }.runCatchingUpdatingState(saveAction)
    }
}

/**
 * Returns the first alias that matches the given server name, or null if none match.
 */
private fun RoomInfo.firstAliasMatching(serverName: String): RoomAlias? {
    // Check if the canonical alias matches the homeserver
    if (canonicalAlias?.matchesServer(serverName) == true) {
        return canonicalAlias
    }
    return alternativeAliases.firstOrNull { it.matchesServer(serverName) }
}
