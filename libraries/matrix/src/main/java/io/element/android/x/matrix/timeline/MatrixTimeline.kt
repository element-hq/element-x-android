package io.element.android.x.matrix.timeline

import io.element.android.x.core.coroutine.CoroutineDispatchers
import io.element.android.x.core.data.flow.chunk
import io.element.android.x.matrix.core.EventId
import io.element.android.x.matrix.room.MatrixRoom
import io.element.android.x.matrix.room.timelineDiff
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.*
import timber.log.Timber
import java.util.*

class MatrixTimeline(
    private val matrixRoom: MatrixRoom,
    private val room: Room,
    private val coroutineDispatchers: CoroutineDispatchers,
) {
    interface Callback {
        fun onUpdatedTimelineItem(eventId: EventId)
        fun onStartedBackPaginating()
        fun onFinishedBackPaginating()
    }

    var callback: Callback? = null

    private val paginationOutcome = MutableStateFlow(PaginationOutcome(true))
    private val timelineItems: MutableStateFlow<List<MatrixTimelineItem>> =
        MutableStateFlow(emptyList())


    fun timelineItems(): Flow<List<MatrixTimelineItem>> {
        return diffFlow().combine(timelineItems) { _, _ ->
            timelineItems.value
        }
    }

    val hasMoreToLoad: Boolean
        get() {
            return paginationOutcome.value.moreMessages
        }


    private fun diffFlow(): Flow<Unit> {
        return room.timelineDiff()
            .chunk(30)
            .onEach { timelineDiffs ->
                updateTimelineItems {
                    timelineDiffs.onEach {
                        applyDiff(it)
                    }
                }
            }.map { }
    }

    private fun MutableList<MatrixTimelineItem>.applyDiff(diff: TimelineDiff) {
        Timber.v("ApplyDiff: ${diff.change()} for list with size: $size")
        when (diff.change()) {
            TimelineChange.PUSH -> {
                val item = diff.push()?.asMatrixTimelineItem() ?: return
                add(item)
            }
            TimelineChange.UPDATE_AT -> {
                val updateAtData = diff.updateAt() ?: return
                val item = updateAtData.item.asMatrixTimelineItem()
                set(updateAtData.index.toInt(), item)
            }
            TimelineChange.INSERT_AT -> {
                val insertAtData = diff.insertAt() ?: return
                val item = insertAtData.item.asMatrixTimelineItem()
                add(insertAtData.index.toInt(), item)
            }
            TimelineChange.MOVE -> {
                val moveData = diff.move() ?: return
                Collections.swap(this, moveData.oldIndex.toInt(), moveData.newIndex.toInt())
            }
            TimelineChange.REMOVE_AT -> {
                val removeAtData = diff.removeAt() ?: return
                removeAt(removeAtData.toInt())
            }
            TimelineChange.REPLACE -> {
                clear()
                val items = diff.replace()?.map { it.asMatrixTimelineItem() } ?: return
                addAll(items)
            }
            TimelineChange.POP -> {
                removeLast()
            }
            TimelineChange.CLEAR -> {
                clear()
            }
        }
    }

    suspend fun paginateBackwards(count: Int): Result<Unit> = withContext(coroutineDispatchers.io) {
        if (!paginationOutcome.value.moreMessages) {
            return@withContext Result.failure(IllegalStateException("no more message"))
        }
        runCatching {
            paginationOutcome.value = room.paginateBackwards(count.toUShort())
        }
    }

    private fun updateTimelineItems(block: MutableList<MatrixTimelineItem>.() -> Unit) {
        val mutableTimelineItems = timelineItems.value.toMutableList()
        block(mutableTimelineItems)
        timelineItems.value = mutableTimelineItems
    }

    fun addListener(timelineListener: TimelineListener) {
        room.addTimelineListener(timelineListener)
    }

    fun dispose() {
        room.removeTimeline()
    }

    /**
     * @param message markdown message
     */
    suspend fun sendMessage(message: String): Result<Unit> {
        return matrixRoom.sendMessage(message)
    }

}