/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomlist

import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.RoomListEntriesUpdate
import org.matrix.rustcomponents.sdk.RoomListServiceInterface
import org.matrix.rustcomponents.sdk.use
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

class RoomSummaryListProcessor(
    private val roomSummaries: MutableSharedFlow<List<RoomSummary>>,
    private val roomListService: RoomListServiceInterface,
    private val coroutineContext: CoroutineContext,
    private val roomSummaryFactory: RoomSummaryFactory,
    private val analyticsService: AnalyticsService,
) {
    private val updateSummariesMutex = Mutex()
    private val modifyPendingJobsMutex = Mutex()

    private val coroutineScope = CoroutineScope(coroutineContext + Dispatchers.Default)

    private var index = AtomicInteger(0)

    private var pendingJobChannel = Channel<UpdateEntry>(Channel.UNLIMITED)
    private var job: Job? = null

    init {
        setupChannel(reset = false)
    }

    private fun setupChannel(reset: Boolean) {
        if (reset) {
            job?.cancel()
            pendingJobChannel.close()
        }

        pendingJobChannel = Channel(Channel.UNLIMITED)

        job = coroutineScope.launch {
            while (coroutineContext.isActive) {
                val entry = pendingJobChannel.receive()
                Timber.d("Applying entry update #${entry.index}")
                updateRoomSummaries(entry.updates) {
                    for (update in entry.updates) {
                        applyUpdate(update)
                    }
                }
            }
        }
    }

    suspend fun postUpdate(updates: List<RoomListEntriesUpdate>) {
        modifyPendingJobsMutex.withLock {
            val firstUpdate = updates.firstOrNull()
            // If there were any pending jobs for the room list and we receive a reset/clear operation, cancel them
            // It makes no sense to apply them if we're going override them later
            val indexedUpdates = UpdateEntry(index.incrementAndGet(), updates)
            Timber.d("#${indexedUpdates.index} - Received entry update")

            if (firstUpdate is RoomListEntriesUpdate.Reset || firstUpdate is RoomListEntriesUpdate.Clear) {
                Timber.d("Cancelling all pending jobs: found entry update ${firstUpdate.javaClass}")
                setupChannel(reset = true)
            }

            pendingJobChannel.send(indexedUpdates)
        }
    }

    suspend fun rebuildRoomSummaries() {
        updateRoomSummaries(emptyList()) {
            forEachIndexed { i, summary ->
                val result = buildRoomSummaryForIdentifier(summary.roomId.value)
                if (result != null) {
                    this[i] = result
                }
            }
        }
    }

    private suspend fun MutableList<RoomSummary>.applyUpdate(update: RoomListEntriesUpdate) {
        // Remove this comment to debug changes in the room list
        // Timber.d("Apply room list update: ${update.describe()}")
        when (update) {
            is RoomListEntriesUpdate.Append -> {
                val roomSummaries = update.values.map {
                    buildSummaryForRoomListEntry(it)
                }
                addAll(roomSummaries)
            }
            is RoomListEntriesUpdate.PushBack -> {
                val roomSummary = buildSummaryForRoomListEntry(update.value)
                add(roomSummary)
            }
            is RoomListEntriesUpdate.PushFront -> {
                val roomSummary = buildSummaryForRoomListEntry(update.value)
                add(0, roomSummary)
            }
            is RoomListEntriesUpdate.Set -> {
                val roomSummary = buildSummaryForRoomListEntry(update.value)
                this[update.index.toInt()] = roomSummary
            }
            is RoomListEntriesUpdate.Insert -> {
                val roomSummary = buildSummaryForRoomListEntry(update.value)
                add(update.index.toInt(), roomSummary)
            }
            is RoomListEntriesUpdate.Remove -> {
                removeAt(update.index.toInt())
            }
            is RoomListEntriesUpdate.Reset -> {
                clear()
                addAll(update.values.map { buildSummaryForRoomListEntry(it) })
            }
            RoomListEntriesUpdate.PopBack -> {
                removeLastOrNull()
            }
            RoomListEntriesUpdate.PopFront -> {
                removeFirstOrNull()
            }
            RoomListEntriesUpdate.Clear -> {
                clear()
            }
            is RoomListEntriesUpdate.Truncate -> {
                subList(update.length.toInt(), size).clear()
            }
        }
    }

    private suspend fun buildSummaryForRoomListEntry(entry: Room): RoomSummary {
        return entry.use { roomSummaryFactory.create(room = it) }
    }

    private suspend fun buildRoomSummaryForIdentifier(identifier: String): RoomSummary? {
        return roomListService.roomOrNull(identifier)?.let { room ->
            buildSummaryForRoomListEntry(room)
        }
    }

    private suspend fun updateRoomSummaries(updates: List<RoomListEntriesUpdate>, block: suspend MutableList<RoomSummary>.() -> Unit) = withContext(
        coroutineContext
    ) {
        updateSummariesMutex.withLock {
            val current = roomSummaries.replayCache.lastOrNull()
            val mutableRoomSummaries = current.orEmpty().toMutableList()
            block(mutableRoomSummaries)

            // TODO remove once https://github.com/element-hq/element-x-android/issues/5031 has been confirmed as fixed
            val uniqueRooms = mutableRoomSummaries.distinctBy { it.roomId }

            if (uniqueRooms.size != mutableRoomSummaries.size) {
                val duplicates = mutableRoomSummaries.groupingBy { it.roomId }.eachCount().filter { it.value > 1 }
                if (duplicates.isNotEmpty()) {
                    analyticsService.trackError(
                        IllegalStateException(
                            "Found duplicates in room summaries after a list update from the SDK: $duplicates. " +
                                "Updates: ${updates.description()}"
                        )
                    )
                }
            }

            roomSummaries.emit(uniqueRooms)
        }
    }
}

private fun List<RoomListEntriesUpdate>.description(): String = joinToString { it.describe() }

data class UpdateEntry(
    val index: Int,
    val updates: List<RoomListEntriesUpdate>,
)
