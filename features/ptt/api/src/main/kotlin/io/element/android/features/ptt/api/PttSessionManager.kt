/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.api

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.StateFlow

/**
 * App-wide handle to the single active PTT session, usable by any feature (the room screen, the PTT
 * prototype screen) without depending on the transport internals. Backed by the foreground session
 * host + the selected [PttTransport].
 */
interface PttSessionManager {
    /** The active session's state, or `null` when no channel is joined. */
    val sessionState: StateFlow<PttSessionState?>

    /** Whether the user has opted in to hearing others (audio output). Off by default — silent. */
    val isHearingEnabled: StateFlow<Boolean>

    /** Covert/silent master override: forces audio output off and suppresses all tones when on. */
    val isCovert: StateFlow<Boolean>

    /** Start a PTT session for the room via the foreground host (no-op if one is already live). */
    fun start(sessionId: SessionId, roomId: RoomId)

    /** Leave the active session and tear the host down. */
    fun stop()

    /** Take the floor and begin transmitting (talk button / hardware-key down). */
    fun pressToTalk()

    /** Release the floor and stop transmitting (talk button / hardware-key up). */
    fun releaseToTalk()

    /** Opt in/out of hearing others; takes effect immediately on the live session. */
    fun setHearingEnabled(enabled: Boolean)

    /** Toggle covert/silent mode — hard-silences output and tones, overriding [isHearingEnabled]. */
    fun setCovert(enabled: Boolean)
}
