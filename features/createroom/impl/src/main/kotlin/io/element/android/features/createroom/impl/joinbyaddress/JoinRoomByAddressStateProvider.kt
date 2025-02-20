/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.joinbyaddress

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class JoinRoomByAddressStateProvider : PreviewParameterProvider<JoinRoomByAddressState> {
    override val values: Sequence<JoinRoomByAddressState>
        get() = sequenceOf(
            aJoinRoomByAddressState(),
            aJoinRoomByAddressState("#room-"),
            aJoinRoomByAddressState("#room-", addressState = RoomAddressState.Invalid),
            aJoinRoomByAddressState("#room-name:matrix.org", addressState = RoomAddressState.Valid(true)),
            aJoinRoomByAddressState("#room-name-here:matrix.org", addressState = RoomAddressState.Valid(false)),
            // Add other states here
        )
}

fun aJoinRoomByAddressState(
    address: String = "",
    addressState: RoomAddressState = RoomAddressState.Unknown,
) = JoinRoomByAddressState(
    address = address,
    addressState = addressState,
    eventSink = {}
)
