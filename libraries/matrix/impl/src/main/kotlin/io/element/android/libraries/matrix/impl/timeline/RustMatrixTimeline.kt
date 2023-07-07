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
import io.element.android.libraries.matrix.impl.timeline.item.event.EventMessageMapper
import io.element.android.libraries.matrix.impl.timeline.item.event.EventTimelineItemMapper
import io.element.android.libraries.matrix.impl.timeline.item.event.TimelineEventContentMapper
import io.element.android.libraries.matrix.impl.timeline.item.virtual.VirtualTimelineItemMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.BackPaginationStatus
import org.matrix.rustcomponents.sdk.PaginationOptions
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.TimelineDiff
import org.matrix.rustcomponents.sdk.TimelineItem
import timber.log.Timber

class RustMatrixTimeline(
    roomCoroutineScope: CoroutineScope,
    private val matrixRoom: MatrixRoom,
    private val innerRoom: Room,
    private val coroutineDispatchers: CoroutineDispatchers,
) : MatrixTimeline {

    private val _timelineItems: MutableStateFlow<List<MatrixTimelineItem>> =
        MutableStateFlow(emptyList())

    private val _paginationState = MutableStateFlow(
        MatrixTimeline.PaginationState(canBackPaginate = true, isBackPaginating = false)
    )

    private val timelineItemFactory = MatrixTimelineItemMapper(
        fetchDetailsForEvent = this::fetchDetailsForEvent,
        roomCoroutineScope = roomCoroutineScope,
        virtualTimelineItemMapper = VirtualTimelineItemMapper(),
        eventTimelineItemMapper = EventTimelineItemMapper(
            contentMapper = TimelineEventContentMapper(
                eventMessageMapper = EventMessageMapper()
            )
        )
    )

    private val timelineDiffProcessor = MatrixTimelineDiffProcessor(
        timelineItems = _timelineItems,
        timelineItemFactory = timelineItemFactory,
    )

    override val paginationState: StateFlow<MatrixTimeline.PaginationState> = _paginationState.asStateFlow()

    @OptIn(FlowPreview::class)
    override val timelineItems: Flow<List<MatrixTimelineItem>> = _timelineItems.sample(50)

    internal suspend fun postItems(items: List<TimelineItem>) {
        timelineDiffProcessor.postItems(items)
    }

    internal suspend fun postDiff(timelineDiff: TimelineDiff) {
        timelineDiffProcessor.postDiff(timelineDiff)
    }

    internal fun postPaginationStatus(status: BackPaginationStatus) {
        _paginationState.getAndUpdate { currentPaginationState ->
            when (status) {
                BackPaginationStatus.IDLE -> {
                    currentPaginationState.copy(
                        isBackPaginating = false,
                        canBackPaginate = true
                    )
                }
                BackPaginationStatus.PAGINATING -> {
                    currentPaginationState.copy(
                        isBackPaginating = true,
                        canBackPaginate = true
                    )
                }
                BackPaginationStatus.TIMELINE_START_REACHED -> {
                    currentPaginationState.copy(
                        isBackPaginating = false,
                        canBackPaginate = false
                    )
                }
            }
        }
    }

    override suspend fun fetchDetailsForEvent(eventId: EventId): Result<Unit> = withContext(coroutineDispatchers.io) {
        runCatching {
            innerRoom.fetchDetailsForEvent(eventId.value)
        }
    }

    override suspend fun paginateBackwards(requestSize: Int, untilNumberOfItems: Int): Result<Unit> = withContext(coroutineDispatchers.io) {
        runCatching {
            Timber.v("Start back paginating for room ${matrixRoom.roomId} ")
            val paginationOptions = PaginationOptions.UntilNumItems(
                eventLimit = requestSize.toUShort(),
                items = untilNumberOfItems.toUShort(),
                waitForToken = true,
            )
            innerRoom.paginateBackwards(paginationOptions)
        }.onFailure {
            Timber.e(it, "Fail to paginate for room ${matrixRoom.roomId}")
        }.onSuccess {
            Timber.v("Success back paginating for room ${matrixRoom.roomId}")
        }
    }

    override suspend fun sendReadReceipt(eventId: EventId) = withContext(coroutineDispatchers.io) {
        runCatching {
            innerRoom.sendReadReceipt(eventId = eventId.value)
        }
    }

    fun getItemById(eventId: EventId): MatrixTimelineItem.Event? {
        return _timelineItems.value.firstOrNull { (it as? MatrixTimelineItem.Event)?.eventId == eventId } as? MatrixTimelineItem.Event
    }
}
