package io.element.android.x.matrix

import android.util.Log
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.Room

class RoomWrapper(
    private val client: Client
) {
    fun getRoom(roomId: String): Room? {
        val rooms = client.rooms()
        Log.d(LOG_TAG, "We have ${rooms.size} rooms")
        return rooms.firstOrNull { it.id() == roomId }
    }
}