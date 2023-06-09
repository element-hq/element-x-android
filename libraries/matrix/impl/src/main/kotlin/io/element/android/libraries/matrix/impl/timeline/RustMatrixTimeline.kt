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

package io.element.android.libraries.matrix.impl.timeline

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.EventType
import io.element.android.libraries.matrix.impl.timeline.item.event.EventMessageMapper
import io.element.android.libraries.matrix.impl.timeline.item.event.EventTimelineItemMapper
import io.element.android.libraries.matrix.impl.timeline.item.event.TimelineEventContentMapper
import io.element.android.libraries.matrix.impl.timeline.item.virtual.VirtualTimelineItemMapper
import io.element.android.libraries.matrix.impl.util.TaskHandleBag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.PaginationOptions
import org.matrix.rustcomponents.sdk.RequiredState
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.RoomSubscription
import org.matrix.rustcomponents.sdk.SlidingSyncRoom
import org.matrix.rustcomponents.sdk.TimelineItem
import org.matrix.rustcomponents.sdk.TimelineListener
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class RustMatrixTimeline(
    private val matrixRoom: MatrixRoom,
    private val innerRoom: Room,
    private val slidingSyncRoom: SlidingSyncRoom,
    private val coroutineScope: CoroutineScope,
    private val coroutineDispatchers: CoroutineDispatchers,
) : MatrixTimeline {

    private val isInit = AtomicBoolean(false)

    private val timelineItems: MutableStateFlow<List<MatrixTimelineItem>> =
        MutableStateFlow(emptyList())

    private val paginationState = MutableStateFlow(
        MatrixTimeline.PaginationState(canBackPaginate = true, isBackPaginating = false)
    )

    private val timelineItemFactory = MatrixTimelineItemMapper(
        room = innerRoom,
        coroutineScope = coroutineScope,
        virtualTimelineItemMapper = VirtualTimelineItemMapper(),
        eventTimelineItemMapper = EventTimelineItemMapper(
            contentMapper = TimelineEventContentMapper(
                eventMessageMapper = EventMessageMapper()
            )
        )
    )

    private val innerTimelineListener = MatrixTimelineDiffProcessor(
        paginationState = paginationState,
        timelineItems = timelineItems,
        coroutineScope = coroutineScope,
        diffDispatcher = coroutineDispatchers.diffUpdateDispatcher,
        timelineItemFactory = timelineItemFactory,
    )

    private val listenerTokens = TaskHandleBag()
    override fun paginationState(): StateFlow<MatrixTimeline.PaginationState> {
        return paginationState
    }

    @OptIn(FlowPreview::class)
    override fun timelineItems(): Flow<List<MatrixTimelineItem>> {
        return timelineItems.sample(50)
    }

    override fun initialize() {
        Timber.v("Init timeline for room ${matrixRoom.roomId}")
        coroutineScope.launch {
            val result = addListener(innerTimelineListener)
            result
                .onSuccess { timelineItems ->
                    val matrixTimelineItems = timelineItems.map(timelineItemFactory::map)
                    withContext(coroutineDispatchers.diffUpdateDispatcher) {
                        this@RustMatrixTimeline.timelineItems.value = matrixTimelineItems
                    }
                    isInit.set(true)
                }
                .onFailure {
                    Timber.e("Failed adding timeline listener on room with identifier: ${matrixRoom.roomId})")
                }
        }
    }

    override fun dispose() {
        Timber.v("Dispose timeline for room ${matrixRoom.roomId}")
        listenerTokens.dispose()
        isInit.set(false)
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

    override suspend fun paginateBackwards(requestSize: Int, untilNumberOfItems: Int): Result<Unit> = withContext(coroutineDispatchers.io) {
        runCatching {
            Timber.v("Start back paginating for room ${matrixRoom.roomId} ")
            if (!isInit.get()) {
                throw IllegalStateException("Timeline is not init yet")
            }
            val paginationOptions = PaginationOptions.UntilNumItems(
                eventLimit = requestSize.toUShort(),
                items = untilNumberOfItems.toUShort()
            )
            innerRoom.paginateBackwards(paginationOptions)
        }.onFailure {
            Timber.e(it, "Fail to paginate for room ${matrixRoom.roomId}")
        }.onSuccess {
            Timber.v("Success back paginating for room ${matrixRoom.roomId}")
        }
    }

    private suspend fun addListener(timelineListener: TimelineListener): Result<List<TimelineItem>> = withContext(coroutineDispatchers.io) {
        runCatching {
            val settings = RoomSubscription(
                requiredState = listOf(
                    RequiredState(key = EventType.STATE_ROOM_CANONICAL_ALIAS, value = ""),
                    RequiredState(key = EventType.STATE_ROOM_TOPIC, value = ""),
                    RequiredState(key = EventType.STATE_ROOM_JOIN_RULES, value = ""),
                    RequiredState(key = EventType.STATE_ROOM_POWER_LEVELS, value = ""),
                ),
                timelineLimit = null
            )
            slidingSyncRoom.subscribeToRoom(settings)
            val result = slidingSyncRoom.addTimelineListener(timelineListener)
            launch {
                fetchMembers()
            }
            listenerTokens += result.taskHandle
            result.items
        }
    }

    private suspend fun fetchMembers() = withContext(coroutineDispatchers.io) {
        runCatching {
            innerRoom.fetchMembers()
        }
    }
}
