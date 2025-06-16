/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomlist

import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.impl.room.RoomInfoMapper
import io.element.android.libraries.matrix.impl.room.message.RoomMessageFactory
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.use

class RoomSummaryFactory(
    private val roomMessageFactory: RoomMessageFactory = RoomMessageFactory(),
    private val roomInfoMapper: RoomInfoMapper = RoomInfoMapper(),
) {
    suspend fun create(room: Room): RoomSummary {
        val roomInfo = room.roomInfo().let(roomInfoMapper::map)
        val latestRoomMessage = room.latestEvent().use { event ->
            roomMessageFactory.create(event)
        }
        return RoomSummary(
            info = roomInfo,
            lastMessage = latestRoomMessage,
        )
    }
}
