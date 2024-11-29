/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.appnav

import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias

/**
 * Plugin that will be notified when we want to navigate back to a room from a screen that is a descendant of it.
 */
class NavigateBackToRoomObserverPlugin : Plugin {
    private val listeners = mutableListOf<(RoomIdOrAlias) -> Unit>()

    fun addListener(listener: (RoomIdOrAlias) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (RoomIdOrAlias) -> Unit) {
        listeners.remove(listener)
    }

    fun navigateBackToRoom(roomIdOrAlias: RoomIdOrAlias) {
        listeners.forEach { it(roomIdOrAlias) }
    }
}
