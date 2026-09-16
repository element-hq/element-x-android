/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.appnavstate.api

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import kotlinx.coroutines.flow.StateFlow

/**
 * A service that tracks the navigation and foreground states of the app.
 */
interface AppNavigationStateService {
    /**
     * Where the user currently is, as a stack that deepens from root to session, room and thread.
     * Used by the components that need to know what is on screen, notably to suppress the notifications of the room being read.
     */
    val appNavigationState: StateFlow<AppNavigationState>

    /**
     * Pushes a session onto the navigation state.
     *
     * @param owner identifies the navigation node reporting this, so that a later leave call can be matched to it.
     * @param sessionId the session that was opened.
     */
    fun onNavigateToSession(owner: String, sessionId: SessionId)

    /**
     * Pops back to the root. Does nothing when [owner] is not the owner of the current state, which is how out-of-order
     * calls from a node that has already been replaced are ignored.
     *
     * @param owner the same value passed to [onNavigateToSession].
     */
    fun onLeavingSession(owner: String)

    /**
     * Pushes a room onto the navigation state. Logs an error and does nothing unless a session was pushed first.
     *
     * @param owner identifies the navigation node reporting this.
     * @param roomId the room that was opened.
     */
    fun onNavigateToRoom(owner: String, roomId: RoomId)

    /**
     * Pops back to the parent session; as with [onLeavingSession], a mismatched [owner] makes this a no-op.
     *
     * @param owner the same value passed to [onNavigateToRoom].
     */
    fun onLeavingRoom(owner: String)

    /**
     * Pushes a thread onto the navigation state. Logs an error and does nothing unless a room was pushed first.
     *
     * @param owner identifies the navigation node reporting this.
     * @param threadId the thread that was opened.
     */
    fun onNavigateToThread(owner: String, threadId: ThreadId)

    /**
     * Pops back to the parent room; as with [onLeavingSession], a mismatched [owner] makes this a no-op.
     *
     * @param owner the same value passed to [onNavigateToThread].
     */
    fun onLeavingThread(owner: String)
}
