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
import io.element.android.libraries.matrix.api.room.RoomSummary
import io.element.android.libraries.matrix.api.room.RoomSummaryDataSource
import io.element.android.libraries.matrix.impl.sync.roomListDiff
import io.element.android.libraries.matrix.impl.sync.state
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.RoomListEntry
import org.matrix.rustcomponents.sdk.SlidingSync
import org.matrix.rustcomponents.sdk.SlidingSyncList
import org.matrix.rustcomponents.sdk.SlidingSyncListRoomsListDiff
import org.matrix.rustcomponents.sdk.SlidingSyncSelectiveModeBuilder
import org.matrix.rustcomponents.sdk.SlidingSyncState
import org.matrix.rustcomponents.sdk.UpdateSummary
import timber.log.Timber
import java.io.Closeable
import java.util.UUID

internal class RustRoomSummaryDataSource(
    private val slidingSyncUpdateFlow: Flow<UpdateSummary>,
    private val slidingSync: SlidingSync,
    private val slidingSyncListFlow: Flow<SlidingSyncList>,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val roomSummaryDetailsFactory: RoomSummaryDetailsFactory = RoomSummaryDetailsFactory(),
) : RoomSummaryDataSource, Closeable {

    private val coroutineScope = CoroutineScope(SupervisorJob() + coroutineDispatchers.io)

    private val roomSummaries = MutableStateFlow<List<RoomSummary>>(emptyList())
    private val state = MutableStateFlow(SlidingSyncState.NOT_LOADED)

    fun init() {
        coroutineScope.launch {
            val slidingSyncList = slidingSyncListFlow.first()
            val summaries = slidingSyncList.currentRoomList().map(::buildSummaryForRoomListEntry)
            updateRoomSummaries {
                addAll(summaries)
            }

            slidingSyncList.roomListDiff(this)
                .onEach { diffs ->
                    updateRoomSummaries {
                        applyDiff(diffs)
                    }
                }
                .launchIn(this)

            slidingSyncList.state(this)
                .onEach { slidingSyncState ->
                    Timber.v("New sliding sync state: $slidingSyncState")
                    state.value = slidingSyncState
                }.launchIn(this)
        }

        slidingSyncUpdateFlow
            .onEach {
                didReceiveSyncUpdate(it)
            }.launchIn(coroutineScope)
    }

    override fun close() {
        runBlocking { slidingSyncListFlow.firstOrNull() }?.close()
        coroutineScope.cancel()
    }

    override fun roomSummaries(): StateFlow<List<RoomSummary>> {
        return roomSummaries
    }

    override fun setSlidingSyncRange(range: IntRange) {
        Timber.v("setVisibleRange=$range")
        coroutineScope.launch {
            val slidingSyncMode = SlidingSyncSelectiveModeBuilder()
                .addRange(range.first.toUInt(), range.last.toUInt())
            slidingSyncListFlow.first().setSyncMode(slidingSyncMode)
        }
    }

    private suspend fun didReceiveSyncUpdate(summary: UpdateSummary) {
        Timber.v("UpdateRooms with identifiers: ${summary.rooms}")
        if (state.value != SlidingSyncState.FULLY_LOADED) {
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

    private fun MutableList<RoomSummary>.applyDiff(diff: SlidingSyncListRoomsListDiff) {
        fun MutableList<RoomSummary>.fillUntil(untilIndex: Int) {
            repeat((size - 1 until untilIndex).count()) {
                add(buildEmptyRoomSummary())
            }
        }
        Timber.v("ApplyDiff: $diff for list with size: $size")
        when (diff) {
            is SlidingSyncListRoomsListDiff.Append -> {
                val roomSummaries = diff.values.map {
                    buildSummaryForRoomListEntry(it)
                }
                addAll(roomSummaries)
            }
            is SlidingSyncListRoomsListDiff.PushBack -> {
                val roomSummary = buildSummaryForRoomListEntry(diff.value)
                add(roomSummary)
            }
            is SlidingSyncListRoomsListDiff.PushFront -> {
                val roomSummary = buildSummaryForRoomListEntry(diff.value)
                add(0, roomSummary)
            }
            is SlidingSyncListRoomsListDiff.Set -> {
                fillUntil(diff.index.toInt())
                val roomSummary = buildSummaryForRoomListEntry(diff.value)
                set(diff.index.toInt(), roomSummary)
            }
            is SlidingSyncListRoomsListDiff.Insert -> {
                val roomSummary = buildSummaryForRoomListEntry(diff.value)
                add(diff.index.toInt(), roomSummary)
            }
            is SlidingSyncListRoomsListDiff.Remove -> {
                removeAt(diff.index.toInt())
            }
            is SlidingSyncListRoomsListDiff.Reset -> {
                clear()
                addAll(diff.values.map { buildSummaryForRoomListEntry(it) })
            }
            SlidingSyncListRoomsListDiff.PopBack -> {
                removeFirstOrNull()
            }
            SlidingSyncListRoomsListDiff.PopFront -> {
                removeLastOrNull()
            }
            SlidingSyncListRoomsListDiff.Clear -> {
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
        val slidingSyncRoom = slidingSync.getRoom(identifier) ?: return RoomSummary.Empty(identifier)
        val fullRoom = slidingSyncRoom.fullRoom()
        val roomSummary = RoomSummary.Filled(
            details = roomSummaryDetailsFactory.create(slidingSyncRoom, fullRoom)
        )
        fullRoom?.destroy()
        slidingSyncRoom.destroy()
        return roomSummary
    }

    private suspend fun updateRoomSummaries(block: MutableList<RoomSummary>.() -> Unit) =
        withContext(coroutineDispatchers.diffUpdateDispatcher) {
            val mutableRoomSummaries = roomSummaries.value.toMutableList()
            block(mutableRoomSummaries)
            roomSummaries.value = mutableRoomSummaries
        }
}
