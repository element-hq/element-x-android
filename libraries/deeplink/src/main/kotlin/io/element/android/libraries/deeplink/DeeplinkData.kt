/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.deeplink

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId

sealed interface DeeplinkData {
    /** Session id is common for all deep links. */
    val sessionId: SessionId

    /** The target is the root of the app, with the given [sessionId]. */
    data class Root(override val sessionId: SessionId) : DeeplinkData

    /** The target is a room, with the given [sessionId], [roomId] and optionally a [threadId]. */
    data class Room(override val sessionId: SessionId, val roomId: RoomId, val threadId: ThreadId?) : DeeplinkData
}
