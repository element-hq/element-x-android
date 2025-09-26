/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.spaces

import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.impl.room.join.map
import io.element.android.libraries.matrix.impl.room.map
import org.matrix.rustcomponents.sdk.SpaceRoom as RustSpaceRoom

class SpaceRoomMapper {
    fun map(spaceRoom: RustSpaceRoom): SpaceRoom {
        return SpaceRoom(
            avatarUrl = spaceRoom.avatarUrl,
            canonicalAlias = spaceRoom.canonicalAlias?.let(::RoomAlias),
            childrenCount = spaceRoom.childrenCount.toInt(),
            guestCanJoin = spaceRoom.guestCanJoin,
            heroes = spaceRoom.heroes.orEmpty().map { it.map() },
            joinRule = spaceRoom.joinRule?.map(),
            name = spaceRoom.name,
            numJoinedMembers = spaceRoom.numJoinedMembers.toInt(),
            roomId = RoomId(spaceRoom.roomId),
            roomType = spaceRoom.roomType.map(),
            state = spaceRoom.state?.map(),
            topic = spaceRoom.topic,
            worldReadable = spaceRoom.worldReadable.orFalse(),
            via = spaceRoom.via,
        )
    }
}
