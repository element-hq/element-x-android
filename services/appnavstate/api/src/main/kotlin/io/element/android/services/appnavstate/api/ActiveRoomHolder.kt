/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.appnavstate.api

import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Holds the active room for a given session so it can be reused instead of instantiating a new one.
 */
@SingleIn(AppScope::class)
class ActiveRoomHolder @Inject constructor() {
    private val rooms = ConcurrentHashMap<SessionId, JoinedRoom>()

    fun addRoom(room: JoinedRoom) {
        rooms[room.sessionId] = room
    }

    fun getActiveRoom(sessionId: SessionId): JoinedRoom? {
        return rooms[sessionId]
    }

    fun removeRoom(sessionId: SessionId): JoinedRoom? {
        return rooms.remove(sessionId)
    }
}
