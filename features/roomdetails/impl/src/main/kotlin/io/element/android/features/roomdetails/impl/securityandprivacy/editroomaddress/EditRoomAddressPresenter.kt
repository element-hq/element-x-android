/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.securityandprivacy.editroomaddress

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.roomdetails.impl.securityandprivacy.SecurityAndPrivacyNavigator
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.alias.RoomAliasHelper
import io.element.android.libraries.matrix.api.roomAliasFromName
import io.element.android.libraries.matrix.ui.room.address.RoomAddressValidity
import io.element.android.libraries.matrix.ui.room.address.RoomAddressValidityEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class EditRoomAddressPresenter @AssistedInject constructor(
    @Assisted private val navigator: SecurityAndPrivacyNavigator,
    private val client: MatrixClient,
    private val room: MatrixRoom,
    private val roomAliasHelper: RoomAliasHelper,
) : Presenter<EditRoomAddressState> {
    @AssistedFactory
    interface Factory {
        fun create(navigator: SecurityAndPrivacyNavigator): EditRoomAddressPresenter
    }

    @Composable
    override fun present(): EditRoomAddressState {
        val coroutineScope = rememberCoroutineScope()
        val homeserverName = remember { client.userIdServerName() }
        val roomAddressValidity = remember {
            mutableStateOf<RoomAddressValidity>(RoomAddressValidity.Unknown)
        }
        val savedRoomAddress = remember { room.firstAliasMatching(homeserverName)?.addressName() }
        val saveAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }
        var newRoomAddress by remember {
            mutableStateOf(
                savedRoomAddress ?: roomAliasHelper.roomAliasNameFromRoomDisplayName(room.displayName)
            )
        }

        fun handleEvents(event: EditRoomAddressEvents) {
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
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.save(
        saveAction: MutableState<AsyncAction<Unit>>,
        serverName: String,
        newRoomAddress: String,
    ) = launch {
        suspend {
            val savedCanonicalAlias = room.canonicalAlias
            val savedAliasFromHomeserver = room.firstAliasMatching(serverName)
            val newRoomAlias = client.roomAliasFromName(newRoomAddress).getOrThrow()

            // First publish the new alias in the room directory
            room.publishRoomAliasInRoomDirectory(newRoomAlias).getOrThrow()
            // Then try remove the old alias from the room directory
            if (savedAliasFromHomeserver != null) {
                room.removeRoomAliasFromRoomDirectory(savedAliasFromHomeserver).getOrThrow()
            }
            // Finally update the canonical alias state..
            when {
                // Allow to update the canonical alias only if the saved canonical alias matches the homeserver or if there is no canonical alias
                savedCanonicalAlias == null || savedCanonicalAlias.matchesServer(serverName) -> {
                    room.updateCanonicalAlias(newRoomAlias, room.alternativeAliases).getOrThrow()
                }
                // Otherwise, update the alternative aliases and keep the current canonical alias
                else -> {
                    val newAlternativeAliases = listOf(newRoomAlias) + room.alternativeAliases
                    room.updateCanonicalAlias(savedCanonicalAlias, newAlternativeAliases).getOrThrow()
                }
            }
            navigator.closeEditorRoomAddress()
        }.runCatchingUpdatingState(saveAction)
    }
}

/**
 * Returns the first alias that matches the given server name, or null if none match.
 */
private fun MatrixRoom.firstAliasMatching(serverName: String): RoomAlias? {
    // Check if the canonical alias matches the homeserver
    if (canonicalAlias?.matchesServer(serverName) == true) {
        return canonicalAlias
    }
    return alternativeAliases.firstOrNull { it.value.contains(serverName) }
}
