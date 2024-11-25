/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.room.PendingRoom
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import org.matrix.rustcomponents.sdk.RoomPreview

class RustPendingRoom(
    override val sessionId: SessionId,
    override val roomId: RoomId,
    private val inner: RoomPreview,
    private val roomMembershipObserver: RoomMembershipObserver,
) : PendingRoom {
    override suspend fun leave(): Result<Unit> = runCatching {
        inner.leave()
    }.onSuccess {
        roomMembershipObserver.notifyUserLeftRoom(roomId)
    }

    override fun close() {
        inner.destroy()
    }
}
