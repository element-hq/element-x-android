/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.room.address

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.room.alias.RoomAliasHelper
import io.element.android.libraries.matrix.api.roomAliasFromName
import kotlinx.coroutines.delay

@Composable
fun RoomAddressValidityEffect(
    client: MatrixClient,
    roomAliasHelper: RoomAliasHelper,
    newRoomAddress: String,
    knownRoomAddress: String?,
    onRoomAddressValidityChange: (RoomAddressValidity) -> Unit,
) {
    val onChange by rememberUpdatedState(onRoomAddressValidityChange)
    LaunchedEffect(newRoomAddress) {
        if (newRoomAddress.isEmpty() || newRoomAddress == knownRoomAddress) {
            onChange(RoomAddressValidity.Unknown)
            return@LaunchedEffect
        }
        // debounce the room address validation
        delay(300)
        val roomAlias = client.roomAliasFromName(newRoomAddress)
        if (roomAlias == null || !roomAliasHelper.isRoomAliasValid(roomAlias)) {
            onChange(RoomAddressValidity.InvalidSymbols)
        } else {
            client.resolveRoomAlias(roomAlias)
                .onSuccess { resolved ->
                    if (resolved.isPresent) {
                        onChange(RoomAddressValidity.NotAvailable)
                    } else {
                        onChange(RoomAddressValidity.Valid)
                    }
                }
                .onFailure {
                    onChange(RoomAddressValidity.Valid)
                }
        }
    }
}
