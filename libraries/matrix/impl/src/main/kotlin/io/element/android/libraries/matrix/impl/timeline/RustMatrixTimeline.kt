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
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.withContext
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

    private val timelineItems: MutableStateFlow<List<MatrixTimelineItem>> =
        MutableStateFlow(emptyList())

    private val paginationState = MutableStateFlow(
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
        paginationState = paginationState,
        timelineItems = timelineItems,
        coroutineScope = roomCoroutineScope,
        diffDispatcher = coroutineDispatchers.diffUpdateDispatcher,
        timelineItemFactory = timelineItemFactory,
    )

    override fun paginationState(): StateFlow<MatrixTimeline.PaginationState> {
        return paginationState
    }

    @OptIn(FlowPreview::class)
    override fun timelineItems(): Flow<List<MatrixTimelineItem>> {
        return timelineItems.sample(50)
    }

    internal fun postItems(items: List<TimelineItem>)  {
        timelineItems.value = items.map(timelineItemFactory::map)
    }

    internal fun postDiff(timelineDiff: TimelineDiff) {
        timelineDiffProcessor.postDiff(timelineDiff)
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
}
