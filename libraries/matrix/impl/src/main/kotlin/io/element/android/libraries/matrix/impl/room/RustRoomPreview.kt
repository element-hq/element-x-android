/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.RoomPreview
import org.matrix.rustcomponents.sdk.Membership
import org.matrix.rustcomponents.sdk.RoomPreview as InnerRoomPreview

class RustRoomPreview(
    override val sessionId: SessionId,
    override val roomId: RoomId,
    private val inner: InnerRoomPreview,
    private val roomMembershipObserver: RoomMembershipObserver,
) : RoomPreview {
    companion object {
        val ALLOWED_MEMBERSHIPS = setOf(Membership.INVITED, Membership.KNOCKED, Membership.BANNED)
    }

    override suspend fun leave(): Result<Unit> = runCatching {
        inner.leave()
    }.onSuccess {
        roomMembershipObserver.notifyUserLeftRoom(roomId)
    }

    override suspend fun forget(): Result<Unit> = runCatching {
        inner.forget()
    }

    override fun close() {
        inner.destroy()
    }
}
