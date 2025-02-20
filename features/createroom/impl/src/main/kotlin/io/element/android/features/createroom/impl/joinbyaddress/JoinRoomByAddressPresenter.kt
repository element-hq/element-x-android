/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.joinbyaddress

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.createroom.CreateRoomNavigator
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.room.alias.RoomAliasHelper
import kotlinx.coroutines.delay

class JoinRoomByAddressPresenter @AssistedInject constructor(
    @Assisted private val navigator: CreateRoomNavigator,
    private val client: MatrixClient,
    private val roomAliasHelper: RoomAliasHelper,
) : Presenter<JoinRoomByAddressState> {

    @AssistedFactory
    interface Factory {
        fun create(navigator: CreateRoomNavigator): JoinRoomByAddressPresenter
    }

    @Composable
    override fun present(): JoinRoomByAddressState {
        var address by remember { mutableStateOf("") }
        var addressState by remember { mutableStateOf<RoomAddressState>(RoomAddressState.Unknown) }

        fun handleEvents(event: JoinRoomByAddressEvents) {
            when (event) {
                JoinRoomByAddressEvents.Continue -> {
                    navigator.onDismissJoinRoomByAddress()
                    navigator.onOpenRoom(RoomIdOrAlias.Alias(RoomAlias(address)))
                }
                JoinRoomByAddressEvents.Dismiss -> navigator.onDismissJoinRoomByAddress()
                is JoinRoomByAddressEvents.UpdateAddress -> {
                    address = event.address.trim()
                }
            }
        }

        RoomAddressStateEffect(
            fullAddress = address,
            onRoomAddressStateChange = { addressState = it }
        )

        return JoinRoomByAddressState(
            address = address,
            addressState = addressState,
            eventSink = ::handleEvents
        )
    }

    @Composable
    private fun RoomAddressStateEffect(
        fullAddress: String,
        onRoomAddressStateChange: (RoomAddressState) -> Unit,
    ) {
        val onChange by rememberUpdatedState(onRoomAddressStateChange)
        LaunchedEffect(fullAddress) {
            if (fullAddress.isEmpty()) {
                onChange(RoomAddressState.Unknown)
                return@LaunchedEffect
            }
            // debounce the room address validation
            delay(300)
            val roomAlias = tryOrNull { RoomAlias(fullAddress) }
            if (roomAlias == null || !roomAliasHelper.isRoomAliasValid(roomAlias)) {
                onChange(RoomAddressState.Invalid)
            } else {
                onChange(RoomAddressState.Valid(matchingRoomFound = false))
                client.resolveRoomAlias(roomAlias)
                    .onSuccess { resolved ->
                        onChange(RoomAddressState.Valid(matchingRoomFound = resolved.isPresent))
                    }
            }
        }
    }
}



