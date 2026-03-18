/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.datasource

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.features.home.impl.bridge.BridgeEnrichmentService
import io.element.android.features.home.impl.bridge.BridgeTypeCache
import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.libraries.androidutils.diff.DiffCacheUpdater
import io.element.android.libraries.androidutils.diff.MutableListDiffCache
import io.element.android.libraries.androidutils.system.DateTimeObserver
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.api.roomlist.updateVisibleRange
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

private const val PAGE_SIZE = 20
private const val EXTENDED_VISIBILITY_RANGE_SIZE = 40
private const val SUBSCRIBE_TO_VISIBLE_ROOMS_DEBOUNCE_IN_MILLIS = 300L
private const val PAGINATION_THRESHOLD = 3 * PAGE_SIZE

@Inject
@SingleIn(SessionScope::class)
class RoomListDataSource(
    private val roomListService: RoomListService,
    private val roomListRoomSummaryFactory: RoomListRoomSummaryFactory,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val notificationSettingsService: NotificationSettingsService,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
    private val dateTimeObserver: DateTimeObserver,
    private val analyticsService: AnalyticsService,
    private val bridgeEnrichmentService: BridgeEnrichmentService,
    private val bridgeTypeCache: BridgeTypeCache,
) {
    init {
        observeNotificationSettings()
        observeDateTimeChanges()
        observeBridgeTypeCache()
    }

    private val roomList = roomListService.createRoomList(
        pageSize = PAGE_SIZE,
        source = RoomList.Source.All,
        coroutineScope = sessionCoroutineScope
    )
    private val _roomSummariesFlow = MutableSharedFlow<ImmutableList<RoomListRoomSummary>>(replay = 1)

    private val lock = Mutex()
    private val diffCache = MutableListDiffCache<RoomListRoomSummary>()
    private val diffCacheUpdater = DiffCacheUpdater<RoomSummary, RoomListRoomSummary>(diffCache = diffCache, detectMoves = true) { old, new ->
        old?.roomId == new?.roomId
    }

    val roomSummariesFlow: Flow<ImmutableList<RoomListRoomSummary>> = _roomSummariesFlow

    val loadingState = roomList.loadingState

    fun launchIn(coroutineScope: CoroutineScope) {
        roomList
            .summaries
            .onEach { roomSummaries ->
                replaceWith(roomSummaries)
            }
            .launchIn(coroutineScope)
    }

    suspend fun updateFilter(filter: RoomListFilter) {
        roomList.updateFilter(filter)
    }

    suspend fun updateVisibleRange(visibleRange: IntRange) = coroutineScope {
        launch {
            roomList.updateVisibleRange(visibleRange, PAGINATION_THRESHOLD)
        }
        launch {
            subscribeToVisibleRoomsIfNeeded(visibleRange)
        }
    }

    private var currentSubscribeToVisibleRoomsJob: Job? = null
    private fun CoroutineScope.subscribeToVisibleRoomsIfNeeded(range: IntRange) {
        currentSubscribeToVisibleRoomsJob?.cancel()
        currentSubscribeToVisibleRoomsJob = launch {
            // Debounce the subscription to avoid subscribing to too many rooms
            delay(SUBSCRIBE_TO_VISIBLE_ROOMS_DEBOUNCE_IN_MILLIS)

            if (range.isEmpty()) return@launch
            val currentRoomList = roomSummariesFlow.first()
            // Use extended range to 'prefetch' the next rooms info
            val midExtendedRangeSize = EXTENDED_VISIBILITY_RANGE_SIZE / 2
            val extendedRange = range.first until range.last + midExtendedRangeSize
            val roomIds = extendedRange.mapNotNull { index ->
                currentRoomList.getOrNull(index)?.roomId
            }
            roomListService.subscribeToVisibleRooms(roomIds)
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeNotificationSettings() {
        notificationSettingsService.notificationSettingsChangeFlow
            .debounce(0.5.seconds)
            .onEach {
                roomList.rebuildSummaries()
            }
            .launchIn(sessionCoroutineScope)
    }

    private fun observeDateTimeChanges() {
        dateTimeObserver.changes
            .onEach { event ->
                when (event) {
                    is DateTimeObserver.Event.TimeZoneChanged -> rebuildAllRoomSummaries()
                    is DateTimeObserver.Event.DateChanged -> rebuildAllRoomSummaries()
                }
            }
            .launchIn(sessionCoroutineScope)
    }

    private fun observeBridgeTypeCache() {
        bridgeTypeCache.cacheFlow
            .drop(1) // skip initial empty map
            .onEach { rebuildAllRoomSummaries() }
            .launchIn(sessionCoroutineScope)
    }

    private suspend fun replaceWith(roomSummaries: List<RoomSummary>) = withContext(coroutineDispatchers.computation) {
        lock.withLock {
            diffCacheUpdater.updateWith(roomSummaries)
            buildAndEmitAllRooms(roomSummaries)
        }
        triggerBridgeEnrichment()
    }

    private fun triggerBridgeEnrichment() {
        val summaries = _roomSummariesFlow.replayCache.firstOrNull() ?: return
        val roomIdsToEnrich = summaries
            .filter { it.bridgeType == null }
            .map { it.roomId }
        bridgeEnrichmentService.enrich(roomIdsToEnrich, sessionCoroutineScope)
    }

    private suspend fun buildAndEmitAllRooms(roomSummaries: List<RoomSummary>, useCache: Boolean = true) {
        // Used to detect duplicates in the room list summaries - see comment below
        data class CacheResult(val index: Int, val fromCache: Boolean)

        val cachingResults = mutableMapOf<RoomId, MutableList<CacheResult>>()

        val roomListRoomSummaries = diffCache.indices().mapNotNull { index ->
            if (useCache) {
                diffCache.get(index)?.let { cachedItem ->
                    // Add the cached item to the caching results
                    val pairs = cachingResults.getOrDefault(cachedItem.roomId, mutableListOf())
                    pairs.add(CacheResult(index, fromCache = true))
                    cachingResults[cachedItem.roomId] = pairs
                    cachedItem
                } ?: run {
                    roomSummaries.getOrNull(index)?.roomId?.let {
                        // Add the non-cached item to the caching results
                        val pairs = cachingResults.getOrDefault(it, mutableListOf())
                        pairs.add(CacheResult(index, fromCache = false))
                        cachingResults[it] = pairs
                    }
                    buildAndCacheItem(roomSummaries, index)
                }
            } else {
                roomSummaries.getOrNull(index)?.roomId?.let {
                    // Add the non-cached item to the caching results
                    val pairs = cachingResults.getOrDefault(it, mutableListOf())
                    pairs.add(CacheResult(index, fromCache = false))
                    cachingResults[it] = pairs
                }
                buildAndCacheItem(roomSummaries, index)
            }
        }

        // TODO remove once https://github.com/element-hq/element-x-android/issues/5031 has been confirmed as fixed
        val duplicates = cachingResults.filter { (_, operations) -> operations.size > 1 }
        if (duplicates.isNotEmpty()) {
            analyticsService.trackError(
                IllegalStateException(
                    "Found duplicates in room summaries after a local UI update: $duplicates. " +
                        "This could be a race condition/caching issue of some kind"
                )
            )

            // Remove duplicates before emitting the new values
            _roomSummariesFlow.emit(roomListRoomSummaries.distinctBy { it.roomId }.toImmutableList())
        } else {
            _roomSummariesFlow.emit(roomListRoomSummaries.toImmutableList())
        }
    }

    private fun buildAndCacheItem(roomSummaries: List<RoomSummary>, index: Int): RoomListRoomSummary? {
        val roomListSummary = roomSummaries.getOrNull(index)?.let { roomListRoomSummaryFactory.create(it) }
        diffCache[index] = roomListSummary
        return roomListSummary
    }

    private suspend fun rebuildAllRoomSummaries() {
        lock.withLock {
            roomList.summaries.replayCache.firstOrNull()?.let { roomSummaries ->
                buildAndEmitAllRooms(roomSummaries, useCache = false)
            }
        }
    }
}
