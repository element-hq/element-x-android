/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.securityandprivacy.editroomaddress

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.ui.room.address.RoomAddressValidity

open class EditRoomAddressStateProvider : PreviewParameterProvider<EditRoomAddressState> {
    override val values: Sequence<EditRoomAddressState>
        get() = sequenceOf(
            aEditRoomAddressState(),
            // Add other states here
        )
}

fun aEditRoomAddressState(
    roomAddress: String = "therapy",
    roomAddressValidity: RoomAddressValidity = RoomAddressValidity.Unknown,
    homeserverName: String = ":myserver.org",
    eventSink: (EditRoomAddressEvents) -> Unit = {}
) = EditRoomAddressState(
    roomAddress = roomAddress,
    roomAddressValidity = roomAddressValidity,
    homeserverName = homeserverName,
    eventSink = eventSink
)
