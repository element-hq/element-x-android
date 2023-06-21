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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.RoomList
import org.matrix.rustcomponents.sdk.RoomListEntriesUpdate
import org.matrix.rustcomponents.sdk.RoomListEntry
import org.matrix.rustcomponents.sdk.RoomListInput
import org.matrix.rustcomponents.sdk.RoomListRange
import timber.log.Timber
import java.util.UUID

internal class RustRoomSummaryDataSource(
    private val roomList: RoomList,
    private val sessionCoroutineScope: CoroutineScope,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val roomSummaryDetailsFactory: RoomSummaryDetailsFactory = RoomSummaryDetailsFactory(),
) : RoomSummaryDataSource {

    private val roomSummaries = MutableStateFlow<List<RoomSummary>>(emptyList())
    private val loadingState = MutableStateFlow(RoomSummaryDataSource.LoadingState.NotLoaded)

    fun subscribeIfNeeded() {
        sessionCoroutineScope.launch {
            roomList.roomListEntriesUpdateFlow { roomListEntries ->
                val summaries = roomListEntries.map(::buildSummaryForRoomListEntry)
                updateRoomSummaries {
                    addAll(summaries)
                }
            }.onEach {
                updateRoomSummaries {
                    applyUpdate(it)
                }
            }.launchIn(this)
        }
    }

    override fun roomSummaries(): StateFlow<List<RoomSummary>> {
        return roomSummaries
    }

    override fun loadingState(): StateFlow<RoomSummaryDataSource.LoadingState> {
        return loadingState
    }

    override fun setSlidingSyncRange(range: IntRange) {
        Timber.v("setVisibleRange=$range")
        sessionCoroutineScope.launch {
            val ranges = listOf(RoomListRange(range.first.toUInt(), range.last.toUInt()))
            roomList.applyInput(
                RoomListInput.Viewport(ranges)
            )
        }
    }

    private fun MutableList<RoomSummary>.applyUpdate(update: RoomListEntriesUpdate) {
        fun MutableList<RoomSummary>.fillUntil(untilIndex: Int) {
            repeat((size - 1 until untilIndex).count()) {
                add(buildEmptyRoomSummary())
            }
        }
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
                fillUntil(update.index.toInt())
                val roomSummary = buildSummaryForRoomListEntry(update.value)
                set(update.index.toInt(), roomSummary)
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
                removeFirstOrNull()
            }
            RoomListEntriesUpdate.PopFront -> {
                removeLastOrNull()
            }
            RoomListEntriesUpdate.Clear -> {
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
        val roomListItem = roomList.roomOrNull(identifier) ?: return RoomSummary.Empty(identifier)
        return roomListItem.use {
            roomListItem.fullRoom().use { fullRoom ->
                RoomSummary.Filled(
                    details = roomSummaryDetailsFactory.create(roomListItem, fullRoom)
                )
            }
        }
    }

    private suspend fun updateRoomSummaries(block: MutableList<RoomSummary>.() -> Unit) =
        withContext(coroutineDispatchers.diffUpdateDispatcher) {
            val mutableRoomSummaries = roomSummaries.value.toMutableList()
            block(mutableRoomSummaries)
            roomSummaries.value = mutableRoomSummaries
        }
}
