package io.element.android.x.matrix.room

import io.element.android.x.matrix.core.RoomId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.SlidingSyncRoom
import org.matrix.rustcomponents.sdk.UpdateSummary

class MatrixRoom(
    private val slidingSyncUpdateFlow: Flow<UpdateSummary>,
    private val slidingSyncRoom: SlidingSyncRoom,
    private val room: Room,
) {

    fun syncUpdateFlow(): Flow<Unit> {
        return slidingSyncUpdateFlow
            .filter {
                it.rooms.contains(room.id())
            }
            .map { }
            .onStart { emit(Unit) }
    }

    val roomId = RoomId(room.id())

    val name: String?
        get() {
            return slidingSyncRoom.name()
        }

    val displayName: String
        get() {
            return room.displayName()
        }

    val topic: String?
        get() {
            return room.topic()
        }

    val avatarUrl: String?
        get() {
            return room.avatarUrl()
        }


}