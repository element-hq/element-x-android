/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.startchat.impl

import io.element.android.features.startchat.StartChatNavigator
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias

class FakeStartChatNavigator(
    private val openRoomLambda: (roomIdOrAlias: RoomIdOrAlias, serverNames: List<String>) -> Unit = { _, _ -> },
    private val createNewRoomLambda: () -> Unit = {},
    private val showJoinRoomByAddressLambda: () -> Unit = {},
    private val dismissJoinRoomByAddressLambda: () -> Unit = {},
    private val openRoomDirectoryLambda: () -> Unit = {},
) : StartChatNavigator {
    override fun onRoomCreated(roomIdOrAlias: RoomIdOrAlias, serverNames: List<String>) {
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
