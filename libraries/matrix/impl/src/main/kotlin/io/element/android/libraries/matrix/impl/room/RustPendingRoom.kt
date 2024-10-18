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
import org.matrix.rustcomponents.sdk.Room

class RustPendingRoom(
    override val sessionId: SessionId,
    private val inner: Room,
) : PendingRoom {
    override val roomId = RoomId(inner.id())

    override suspend fun leave(): Result<Unit> = runCatching {
        inner.leave()
    }

    override fun close() {
        inner.destroy()
    }
}
