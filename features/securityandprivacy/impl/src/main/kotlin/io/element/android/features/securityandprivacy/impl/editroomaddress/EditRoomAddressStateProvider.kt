/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.editroomaddress

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.ui.room.address.RoomAddressValidity

open class EditRoomAddressStateProvider : PreviewParameterProvider<EditRoomAddressState> {
    override val values: Sequence<EditRoomAddressState>
        get() = sequenceOf(
            anEditRoomAddressState(),
            anEditRoomAddressState(roomAddressValidity = RoomAddressValidity.NotAvailable),
            anEditRoomAddressState(roomAddressValidity = RoomAddressValidity.InvalidSymbols),
            anEditRoomAddressState(roomAddressValidity = RoomAddressValidity.Valid),
            anEditRoomAddressState(roomAddressValidity = RoomAddressValidity.Valid, saveAction = AsyncAction.Loading),
        )
}

fun anEditRoomAddressState(
    roomAddress: String = "therapy",
    roomAddressValidity: RoomAddressValidity = RoomAddressValidity.Unknown,
    homeserverName: String = ":myserver.org",
    saveAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (EditRoomAddressEvents) -> Unit = {}
) = EditRoomAddressState(
    roomAddress = roomAddress,
    roomAddressValidity = roomAddressValidity,
    homeserverName = homeserverName,
    saveAction = saveAction,
    eventSink = eventSink
)
