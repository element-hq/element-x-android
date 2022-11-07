package io.element.android.x.matrix.timeline

import io.element.android.x.matrix.core.EventId
import io.element.android.x.matrix.room.MatrixRoom
import kotlinx.coroutines.flow.*
import org.matrix.rustcomponents.sdk.TimelineChange
import org.matrix.rustcomponents.sdk.TimelineDiff
import timber.log.Timber
import java.util.*

class MatrixTimeline(
    private val room: MatrixRoom,
) {

    interface Callback {
        fun onUpdatedTimelineItem(eventId: EventId)
        fun onStartedBackPaginating()
        fun onFinishedBackPaginating()
    }

    var callback: Callback? = null

    private val timelineItems: MutableStateFlow<List<MatrixTimelineItem>> =
        MutableStateFlow(emptyList())


    fun timelineItems(): Flow<List<MatrixTimelineItem>> {
        return diffFlow().combine(timelineItems) { _, _ ->
            timelineItems.value.reversed()
        }
    }

    private fun diffFlow(): Flow<Unit> {
        return room.timelineDiff()
            .onEach { timelineDiff ->
                updateTimelineItems {
                    applyDiff(timelineDiff)
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

    private fun updateTimelineItems(block: MutableList<MatrixTimelineItem>.() -> Unit) {
        val mutableTimelineItems = timelineItems.value.toMutableList()
        block(mutableTimelineItems)
        timelineItems.value = mutableTimelineItems
    }


    suspend fun processItemAppearance(itemId: String) {

    }

    suspend fun processItemDisappearance(itemId: String) {

    }

    suspend fun paginateBackwards(count: Int): Result<Unit> {
        return room.paginateBackwards(count)
    }

    suspend fun sendMessage(message: String): Result<Unit> {
        return Result.success(Unit)
    }


}