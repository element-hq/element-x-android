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

package io.element.android.libraries.matrix.impl.roomlist

import io.element.android.libraries.core.coroutine.parallelMap
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.RoomListEntriesUpdate
import org.matrix.rustcomponents.sdk.RoomListEntry
import org.matrix.rustcomponents.sdk.RoomListItem
import org.matrix.rustcomponents.sdk.RoomListService
import timber.log.Timber
import java.util.UUID

class RoomSummaryListProcessor(
    private val roomSummaries: MutableStateFlow<List<RoomSummary>>,
    private val roomListService: RoomListService,
    private val roomSummaryDetailsFactory: RoomSummaryDetailsFactory = RoomSummaryDetailsFactory(),
    private val shouldFetchFullRoom: Boolean = false,
) {

    private val roomSummariesByIdentifier = HashMap<String, RoomSummary>()
    private val initLatch = CompletableDeferred<Unit>()
    private val mutex = Mutex()

    suspend fun postEntries(entries: List<RoomListEntry>) {
        updateRoomSummaries {
            Timber.v("Update rooms from postEntries (with ${entries.size} items) on ${Thread.currentThread()}")
            val roomSummaries = entries.parallelMap(::buildSummaryForRoomListEntry)
            addAll(roomSummaries)
        }
        initLatch.complete(Unit)
    }

    suspend fun postUpdate(updates: List<RoomListEntriesUpdate>) {
        // Makes sure to process first entries before update.
        initLatch.await()
        updateRoomSummaries {
            Timber.v("Update rooms from postUpdates (with ${updates.size} items) on ${Thread.currentThread()}")
            updates.forEach { update ->
                applyUpdate(update)
            }
        }
    }

    private suspend fun MutableList<RoomSummary>.applyUpdate(update: RoomListEntriesUpdate) {
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
        }
    }

    private suspend fun buildSummaryForRoomListEntry(entry: RoomListEntry): RoomSummary {
        return when (entry) {
            RoomListEntry.Empty -> buildEmptyRoomSummary()
            is RoomListEntry.Filled -> buildAndCacheRoomSummaryForIdentifier(entry.roomId)
            is RoomListEntry.Invalidated -> {
                roomSummariesByIdentifier[entry.roomId] ?: buildEmptyRoomSummary()
            }
        }
    }

    private fun buildEmptyRoomSummary(): RoomSummary {
        return RoomSummary.Empty(UUID.randomUUID().toString())
    }

    private suspend fun buildAndCacheRoomSummaryForIdentifier(identifier: String): RoomSummary {
        val builtRoomSummary = roomListService.roomOrNull(identifier)?.use { roomListItem ->
            roomListItem.fullRoomOrNull().use { fullRoom ->
                RoomSummary.Filled(
                    details = roomSummaryDetailsFactory.create(roomListItem, fullRoom)
                )
            }
        } ?: buildEmptyRoomSummary()
        roomSummariesByIdentifier[builtRoomSummary.identifier()] = builtRoomSummary
        return builtRoomSummary
    }

    private fun RoomListItem.fullRoomOrNull(): Room? {
        return if (shouldFetchFullRoom) {
            fullRoom()
        } else {
            null
        }
    }

    private suspend fun updateRoomSummaries(block: suspend MutableList<RoomSummary>.() -> Unit) =
        mutex.withLock {
            val mutableRoomSummaries = roomSummaries.value.toMutableList()
            block(mutableRoomSummaries)
            roomSummaries.value = mutableRoomSummaries
        }
}
