/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

/** A reference to a room the current user has been invited to, with the ability to decline the invite. */
interface InvitedRoom : AutoCloseable {
    val sessionId: SessionId
    val roomId: RoomId

    /** Decline the invite to this room. */
    suspend fun declineInvite(): Result<Unit>
}
