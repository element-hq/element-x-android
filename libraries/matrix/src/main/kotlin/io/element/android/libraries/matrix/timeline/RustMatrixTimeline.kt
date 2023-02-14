/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.matrix.timeline

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.core.EventId
import io.element.android.libraries.matrix.room.RustMatrixRoom
import io.element.android.libraries.matrix.util.StoppableSpawnBag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.PaginationOptions
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.SlidingSyncRoom
import org.matrix.rustcomponents.sdk.TimelineChange
import org.matrix.rustcomponents.sdk.TimelineDiff
import org.matrix.rustcomponents.sdk.TimelineListener
import timber.log.Timber
import java.util.Collections

class RustMatrixTimeline(
    private val matrixRoom: RustMatrixRoom,
    private val innerRoom: Room,
    private val slidingSyncRoom: SlidingSyncRoom,
    private val coroutineScope: CoroutineScope,
    private val coroutineDispatchers: CoroutineDispatchers,
) : MatrixTimeline {

    private val innerTimelineListener = object : TimelineListener {
        override fun onUpdate(update: TimelineDiff) {
            coroutineScope.launch {
                updateTimelineItems {
                    applyDiff(update)
                }
            }
        }
    }

    override var callback: MatrixTimeline.Callback? = null

    private val timelineItems: MutableStateFlow<List<MatrixTimelineItem>> =
        MutableStateFlow(emptyList())

    private val listenerTokens = StoppableSpawnBag()

    @OptIn(FlowPreview::class)
    override fun timelineItems(): Flow<List<MatrixTimelineItem>> {
        return timelineItems.sample(50)
    }

    private fun MutableList<MatrixTimelineItem>.applyDiff(diff: TimelineDiff) {
        when (diff.change()) {
            TimelineChange.PUSH -> {
                val item = diff.push()?.asMatrixTimelineItem() ?: return
                callback?.onPushedTimelineItem(item)
                add(item)
            }
            TimelineChange.UPDATE_AT -> {
                val updateAtData = diff.updateAt() ?: return
                val item = updateAtData.item.asMatrixTimelineItem()
                callback?.onUpdatedTimelineItem(item)
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

    override suspend fun paginateBackwards(requestSize: Int, untilNumberOfItems: Int): Result<Unit> = withContext(coroutineDispatchers.io) {
        runCatching {
            Timber.v("Start back paginating for room ${slidingSyncRoom.roomId()} ")
            val paginationOptions = PaginationOptions.UntilNumItems(
                eventLimit = requestSize.toUShort(),
                items = untilNumberOfItems.toUShort()
            )
            innerRoom.paginateBackwards(paginationOptions)
        }.onFailure {
            Timber.e(it, "Fail to paginate for room ${slidingSyncRoom.roomId()}")
        }.onSuccess {
            Timber.v("Success back paginating for room ${slidingSyncRoom.roomId()}")
        }
    }

    private suspend fun updateTimelineItems(block: MutableList<MatrixTimelineItem>.() -> Unit) =
        withContext(coroutineDispatchers.diffUpdateDispatcher) {
            val mutableTimelineItems = timelineItems.value.toMutableList()
            block(mutableTimelineItems)
            timelineItems.value = mutableTimelineItems
        }

    override fun addListener(timelineListener: TimelineListener) {
        listenerTokens += slidingSyncRoom.subscribeAndAddTimelineListener(timelineListener, null)
    }

    override fun initialize() {
        Timber.v("Init timeline for room ${slidingSyncRoom.roomId()}")
        coroutineScope.launch {
            matrixRoom.fetchMembers()
                .onFailure {
                    Timber.e(it, "Fail to fetch members for room ${slidingSyncRoom.roomId()}")
                }.onSuccess {
                    Timber.v("Success fetching members for room ${slidingSyncRoom.roomId()}")
                }
        }
        addListener(innerTimelineListener)
    }

    override fun dispose() {
        Timber.v("Dispose timeline for room ${slidingSyncRoom.roomId()}")
        listenerTokens.dispose()
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
}
