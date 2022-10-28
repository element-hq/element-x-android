package io.element.android.x.matrix.room

import io.element.android.x.matrix.core.RoomId
import org.matrix.rustcomponents.sdk.Room

class MatrixRoom(private val room: Room) {

    val roomId = RoomId(room.id())


}