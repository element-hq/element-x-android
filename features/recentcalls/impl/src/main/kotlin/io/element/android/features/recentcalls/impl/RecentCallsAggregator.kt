/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.recentcalls.impl

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.features.recentcalls.impl.cache.RecentCallsCache
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CreateTimelineParams
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.Timeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber

@SingleIn(SessionScope::class)
@Inject
class RecentCallsAggregator(
    private val matrixClient: MatrixClient,
    private val cache: RecentCallsCache,
    private val normalizer: RecentCallsNormalizer,
    private val dispatchers: CoroutineDispatchers,
    @SessionCoroutineScope private val sessionScope: CoroutineScope,
) {
    private val mutex = Mutex()
    private val controllers = mutableMapOf<RoomId, RoomCallTimelineController>()
    private val controllerListFlow = MutableStateFlow<List<RoomCallTimelineController>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _canLoadMore = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

    private var roomListJob: Job? = null
    private var aggregationJob: Job? = null

    init {
        start()
    }

    private fun start() {
        roomListJob?.cancel()
        roomListJob = sessionScope.launch(dispatchers.io) {
            matrixClient.roomListService.allRooms.summaries
                .map { summaries -> summaries.filterJoinedNonSpaceRooms() }
                .distinctUntilChanged()
                .collect { summaries ->
                    syncControllers(summaries)
                }
        }
        startAggregation()
    }

    private fun startAggregation() {
        aggregationJob?.cancel()
        aggregationJob = sessionScope.launch(dispatchers.computation) {
            buildTimelineFlow().collect { pairs ->
                rebuild(pairs)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun buildTimelineFlow() = controllerListFlow.flatMapLatest { controllerList ->
        if (controllerList.isEmpty()) {
            flowOf(emptyList())
        } else {
            val flows = controllerList.map { controller ->
                controller.timeline.timelineItems.map { items ->
                    controller to items
                }
            }
            combine(flows) { pairs -> pairs.toList() }
        }
    }

    private suspend fun rebuild(pairs: List<Pair<RoomCallTimelineController, List<MatrixTimelineItem>>>) {
        val rawEvents = pairs.flatMap { (controller, items) ->
            controller.extractRawEvents(items)
        }
        val entries = normalizer.normalize(rawEvents, matrixClient.sessionId)
        cache.setTimelineEntries(entries)
        updateCanLoadMore(pairs.map { it.first })
        _isLoading.value = false
    }

    private suspend fun syncControllers(summaries: List<RoomSummary>) {
        _isLoading.value = true
        val joinedRoomIds = summaries.map { it.roomId }.toSet()
        val infoByRoomId = summaries.associateBy { it.roomId }
        val toRemove = mutex.withLock { controllers.keys - joinedRoomIds }
        toRemove.forEach { roomId ->
            mutex.withLock {
                controllers.remove(roomId)?.close()
            }
        }
        val toAdd = mutex.withLock { joinedRoomIds - controllers.keys }
        coroutineScope {
            toAdd.map { roomId ->
                launch {
                    addController(roomId, infoByRoomId[roomId]?.info ?: return@launch)
                }
            }.joinAll()
        }
        publishControllerList()
    }

    private suspend fun publishControllerList() {
        controllerListFlow.value = mutex.withLock { controllers.values.toList() }
    }

    private suspend fun addController(roomId: RoomId, roomInfo: RoomInfo) {
        val room = matrixClient.getJoinedRoom(roomId) ?: return
        val timeline = room.createTimeline(CreateTimelineParams.CallHistory).getOrElse {
            Timber.w(it, "Failed to create call history timeline for $roomId")
            return
        }
        paginateInitially(timeline)
        mutex.withLock {
            controllers[roomId] = RoomCallTimelineController(
                roomId = roomId,
                roomInfo = roomInfo,
                timeline = timeline,
            )
        }
        publishControllerList()
    }

    private suspend fun paginateInitially(timeline: Timeline) {
        repeat(INITIAL_PAGINATION_COUNT) {
            val hasMore = timeline.paginate(Timeline.PaginationDirection.BACKWARDS)
                .getOrDefault(false)
            if (!hasMore) return
        }
    }

    suspend fun loadMore() = withContext(dispatchers.io) {
        val controllerList = mutex.withLock { controllers.values.toList() }
        coroutineScope {
            controllerList
                .filter { it.canPaginateBackwards() }
                .map { controller ->
                    launch { controller.paginateBackwards() }
                }
                .joinAll()
        }
        updateCanLoadMore(controllerList)
    }

    private fun updateCanLoadMore(controllerList: List<RoomCallTimelineController>) {
        _canLoadMore.value = controllerList.any { it.canPaginateBackwards() }
    }

    private fun List<RoomSummary>.filterJoinedNonSpaceRooms(): List<RoomSummary> {
        return filter { summary ->
            summary.info.currentUserMembership == CurrentUserMembership.JOINED && !summary.info.isSpace
        }
    }

    companion object {
        private const val INITIAL_PAGINATION_COUNT = 2
    }
}
