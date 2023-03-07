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

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.impl.sync.roomListDiff
import io.element.android.libraries.matrix.impl.sync.state
import io.element.android.libraries.matrix.api.room.RoomSummary
import io.element.android.libraries.matrix.api.room.RoomSummaryDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.RoomListEntry
import org.matrix.rustcomponents.sdk.SlidingSync
import org.matrix.rustcomponents.sdk.SlidingSyncState
import org.matrix.rustcomponents.sdk.SlidingSyncView
import org.matrix.rustcomponents.sdk.SlidingSyncViewRoomsListDiff
import org.matrix.rustcomponents.sdk.UpdateSummary
import timber.log.Timber
import java.io.Closeable
import java.util.UUID

internal class RustRoomSummaryDataSource(
    private val slidingSyncUpdateFlow: Flow<UpdateSummary>,
    private val slidingSync: SlidingSync,
    private val slidingSyncView: SlidingSyncView,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val onRestartSync: () -> Unit,
    private val roomSummaryDetailsFactory: RoomSummaryDetailsFactory = RoomSummaryDetailsFactory(),
) : RoomSummaryDataSource, Closeable {

    private val coroutineScope = CoroutineScope(SupervisorJob() + coroutineDispatchers.io)

    private val roomSummaries = MutableStateFlow<List<RoomSummary>>(emptyList())
    private val state = MutableStateFlow(SlidingSyncState.COLD)

    fun init() {
        coroutineScope.launch {
            updateRoomSummaries {
                addAll(
                    slidingSyncView.currentRoomsList().map(::buildSummaryForRoomListEntry)
                )
            }
        }

        slidingSyncUpdateFlow
            .onEach {
                didReceiveSyncUpdate(it)
            }.launchIn(coroutineScope)

        slidingSyncView.roomListDiff(coroutineScope)
            .onEach { diffs ->
                updateRoomSummaries {
                    applyDiff(diffs)
                }
            }
            .launchIn(coroutineScope)

        slidingSyncView.state(coroutineScope)
            .onEach { slidingSyncState ->
                Timber.v("New sliding sync state: $slidingSyncState")
                state.value = slidingSyncState
            }.launchIn(coroutineScope)
    }

    override fun close() {
        coroutineScope.cancel()
    }

    //@OptIn(FlowPreview::class)
    override fun roomSummaries(): StateFlow<List<RoomSummary>> {
        return roomSummaries
    }

    override fun setSlidingSyncRange(range: IntRange) {
        Timber.v("setVisibleRange=$range")
        slidingSyncView.setRange(range.first.toUInt(), range.last.toUInt())
        onRestartSync()
    }

    private suspend fun didReceiveSyncUpdate(summary: UpdateSummary) {
        Timber.v("UpdateRooms with identifiers: ${summary.rooms}")
        if (state.value != SlidingSyncState.LIVE) {
            return
        }
        updateRoomSummaries {
            for (identifier in summary.rooms) {
                val index = indexOfFirst { it.identifier() == identifier }
                if (index == -1) {
                    continue
                }
                val updatedRoomSummary = buildRoomSummaryForIdentifier(identifier)
                set(index, updatedRoomSummary)
            }
        }
    }

    private fun MutableList<RoomSummary>.applyDiff(diff: SlidingSyncViewRoomsListDiff) {
        fun MutableList<RoomSummary>.fillUntil(untilIndex: Int) {
            repeat((size - 1 until untilIndex).count()) {
                add(buildEmptyRoomSummary())
            }
        }
        Timber.v("ApplyDiff: $diff for list with size: $size")
        when (diff) {
            is SlidingSyncViewRoomsListDiff.Append -> {
                val roomSummaries = diff.values.map {
                    buildSummaryForRoomListEntry(it)
                }
                addAll(roomSummaries)
            }
            is SlidingSyncViewRoomsListDiff.PushBack -> {
                val roomSummary = buildSummaryForRoomListEntry(diff.value)
                add(roomSummary)
            }
            is SlidingSyncViewRoomsListDiff.PushFront -> {
                val roomSummary = buildSummaryForRoomListEntry(diff.value)
                add(0, roomSummary)
            }
            is SlidingSyncViewRoomsListDiff.Set -> {
                fillUntil(diff.index.toInt())
                val roomSummary = buildSummaryForRoomListEntry(diff.value)
                set(diff.index.toInt(), roomSummary)
            }
            is SlidingSyncViewRoomsListDiff.Insert -> {
                val roomSummary = buildSummaryForRoomListEntry(diff.value)
                add(diff.index.toInt(), roomSummary)
            }
            is SlidingSyncViewRoomsListDiff.Remove -> {
                removeAt(diff.index.toInt())
            }
            is SlidingSyncViewRoomsListDiff.Reset -> {
                clear()
                addAll(diff.values.map { buildSummaryForRoomListEntry(it) })
            }
            SlidingSyncViewRoomsListDiff.PopBack -> {
                removeFirstOrNull()
            }
            SlidingSyncViewRoomsListDiff.PopFront -> {
                removeLastOrNull()
            }
            SlidingSyncViewRoomsListDiff.Clear -> {
                clear()
            }
        }
    }

    private fun buildSummaryForRoomListEntry(entry: RoomListEntry): RoomSummary {
        return when (entry) {
            RoomListEntry.Empty -> buildEmptyRoomSummary()
            is RoomListEntry.Invalidated -> buildRoomSummaryForIdentifier(entry.roomId)
            is RoomListEntry.Filled -> buildRoomSummaryForIdentifier(entry.roomId)
        }
    }

    private fun buildEmptyRoomSummary(): RoomSummary {
        return RoomSummary.Empty(UUID.randomUUID().toString())
    }

    private fun buildRoomSummaryForIdentifier(identifier: String): RoomSummary {
        val room = slidingSync.getRoom(identifier) ?: return RoomSummary.Empty(identifier)
        return RoomSummary.Filled(
            details = roomSummaryDetailsFactory.create(room, room.fullRoom())
        )
    }

    private suspend fun updateRoomSummaries(block: MutableList<RoomSummary>.() -> Unit) =
        withContext(coroutineDispatchers.diffUpdateDispatcher) {
            val mutableRoomSummaries = roomSummaries.value.toMutableList()
            block(mutableRoomSummaries)
            roomSummaries.value = mutableRoomSummaries
        }
}
