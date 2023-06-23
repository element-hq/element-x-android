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
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.RoomListEntriesUpdate
import org.matrix.rustcomponents.sdk.RoomListEntry
import org.matrix.rustcomponents.sdk.RoomListException
import org.matrix.rustcomponents.sdk.RoomListInput
import org.matrix.rustcomponents.sdk.RoomListRange
import org.matrix.rustcomponents.sdk.RoomListService
import timber.log.Timber
import java.util.UUID

internal class RustRoomSummaryDataSource(
    private val roomListService: RoomListService,
    private val sessionCoroutineScope: CoroutineScope,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val roomSummaryDetailsFactory: RoomSummaryDetailsFactory = RoomSummaryDetailsFactory(),
) : RoomSummaryDataSource {

    private val roomList = MutableStateFlow<List<RoomSummary>>(emptyList())
    private val inviteList = MutableStateFlow<List<RoomSummary>>(emptyList())
    private val loadingState = MutableStateFlow(RoomSummaryDataSource.LoadingState.NotLoaded)

    fun init() {
        sessionCoroutineScope.launch(coroutineDispatchers.computation) {
            roomListService.allRooms().entriesFlow { roomListEntries ->
                roomList.value = roomListEntries.map(::buildSummaryForRoomListEntry)
            }.onEach { update ->
                roomList.getAndUpdate {
                    it.applyUpdate(update)
                }
            }.launchIn(this)
        }
    }

    override fun roomList(): StateFlow<List<RoomSummary>> {
        return roomList
    }

    override fun inviteList(): StateFlow<List<RoomSummary>> {
        return inviteList
    }

    override fun loadingState(): StateFlow<RoomSummaryDataSource.LoadingState> {
        return loadingState
    }

    override fun updateRoomListVisibleRange(range: IntRange) {
        Timber.v("setVisibleRange=$range")
        sessionCoroutineScope.launch {
            try {
                val ranges = listOf(RoomListRange(range.first.toUInt(), range.last.toUInt()))
                roomListService.applyInput(
                    RoomListInput.Viewport(ranges)
                )
            } catch (exception: RoomListException) {
                Timber.e(exception, "Failed updating visible range")
            }
        }
    }

    private fun List<RoomSummary>.applyUpdate(update: RoomListEntriesUpdate): List<RoomSummary> {
        val newList = toMutableList()
        when (update) {
            is RoomListEntriesUpdate.Append -> {
                val roomSummaries = update.values.map {
                    buildSummaryForRoomListEntry(it)
                }
                newList.addAll(roomSummaries)
            }
            is RoomListEntriesUpdate.PushBack -> {
                val roomSummary = buildSummaryForRoomListEntry(update.value)
                newList.add(roomSummary)
            }
            is RoomListEntriesUpdate.PushFront -> {
                val roomSummary = buildSummaryForRoomListEntry(update.value)
                newList.add(0, roomSummary)
            }
            is RoomListEntriesUpdate.Set -> {
                val roomSummary = buildSummaryForRoomListEntry(update.value)
                newList[update.index.toInt()] = roomSummary
            }
            is RoomListEntriesUpdate.Insert -> {
                val roomSummary = buildSummaryForRoomListEntry(update.value)
                newList.add(update.index.toInt(), roomSummary)
            }
            is RoomListEntriesUpdate.Remove -> {
                newList.removeAt(update.index.toInt())
            }
            is RoomListEntriesUpdate.Reset -> {
                newList.clear()
                newList.addAll(update.values.map { buildSummaryForRoomListEntry(it) })
            }
            RoomListEntriesUpdate.PopBack -> {
                newList.removeFirstOrNull()
            }
            RoomListEntriesUpdate.PopFront -> {
                newList.removeLastOrNull()
            }
            RoomListEntriesUpdate.Clear -> {
                newList.clear()
            }
        }
        return newList
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
        val roomListItem = roomListService.roomOrNull(identifier) ?: return RoomSummary.Empty(identifier)
        return roomListItem.use {
            roomListItem.fullRoom().use { fullRoom ->
                RoomSummary.Filled(
                    details = roomSummaryDetailsFactory.create(roomListItem, fullRoom)
                )
            }
        }
    }
}
