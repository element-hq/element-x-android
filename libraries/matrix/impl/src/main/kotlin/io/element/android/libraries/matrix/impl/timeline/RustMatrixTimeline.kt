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

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.TimelineException
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import io.element.android.libraries.matrix.impl.timeline.item.event.EventMessageMapper
import io.element.android.libraries.matrix.impl.timeline.item.event.EventTimelineItemMapper
import io.element.android.libraries.matrix.impl.timeline.item.event.TimelineEventContentMapper
import io.element.android.libraries.matrix.impl.timeline.item.virtual.VirtualTimelineItemMapper
import io.element.android.libraries.matrix.impl.timeline.postprocessor.TimelineEncryptedHistoryPostProcessor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.BackPaginationStatus
import org.matrix.rustcomponents.sdk.EventItemOrigin
import org.matrix.rustcomponents.sdk.PaginationOptions
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.TimelineDiff
import org.matrix.rustcomponents.sdk.TimelineItem
import timber.log.Timber
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

private const val INITIAL_MAX_SIZE = 50

class RustMatrixTimeline(
    roomCoroutineScope: CoroutineScope,
    private val matrixRoom: MatrixRoom,
    private val innerRoom: Room,
    private val dispatcher: CoroutineDispatcher,
    private val lastLoginTimestamp: Date?,
    private val onNewSyncedEvent: () -> Unit,
) : MatrixTimeline {

    private val initLatch = CompletableDeferred<Unit>()
    private val isInit = AtomicBoolean(false)

    private val _timelineItems: MutableStateFlow<List<MatrixTimelineItem>> =
        MutableStateFlow(emptyList())

    private val _paginationState = MutableStateFlow(
        MatrixTimeline.PaginationState(hasMoreToLoadBackwards = true, isBackPaginating = false)
    )

    private val encryptedHistoryPostProcessor = TimelineEncryptedHistoryPostProcessor(
        lastLoginTimestamp = lastLoginTimestamp,
        isRoomEncrypted = matrixRoom.isEncrypted,
        paginationStateFlow = _paginationState,
        dispatcher = dispatcher,
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

    init {
        Timber.d("Initialize timeline for room ${matrixRoom.roomId}")

        roomCoroutineScope.launch(dispatcher) {
            innerRoom.timelineDiffFlow { initialList ->
                postItems(initialList)
            }.onEach { diffs ->
                if (diffs.any { diff -> diff.eventOrigin() == EventItemOrigin.SYNC }) {
                    onNewSyncedEvent()
                }
                postDiffs(diffs)
            }.launchIn(this)

            innerRoom.backPaginationStatusFlow()
                .onEach {
                    postPaginationStatus(it)
                }
                .launchIn(this)

            fetchMembers()
        }
    }

    private suspend fun fetchMembers() = withContext(dispatcher) {
        initLatch.await()
        innerRoom.fetchMembers()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val timelineItems: Flow<List<MatrixTimelineItem>> = _timelineItems.mapLatest { items ->
        encryptedHistoryPostProcessor.process(items)
    }

    private suspend fun postItems(items: List<TimelineItem>) = coroutineScope {
        // Split the initial items in multiple list as there is no pagination in the cached data, so we can post timelineItems asap.
        items.chunked(INITIAL_MAX_SIZE).reversed().forEach {
            ensureActive()
            timelineDiffProcessor.postItems(it)
        }
        isInit.set(true)
        initLatch.complete(Unit)
    }

    private suspend fun postDiffs(diffs: List<TimelineDiff>) {
        initLatch.await()
        timelineDiffProcessor.postDiffs(diffs)
    }

    private fun postPaginationStatus(status: BackPaginationStatus) {
        _paginationState.getAndUpdate { currentPaginationState ->
            if (hasEncryptionHistoryBanner()) {
                return@getAndUpdate currentPaginationState.copy(
                    isBackPaginating = false,
                    hasMoreToLoadBackwards = false,
                )
            }
            when (status) {
                BackPaginationStatus.IDLE -> {
                    currentPaginationState.copy(
                        isBackPaginating = false,
                        hasMoreToLoadBackwards = true
                    )
                }
                BackPaginationStatus.PAGINATING -> {
                    currentPaginationState.copy(
                        isBackPaginating = true,
                        hasMoreToLoadBackwards = true
                    )
                }
                BackPaginationStatus.TIMELINE_START_REACHED -> {
                    currentPaginationState.copy(
                        isBackPaginating = false,
                        hasMoreToLoadBackwards = false
                    )
                }
            }
        }
    }

    override suspend fun fetchDetailsForEvent(eventId: EventId): Result<Unit> = withContext(dispatcher) {
        runCatching {
            innerRoom.fetchDetailsForEvent(eventId.value)
        }
    }

    override suspend fun paginateBackwards(requestSize: Int, untilNumberOfItems: Int): Result<Unit> = withContext(dispatcher) {
        runCatching {
            if (!canBackPaginate()) throw TimelineException.CannotPaginate
            Timber.v("Start back paginating for room ${matrixRoom.roomId} ")
            val paginationOptions = PaginationOptions.UntilNumItems(
                eventLimit = requestSize.toUShort(),
                items = untilNumberOfItems.toUShort(),
                waitForToken = true,
            )
            innerRoom.paginateBackwards(paginationOptions)
        }.onFailure { error ->
            if (error is TimelineException.CannotPaginate) {
                Timber.d("Can't paginate backwards on room ${matrixRoom.roomId}, we're already at the start")
            } else {
                Timber.e(error, "Error paginating backwards on room ${matrixRoom.roomId}")
            }
        }.onSuccess {
            Timber.v("Success back paginating for room ${matrixRoom.roomId}")
        }
    }

    private fun canBackPaginate(): Boolean {
        return isInit.get() && paginationState.value.canBackPaginate
    }

    override suspend fun sendReadReceipt(eventId: EventId) = withContext(dispatcher) {
        runCatching {
            innerRoom.sendReadReceipt(eventId = eventId.value)
        }
    }

    fun getItemById(eventId: EventId): MatrixTimelineItem.Event? {
        return _timelineItems.value.firstOrNull { (it as? MatrixTimelineItem.Event)?.eventId == eventId } as? MatrixTimelineItem.Event
    }

    private fun hasEncryptionHistoryBanner(): Boolean {
        val firstItem = _timelineItems.value.firstOrNull()
        return firstItem is MatrixTimelineItem.Virtual
            && firstItem.virtual is VirtualTimelineItem.EncryptedHistoryBanner
    }
}
