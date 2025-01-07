/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomdirectory

import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomdirectory.RoomDescription
import org.matrix.rustcomponents.sdk.PublicRoomJoinRule
import org.matrix.rustcomponents.sdk.RoomDescription as RustRoomDescription

class RoomDescriptionMapper {
    fun map(roomDescription: RustRoomDescription): RoomDescription {
        return RoomDescription(
            roomId = RoomId(roomDescription.roomId),
            name = roomDescription.name,
            topic = roomDescription.topic,
            avatarUrl = roomDescription.avatarUrl,
            alias = roomDescription.alias?.let(::RoomAlias),
            joinRule = roomDescription.joinRule.map(),
            isWorldReadable = roomDescription.isWorldReadable,
            numberOfMembers = roomDescription.joinedMembers.toLong(),
        )
    }
}

internal fun PublicRoomJoinRule?.map(): RoomDescription.JoinRule {
    return when (this) {
        PublicRoomJoinRule.PUBLIC -> RoomDescription.JoinRule.PUBLIC
        PublicRoomJoinRule.KNOCK -> RoomDescription.JoinRule.KNOCK
        null -> RoomDescription.JoinRule.UNKNOWN
    }
}
