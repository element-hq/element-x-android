/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.room.InvitedRoom
import org.matrix.rustcomponents.sdk.Room

class RustInvitedRoom(
    override val sessionId: SessionId,
    private val invitedRoom: Room,
) : InvitedRoom {
    override val roomId = RoomId(invitedRoom.id())

    override suspend fun declineInvite(): Result<Unit> = runCatching {
        invitedRoom.leave()
    }

    override fun close() {
        invitedRoom.destroy()
    }
}
