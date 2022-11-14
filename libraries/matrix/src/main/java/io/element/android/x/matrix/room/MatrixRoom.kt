package io.element.android.x.matrix.room

import io.element.android.x.core.coroutine.CoroutineDispatchers
import io.element.android.x.matrix.core.RoomId
import io.element.android.x.matrix.core.UserId
import io.element.android.x.matrix.timeline.MatrixTimeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.*

class MatrixRoom(
    private val slidingSyncUpdateFlow: Flow<UpdateSummary>,
    private val slidingSyncRoom: SlidingSyncRoom,
    private val room: Room,
    private val coroutineScope: CoroutineScope,
    private val coroutineDispatchers: CoroutineDispatchers,
) {

    fun syncUpdateFlow(): Flow<Unit> {
        return slidingSyncUpdateFlow
            .filter {
                it.rooms.contains(room.id())
            }
            .map { }
            .onStart { emit(Unit) }
    }

    fun timeline(): MatrixTimeline {
        return MatrixTimeline(this, room, coroutineScope, coroutineDispatchers)
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

    suspend fun userDisplayName(userId: String): Result<String?> =
        withContext(coroutineDispatchers.io) {
            runCatching {
                room.memberDisplayName(userId)
            }
        }

    suspend fun userAvatarUrl(userId: String): Result<String?> =
        withContext(coroutineDispatchers.io) {
            runCatching {
                room.memberAvatarUrl(userId)
            }
        }

    suspend fun sendMessage(message: String): Result<Unit> = withContext(coroutineDispatchers.io) {
        val transactionId = genTransactionId()
        val content = messageEventContentFromMarkdown(message)
        runCatching {
            room.send(content, transactionId)
        }
    }
}