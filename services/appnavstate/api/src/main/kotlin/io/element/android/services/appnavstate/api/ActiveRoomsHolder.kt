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
 *
 * This works as a FILO (First In Last Out) stack, meaning that the last room added for a session will be the first one to be removed.
 */
@SingleIn(AppScope::class)
class ActiveRoomsHolder @Inject constructor() {
    private val rooms = ConcurrentHashMap<SessionId, MutableList<JoinedRoom>>()

    /**
     * Adds a new held room for the given sessionId.
     */
    fun addRoom(room: JoinedRoom) {
        val roomsForSessionId = rooms.getOrPut(key = room.sessionId, defaultValue = { mutableListOf() })
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
     * Removes the last room added for the given [sessionId] and returns it or null if there weren't any.
     */
    fun removeRoom(sessionId: SessionId): JoinedRoom? {
        val roomsForSessionId = rooms[sessionId] ?: return null
        return roomsForSessionId.removeLastOrNull()
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
