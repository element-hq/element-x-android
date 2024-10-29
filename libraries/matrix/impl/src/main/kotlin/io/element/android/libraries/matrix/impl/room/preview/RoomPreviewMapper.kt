/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.preview

import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.preview.RoomPreview
import io.element.android.libraries.matrix.impl.room.toRoomType
import org.matrix.rustcomponents.sdk.JoinRule
import org.matrix.rustcomponents.sdk.Membership
import org.matrix.rustcomponents.sdk.RoomPreview as RustRoomPreview

object RoomPreviewMapper {
    fun map(roomPreview: RustRoomPreview): RoomPreview {
        return roomPreview.use {
            val info = roomPreview.info()
            RoomPreview(
                roomId = RoomId(info.roomId),
                canonicalAlias = info.canonicalAlias?.let(::RoomAlias),
                name = info.name,
                topic = info.topic,
                avatarUrl = info.avatarUrl,
                numberOfJoinedMembers = info.numJoinedMembers.toLong(),
                roomType = info.roomType.toRoomType(),
                isHistoryWorldReadable = info.isHistoryWorldReadable,
                isJoined = info.membership == Membership.JOINED,
                isInvited = info.membership == Membership.INVITED,
                isPublic = info.joinRule == JoinRule.Public,
                canKnock = info.joinRule == JoinRule.Knock
            )
        }
    }
}
