/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.appnavstate.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.services.appnavstate.api.ActiveRoomsHolder
import java.util.concurrent.ConcurrentHashMap

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultActiveRoomsHolder : ActiveRoomsHolder {
    private val rooms = ConcurrentHashMap<SessionId, MutableSet<JoinedRoom>>()

    override fun addRoom(room: JoinedRoom) {
        val roomsForSessionId = rooms.getOrPut(key = room.sessionId, defaultValue = { mutableSetOf() })
        if (roomsForSessionId.none { it.roomId == room.roomId }) {
            // We don't want to add the same room multiple times
            roomsForSessionId.add(room)
        }
    }

    override fun getActiveRoom(sessionId: SessionId): JoinedRoom? {
        return rooms[sessionId]?.lastOrNull()
    }

    override fun getActiveRoomMatching(sessionId: SessionId, roomId: RoomId): JoinedRoom? {
        return rooms[sessionId]?.find { it.roomId == roomId }
    }

    override fun removeRoom(sessionId: SessionId, roomId: RoomId) {
        val roomsForSessionId = rooms[sessionId] ?: return
        roomsForSessionId.removeIf { it.roomId == roomId }
    }

    override fun clear(sessionId: SessionId) {
        val activeRooms = rooms.remove(sessionId) ?: return
        for (room in activeRooms) {
            // Destroy the room to reset the live timelines
            room.destroy()
        }
    }
}
