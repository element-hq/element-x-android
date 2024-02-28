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

import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.RoomListEntriesUpdate
import org.matrix.rustcomponents.sdk.RoomListEntry
import org.matrix.rustcomponents.sdk.RoomListServiceInterface
import org.matrix.rustcomponents.sdk.use
import timber.log.Timber
import java.util.UUID
import kotlin.coroutines.CoroutineContext

class RoomSummaryListProcessor(
    private val roomSummaries: MutableSharedFlow<List<RoomSummary>>,
    private val roomListService: RoomListServiceInterface,
    private val coroutineContext: CoroutineContext,
    private val roomSummaryDetailsFactory: RoomSummaryDetailsFactory = RoomSummaryDetailsFactory(),
) {
    private val roomSummariesByIdentifier = HashMap<String, RoomSummary>()
    private val mutex = Mutex()

    suspend fun postUpdate(updates: List<RoomListEntriesUpdate>) {
        updateRoomSummaries {
            Timber.v("Update rooms from postUpdates (with ${updates.size} items) on ${Thread.currentThread()}")
            updates.forEach { update ->
                applyUpdate(update)
            }
        }
    }

    suspend fun rebuildRoomSummaries() {
        updateRoomSummaries {
            forEachIndexed { i, summary ->
                this[i] = when (summary) {
                    is RoomSummary.Empty -> summary
                    is RoomSummary.Filled -> buildAndCacheRoomSummaryForIdentifier(summary.identifier())
                }
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
            is RoomListEntriesUpdate.Truncate -> {
                subList(update.length.toInt(), size).clear()
            }
        }
    }

    private suspend fun buildSummaryForRoomListEntry(entry: RoomListEntry): RoomSummary {
        return when (entry) {
            RoomListEntry.Empty -> buildEmptyRoomSummary()
            is RoomListEntry.Filled -> buildAndCacheRoomSummaryForIdentifier(entry.roomId)
            is RoomListEntry.Invalidated -> {
                roomSummariesByIdentifier[entry.roomId] ?: buildAndCacheRoomSummaryForIdentifier(entry.roomId)
            }
        }
    }

    private fun buildEmptyRoomSummary(): RoomSummary {
        return RoomSummary.Empty(UUID.randomUUID().toString())
    }

    private suspend fun buildAndCacheRoomSummaryForIdentifier(identifier: String): RoomSummary {
        val builtRoomSummary = roomListService.roomOrNull(identifier)?.use { roomListItem ->
            roomListItem.roomInfo().use { roomInfo ->
                RoomSummary.Filled(
                    details = roomSummaryDetailsFactory.create(roomInfo)
                )
            }
        } ?: buildEmptyRoomSummary()
        roomSummariesByIdentifier[builtRoomSummary.identifier()] = builtRoomSummary
        return builtRoomSummary
    }

    private suspend fun updateRoomSummaries(block: suspend MutableList<RoomSummary>.() -> Unit) = withContext(coroutineContext) {
        mutex.withLock {
            val current = roomSummaries.replayCache.lastOrNull()
            val mutableRoomSummaries = current.orEmpty().toMutableList()
            block(mutableRoomSummaries)
            roomSummaries.emit(mutableRoomSummaries)
        }
    }
}
