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
            TimelineChange.PUSH_FRONT -> {
                val item = diff.pushFront()?.asMatrixTimelineItem() ?: return
                callback?.onPushedTimelineItem(item)
                add(0, item)
            }
            TimelineChange.PUSH_BACK -> {
                val item = diff.pushBack()?.asMatrixTimelineItem() ?: return
                callback?.onPushedTimelineItem(item)
                add(item)
            }
            TimelineChange.INSERT -> {
                val insertAtData = diff.insert() ?: return
                val item = insertAtData.item.asMatrixTimelineItem()
                val insertAt = insertAtData.index.toInt()
                add(insertAt, item)
            }
            TimelineChange.APPEND -> {
                val appendData = diff.append()?.map { it.asMatrixTimelineItem()} ?: return
                addAll(appendData)
            }
            TimelineChange.SET -> {
                val updateAtData = diff.set() ?: return
                val item = updateAtData.item.asMatrixTimelineItem()
                callback?.onUpdatedTimelineItem(item)
                val updateAt = updateAtData.index.toInt()
                set(updateAt, item)
            }
            TimelineChange.POP_FRONT -> {
                removeFirst()
            }
            TimelineChange.POP_BACK -> {
                removeLast()
            }
            TimelineChange.REMOVE -> {
                val removeAtData = diff.remove() ?: return
                removeAt(removeAtData.toInt())
            }
            TimelineChange.CLEAR -> {
                clear()
            }
            TimelineChange.RESET -> {
                clear()
                val items = diff.reset()?.map { it.asMatrixTimelineItem() } ?: return
                addAll(items)
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
        val subscription = slidingSyncRoom.subscribeAndAddTimelineListener(timelineListener, null)
        timelineItems.value = subscription.items.map { it.asMatrixTimelineItem() }
        listenerTokens += subscription.taskHandle
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
