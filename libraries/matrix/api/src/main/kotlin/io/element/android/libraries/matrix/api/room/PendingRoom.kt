/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

/** A reference to a room the current user has knocked to or has been invited to, with the ability to leave the room. */
interface PendingRoom : AutoCloseable {
    val sessionId: SessionId
    val roomId: RoomId

    /** Leave the room ie.decline invite or cancel knock. */
    suspend fun leave(): Result<Unit>
}
