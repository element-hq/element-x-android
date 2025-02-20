/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.joinbyaddress

import androidx.compose.runtime.Immutable

data class JoinRoomByAddressState(
    val address: String,
    val addressState: RoomAddressState,
    val eventSink: (JoinRoomByAddressEvents) -> Unit
)

@Immutable
sealed interface RoomAddressState {
    data object Unknown : RoomAddressState
    data object Invalid : RoomAddressState
    data class Valid(val matchingRoomFound: Boolean) : RoomAddressState
}
