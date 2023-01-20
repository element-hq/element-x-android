/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.x.matrix.timeline

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.x.matrix.core.EventId
import io.element.android.x.matrix.room.RustMatrixRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.PaginationOutcome
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.SlidingSyncRoom
import org.matrix.rustcomponents.sdk.TimelineChange
import org.matrix.rustcomponents.sdk.TimelineDiff
import org.matrix.rustcomponents.sdk.TimelineListener
import timber.log.Timber
import java.util.Collections

class RustMatrixTimeline(
    private val matrixRoom: RustMatrixRoom,
    private val room: Room,
    private val slidingSyncRoom: SlidingSyncRoom,
    private val coroutineScope: CoroutineScope,
    private val coroutineDispatchers: CoroutineDispatchers,
) : TimelineListener, MatrixTimeline {

    override var callback: MatrixTimeline.Callback? = null

    private val paginationOutcome = MutableStateFlow(PaginationOutcome(true))
    private val timelineItems: MutableStateFlow<List<MatrixTimelineItem>> =
        MutableStateFlow(emptyList())

    @OptIn(FlowPreview::class)
    override fun timelineItems(): Flow<List<MatrixTimelineItem>> {
        return timelineItems.sample(50)
    }

    override val hasMoreToLoad: Boolean
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

    override suspend fun paginateBackwards(count: Int): Result<Unit> = withContext(coroutineDispatchers.io) {
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

    override fun addListener(timelineListener: TimelineListener) {
        slidingSyncRoom.addTimelineListener(timelineListener)
    }

    override fun initialize() {
        addListener(this)
    }

    override fun dispose() {
        slidingSyncRoom.removeTimeline()
    }

    /**
     * @param message markdown message
     */
    override suspend fun sendMessage(message: String): Result<Unit> {
        return matrixRoom.sendMessage(message)
    }

    override suspend fun editMessage(originalEventId: EventId, message: String): Result<Unit> {
        return matrixRoom.editMessage(originalEventId, message = message)
    }

    override suspend fun replyMessage(inReplyToEventId: EventId, message: String): Result<Unit> {
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
