/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.appnavstate.api

import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Holds the active rooms for a given session so they can be reused instead of instantiating new ones.
 */
@SingleIn(AppScope::class)
class ActiveRoomsHolder @Inject constructor() {
    private val rooms = ConcurrentHashMap<SessionId, MutableSet<JoinedRoom>>()

    /**
     * Adds a new held room for the given sessionId.
     */
    fun addRoom(room: JoinedRoom) {
        val roomsForSessionId = rooms.getOrPut(key = room.sessionId, defaultValue = { mutableSetOf() })
        if (roomsForSessionId.none { it.roomId == room.roomId }) {
            // We don't want to add the same room multiple times
            roomsForSessionId.add(room)
        }
    }

    /**
     * Returns the last room added for the given [sessionId] or null if no room was added.
     */
    fun getActiveRoom(sessionId: SessionId): JoinedRoom? {
        return rooms[sessionId]?.lastOrNull()
    }

    /**
     * Returns an active room associated to the given [sessionId], with the given [roomId], or null if none match.
     */
    fun getActiveRoomMatching(sessionId: SessionId, roomId: RoomId): JoinedRoom? {
        return rooms[sessionId]?.find { it.roomId == roomId }
    }

    /**
     * Removes any room matching the provided [sessionId] and [roomId].
     */
    fun removeRoom(sessionId: SessionId, roomId: RoomId) {
        val roomsForSessionId = rooms[sessionId] ?: return
        roomsForSessionId.removeIf { it.roomId == roomId }
    }

    /**
     * Clears all the rooms for the given sessionId.
     */
    fun clear(sessionId: SessionId) {
        val activeRooms = rooms.remove(sessionId) ?: return
        for (room in activeRooms) {
            // Destroy the room to reset the live timelines
            room.destroy()
        }
    }
}
