/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl

import io.element.android.features.createroom.CreateRoomNavigator
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias

class FakeCreateRoomNavigator(
    private val openRoomLambda: (roomIdOrAlias: RoomIdOrAlias, serverNames: List<String>) -> Unit = { _, _ -> },
    private val createNewRoomLambda: () -> Unit = {},
    private val showJoinRoomByAddressLambda: () -> Unit = {},
    private val dismissJoinRoomByAddressLambda: () -> Unit = {},
    private val openRoomDirectoryLambda: () -> Unit = {},
) : CreateRoomNavigator {
    override fun onOpenRoom(roomIdOrAlias: RoomIdOrAlias, serverNames: List<String>) {
        openRoomLambda(roomIdOrAlias, serverNames)
    }

    override fun onCreateNewRoom() {
        createNewRoomLambda()
    }

    override fun onShowJoinRoomByAddress() {
        showJoinRoomByAddressLambda()
    }

    override fun onDismissJoinRoomByAddress() {
        dismissJoinRoomByAddressLambda()
    }

    override fun onOpenRoomDirectory() {
        openRoomDirectoryLambda()
    }
}
