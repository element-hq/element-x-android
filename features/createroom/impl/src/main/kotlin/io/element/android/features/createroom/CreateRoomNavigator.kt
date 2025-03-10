/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom

import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import io.element.android.features.createroom.impl.CreateRoomFlowNode.NavTarget
import io.element.android.libraries.architecture.overlay.Overlay
import io.element.android.libraries.architecture.overlay.operation.hide
import io.element.android.libraries.architecture.overlay.operation.show
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias

interface CreateRoomNavigator : Plugin {
    fun onOpenRoom(roomIdOrAlias: RoomIdOrAlias, serverNames: List<String>)
    fun onCreateNewRoom()
    fun onShowJoinRoomByAddress()
    fun onDismissJoinRoomByAddress()
    fun onOpenRoomDirectory()
}

class DefaultCreateRoomNavigator(
    private val backstack: BackStack<NavTarget>,
    private val overlay: Overlay<NavTarget>,
    private val openRoom: (RoomIdOrAlias, List<String>) -> Unit,
    private val openRoomDirectory: () -> Unit,
) : CreateRoomNavigator {
    override fun onOpenRoom(roomIdOrAlias: RoomIdOrAlias, serverNames: List<String>) = openRoom(roomIdOrAlias, serverNames)

    override fun onOpenRoomDirectory() = openRoomDirectory()

    override fun onCreateNewRoom() {
        backstack.push(NavTarget.NewRoom)
    }

    override fun onShowJoinRoomByAddress() {
        overlay.show(NavTarget.JoinByAddress)
    }

    override fun onDismissJoinRoomByAddress() {
        overlay.hide()
    }
}
