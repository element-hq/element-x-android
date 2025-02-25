/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.joinbyaddress

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.room.alias.RoomAliasHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

private const val ADDRESS_RESOLVE_TIMEOUT_IN_SECONDS = 10

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
        var internalAddressState by remember { mutableStateOf<RoomAddressState>(RoomAddressState.Unknown) }
        var validateAddress: Boolean by remember { mutableStateOf(false) }

        fun handleEvents(event: JoinRoomByAddressEvents) {
            when (event) {
                JoinRoomByAddressEvents.Continue -> {
                    when (val currentState = internalAddressState) {
                        is RoomAddressState.RoomFound -> onRoomFound(currentState)
                        else -> validateAddress = true
                    }
                }
                JoinRoomByAddressEvents.Dismiss -> navigator.onDismissJoinRoomByAddress()
                is JoinRoomByAddressEvents.UpdateAddress -> {
                    validateAddress = false
                    address = event.address.trim()
                }
            }
        }

        RoomAddressStateEffect(
            fullAddress = address,
            onRoomAddressStateChange = { addressState ->
                internalAddressState = addressState
                if (addressState is RoomAddressState.RoomFound && validateAddress) {
                    onRoomFound(addressState)
                }
            }
        )

        val addressState by remember {
            derivedStateOf {
                // We only want to show the "RoomFound" state as long as the user didn't validate the address.
                if (validateAddress || internalAddressState is RoomAddressState.RoomFound) {
                    internalAddressState
                } else {
                    RoomAddressState.Unknown
                }
            }
        }

        return JoinRoomByAddressState(
            address = address,
            addressState = addressState,
            eventSink = ::handleEvents
        )
    }

    private fun onRoomFound(state: RoomAddressState.RoomFound) {
        navigator.onDismissJoinRoomByAddress()
        navigator.onOpenRoom(
            roomIdOrAlias = state.resolved.roomId.toRoomIdOrAlias(),
            serverNames = state.resolved.servers
        )
    }

    @Composable
    private fun RoomAddressStateEffect(
        fullAddress: String,
        onRoomAddressStateChange: (RoomAddressState) -> Unit,
    ) {
        val onChange by rememberUpdatedState(onRoomAddressStateChange)
        LaunchedEffect(fullAddress) {
            // Whenever the address changes, reset the state to unknown
            onChange(RoomAddressState.Unknown)
            // debounce the room address resolution
            delay(300)
            val roomAlias = tryOrNull { RoomAlias(fullAddress) }
            if (roomAlias != null && roomAliasHelper.isRoomAliasValid(roomAlias)) {
                onChange(RoomAddressState.Resolving)
                onChange(client.resolveRoomAddress(roomAlias))
            } else {
                onChange(RoomAddressState.Invalid)
            }
        }
    }

    private suspend fun MatrixClient.resolveRoomAddress(roomAlias: RoomAlias): RoomAddressState {
        return withTimeoutOrNull(ADDRESS_RESOLVE_TIMEOUT_IN_SECONDS.seconds) {
            resolveRoomAlias(roomAlias)
                .fold(
                    onSuccess = { resolved ->
                        if (resolved.isPresent) {
                            RoomAddressState.RoomFound(resolved.get())
                        } else {
                            RoomAddressState.RoomNotFound
                        }
                    },
                    onFailure = { _ -> RoomAddressState.RoomNotFound }
                )
        } ?: RoomAddressState.RoomNotFound
    }
}
