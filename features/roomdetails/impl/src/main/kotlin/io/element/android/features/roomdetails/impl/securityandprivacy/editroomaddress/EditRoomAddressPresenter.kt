/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.securityandprivacy.editroomaddress

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.alias.RoomAliasHelper
import io.element.android.libraries.matrix.ui.room.address.RoomAddressValidity
import io.element.android.libraries.matrix.ui.room.address.RoomAddressValidityEffect
import javax.inject.Inject

class EditRoomAddressPresenter @Inject constructor(
    private val client: MatrixClient,
    private val room: MatrixRoom,
    private val roomAliasHelper: RoomAliasHelper,
) : Presenter<EditRoomAddressState> {

    @Composable
    override fun present(): EditRoomAddressState {
        val homeserverName = remember { client.userIdServerName() }
        val roomAddressValidity = remember {
            mutableStateOf<RoomAddressValidity>(RoomAddressValidity.Unknown)
        }
        val savedRoomAddress = remember {
            room.firstAliasMatching(homeserverName)?.roomAddress()
        }
        var currentRoomAddress by remember {
            mutableStateOf(
                savedRoomAddress ?: roomAliasHelper.roomAliasNameFromRoomDisplayName(room.displayName)
            )
        }

        fun handleEvents(event: EditRoomAddressEvents) {
            when (event) {
                EditRoomAddressEvents.Save -> Unit
                is EditRoomAddressEvents.RoomAddressChanged -> {
                    currentRoomAddress = event.roomAddress
                }
            }
        }

        RoomAddressValidityEffect(
            client = client,
            roomAliasHelper = roomAliasHelper,
            newRoomAddress = currentRoomAddress,
            knownRoomAddress = savedRoomAddress
        ) { newRoomAddressValidity ->
            roomAddressValidity.value = newRoomAddressValidity
        }

        return EditRoomAddressState(
            homeserverName = homeserverName,
            roomAddressValidity = roomAddressValidity.value,
            roomAddress = currentRoomAddress,
            eventSink = ::handleEvents
        )
    }

}

private fun MatrixRoom.firstAliasMatching(serverName: String): RoomAlias? {
    // Check if the canonical alias matches the homeserver
    if (this.alias?.matchesServer(serverName) == true) {
        return this.alias
    }
    return this.alternativeAliases.firstOrNull { it.value.contains(serverName) }
}

private fun RoomAlias.roomAddress(): String {
    return value.drop(1).split(":").first()
}

private fun RoomAlias.matchesServer(serverName: String): Boolean {
    return value.contains(serverName)
}
