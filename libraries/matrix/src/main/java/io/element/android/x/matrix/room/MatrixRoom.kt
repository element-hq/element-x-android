package io.element.android.x.matrix.room

import io.element.android.x.core.data.CoroutineDispatchers
import io.element.android.x.matrix.core.RoomId
import io.element.android.x.matrix.timeline.MatrixTimeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.*

class MatrixRoom(
    private val slidingSyncUpdateFlow: Flow<UpdateSummary>,
    private val slidingSyncRoom: SlidingSyncRoom,
    private val room: Room,
    private val coroutineDispatchers: CoroutineDispatchers,
) {

    private val paginationOutcome = MutableStateFlow(PaginationOutcome(true))
    fun syncUpdateFlow(): Flow<Unit> {
        return slidingSyncUpdateFlow
            .filter {
                it.rooms.contains(room.id())
            }
            .map { }
            .onStart { emit(Unit) }
    }

    fun timeline(): MatrixTimeline {
        return MatrixTimeline(this)
    }

    internal fun timelineDiff(): Flow<TimelineDiff> {
        return room.timelineDiff()
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

    fun addTimelineListener(timelineListener: TimelineListener) {
        room.addTimelineListener(timelineListener)
    }

    suspend fun paginateBackwards(count: Int): Result<Unit> = withContext(coroutineDispatchers.io) {
        if (!paginationOutcome.value.moreMessages) {
            return@withContext Result.failure(IllegalStateException("no more message"))
        }
        runCatching {
            paginationOutcome.value = room.paginateBackwards(count.toUShort())
        }
    }


}