/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.appnavstate.test

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.services.appnavstate.api.ActiveRoomsHolder

class FakeActiveRoomsHolder : ActiveRoomsHolder {
    private var room: JoinedRoom? = null

    override fun addRoom(room: JoinedRoom) {
        this.room = room
    }

    override fun getActiveRoom(sessionId: SessionId): JoinedRoom? {
        return room
    }

    override fun getActiveRoomMatching(sessionId: SessionId, roomId: RoomId): JoinedRoom? {
        return null
    }

    override fun removeRoom(sessionId: SessionId, roomId: RoomId) {
        room = null
    }

    override fun clear(sessionId: SessionId) {
    }
}
