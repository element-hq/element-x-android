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
import org.matrix.rustcomponents.sdk.RoomPreview as RustRoomPreview

object RoomPreviewMapper {
    fun map(roomPreview: RustRoomPreview): RoomPreview {
        return RoomPreview(
            roomId = RoomId(roomPreview.roomId),
            canonicalAlias = roomPreview.canonicalAlias?.let(::RoomAlias),
            name = roomPreview.name,
            topic = roomPreview.topic,
            avatarUrl = roomPreview.avatarUrl,
            numberOfJoinedMembers = roomPreview.numJoinedMembers.toLong(),
            roomType = roomPreview.roomType.toRoomType(),
            isHistoryWorldReadable = roomPreview.isHistoryWorldReadable,
            isJoined = roomPreview.isJoined,
            isInvited = roomPreview.isInvited,
            isPublic = roomPreview.isPublic,
            canKnock = roomPreview.canKnock
        )
    }
}
