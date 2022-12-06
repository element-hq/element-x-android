package io.element.android.x.matrix.timeline

import io.element.android.x.core.coroutine.CoroutineDispatchers
import io.element.android.x.matrix.room.MatrixRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.*
import timber.log.Timber
import java.util.*

class MatrixTimeline(
    private val matrixRoom: MatrixRoom,
    private val room: Room,
    private val slidingSyncRoom: SlidingSyncRoom,
    private val coroutineScope: CoroutineScope,
    private val coroutineDispatchers: CoroutineDispatchers,
) : TimelineListener {

    interface Callback {
        fun onUpdatedTimelineItem(timelineItem: MatrixTimelineItem) = Unit
        fun onPushedTimelineItem(timelineItem: MatrixTimelineItem) = Unit
    }

    var callback: Callback? = null

    private val paginationOutcome = MutableStateFlow(PaginationOutcome(true))
    private val timelineItems: MutableStateFlow<List<MatrixTimelineItem>> =
        MutableStateFlow(emptyList())


    fun timelineItems(): Flow<List<MatrixTimelineItem>> {
        return timelineItems.sample(50)
    }

    val hasMoreToLoad: Boolean
        get() {
            return paginationOutcome.value.moreMessages
        }


    private fun MutableList<MatrixTimelineItem>.applyDiff(diff: TimelineDiff) {
        when (diff.change()) {
            TimelineChange.PUSH -> {
                Timber.v("Apply push on list with size: $size")
                val item = diff.push()?.asMatrixTimelineItem() ?: return
                callback?.onPushedTimelineItem(item)
                add(item)
            }
            TimelineChange.UPDATE_AT -> {
                val updateAtData = diff.updateAt() ?: return
                Timber.v("Apply $updateAtData on list with size: $size")
                val item = updateAtData.item.asMatrixTimelineItem()
                callback?.onUpdatedTimelineItem(item)
                set(updateAtData.index.toInt(), item)
            }
            TimelineChange.INSERT_AT -> {
                val insertAtData = diff.insertAt() ?: return
                Timber.v("Apply $insertAtData on list with size: $size")
                val item = insertAtData.item.asMatrixTimelineItem()
                add(insertAtData.index.toInt(), item)
            }
            TimelineChange.MOVE -> {
                val moveData = diff.move() ?: return
                Timber.v("Apply $moveData on list with size: $size")
                Collections.swap(this, moveData.oldIndex.toInt(), moveData.newIndex.toInt())
            }
            TimelineChange.REMOVE_AT -> {
                val removeAtData = diff.removeAt() ?: return
                Timber.v("Apply $removeAtData on list with size: $size")
                removeAt(removeAtData.toInt())
            }
            TimelineChange.REPLACE -> {
                Timber.v("Apply REPLACE on list with size: $size")
                clear()
                val items = diff.replace()?.map { it.asMatrixTimelineItem() } ?: return
                addAll(items)
            }
            TimelineChange.POP -> {
                Timber.v("Apply POP on list with size: $size")
                removeLast()
            }
            TimelineChange.CLEAR -> {
                Timber.v("Apply CLEAR on list with size: $size")
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

    private suspend fun updateTimelineItems(block: MutableList<MatrixTimelineItem>.() -> Unit) =
        withContext(coroutineDispatchers.diffUpdateDispatcher) {
            val mutableTimelineItems = timelineItems.value.toMutableList()
            block(mutableTimelineItems)
            timelineItems.value = mutableTimelineItems
        }

    fun addListener(timelineListener: TimelineListener) {
        slidingSyncRoom.addTimelineListener(timelineListener)
    }

    fun initialize() {
        addListener(this)
    }

    fun dispose() {
        slidingSyncRoom.removeTimeline()
    }

    /**
     * @param message markdown message
     */
    suspend fun sendMessage(message: String): Result<Unit> {
        return matrixRoom.sendMessage(message)
    }

    suspend fun editMessage(originalEventId: String, message: String): Result<Unit> {
        return matrixRoom.editMessage(originalEventId, message = message)
    }

    suspend fun replyMessage(inReplyToEventId: String, message: String): Result<Unit> {
        return matrixRoom.replyMessage(inReplyToEventId, message)
    }

    override fun onUpdate(update: TimelineDiff) {
        coroutineScope.launch {
            updateTimelineItems {
                applyDiff(update)
            }
        }
    }

}