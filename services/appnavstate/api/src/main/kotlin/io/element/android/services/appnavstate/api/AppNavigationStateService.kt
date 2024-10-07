/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.services.appnavstate.api

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.SpaceId
import io.element.android.libraries.matrix.api.core.ThreadId
import kotlinx.coroutines.flow.StateFlow

/**
 * A service that tracks the navigation and foreground states of the app.
 */
interface AppNavigationStateService {
    val appNavigationState: StateFlow<AppNavigationState>

    fun onNavigateToSession(owner: String, sessionId: SessionId)
    fun onLeavingSession(owner: String)

    fun onNavigateToSpace(owner: String, spaceId: SpaceId)
    fun onLeavingSpace(owner: String)

    fun onNavigateToRoom(owner: String, roomId: RoomId)
    fun onLeavingRoom(owner: String)

    fun onNavigateToThread(owner: String, threadId: ThreadId)
    fun onLeavingThread(owner: String)
}
