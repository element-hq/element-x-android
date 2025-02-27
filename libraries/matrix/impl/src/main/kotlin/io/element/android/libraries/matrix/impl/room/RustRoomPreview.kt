/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.room.RoomMembershipDetails
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.RoomPreview
import io.element.android.libraries.matrix.api.room.preview.RoomPreviewInfo
import io.element.android.libraries.matrix.impl.room.member.RoomMemberMapper
import io.element.android.libraries.matrix.impl.room.preview.RoomPreviewInfoMapper
import org.matrix.rustcomponents.sdk.Membership
import org.matrix.rustcomponents.sdk.RoomPreview as InnerRoomPreview

@Immutable
class RustRoomPreview(
    override val sessionId: SessionId,
    private val inner: InnerRoomPreview,
    private val roomMembershipObserver: RoomMembershipObserver?,
) : RoomPreview {
    companion object {
        val ALLOWED_MEMBERSHIPS = setOf(Membership.INVITED, Membership.KNOCKED, Membership.BANNED)
    }

    override val info: RoomPreviewInfo = RoomPreviewInfoMapper.map(inner.info())

    override suspend fun leave(): Result<Unit> = runCatching {
        inner.leave()
    }.onSuccess {
        roomMembershipObserver?.notifyUserLeftRoom(info.roomId)
    }

    override suspend fun forget(): Result<Unit> = runCatching {
        inner.forget()
    }

    override suspend fun membershipDetails(): Result<RoomMembershipDetails?> = runCatching {
        val details = inner.ownMembershipDetails() ?: return@runCatching null
        RoomMembershipDetails(
            currentUserMember = RoomMemberMapper.map(details.ownRoomMember),
            senderMember = details.senderRoomMember?.let { RoomMemberMapper.map(it) },
        )
    }

    override fun close() {
        inner.destroy()
    }
}
