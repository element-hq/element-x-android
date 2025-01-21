/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.toEventOrTransactionId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

interface MediaGalleryDataSource {
    fun start()
    fun groupedMediaItemsFlow(): Flow<AsyncData<GroupedMediaItems>>
    fun getLastData(): AsyncData<GroupedMediaItems>
    suspend fun loadMore(direction: Timeline.PaginationDirection)
    suspend fun deleteItem(eventId: EventId)
}

@SingleIn(RoomScope::class)
@ContributesBinding(RoomScope::class)
class TimelineMediaGalleryDataSource @Inject constructor(
    private val room: MatrixRoom,
    private val timelineMediaItemsFactory: TimelineMediaItemsFactory,
    private val mediaItemsPostProcessor: MediaItemsPostProcessor,
) : MediaGalleryDataSource {
    private var timeline: Timeline? = null

    private val groupedMediaItemsFlow = MutableSharedFlow<AsyncData<GroupedMediaItems>>(replay = 1)

    override fun groupedMediaItemsFlow(): Flow<AsyncData<GroupedMediaItems>> = groupedMediaItemsFlow

    override fun getLastData(): AsyncData<GroupedMediaItems> = groupedMediaItemsFlow.replayCache.firstOrNull() ?: AsyncData.Uninitialized

    private val isStarted = AtomicBoolean(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun start() {
        if (!isStarted.compareAndSet(false, true)) {
            return
        }
        flow {
            groupedMediaItemsFlow.emit(AsyncData.Loading())
            room.mediaTimeline().fold(
                {
                    timeline = it
                    emit(it)
                },
                {
                    groupedMediaItemsFlow.emit(AsyncData.Failure(it))
                },
            )
        }.flatMapLatest { timeline ->
            timeline.timelineItems.onEach {
                timelineMediaItemsFactory.replaceWith(
                    timelineItems = it,
                )
            }
        }.flatMapLatest {
            timelineMediaItemsFactory.timelineItems
        }.map { timelineItems ->
            mediaItemsPostProcessor.process(mediaItems = timelineItems)
        }.onEach { groupedMediaItems ->
            groupedMediaItemsFlow.emit(AsyncData.Success(groupedMediaItems))
        }
            .onCompletion {
                timeline?.close()
            }
            .launchIn(room.roomCoroutineScope)
    }

    override suspend fun loadMore(direction: Timeline.PaginationDirection) {
        timeline?.paginate(direction)
    }

    override suspend fun deleteItem(eventId: EventId) {
        timeline?.redactEvent(
            eventOrTransactionId = eventId.toEventOrTransactionId(),
            reason = null,
        )
    }
}
