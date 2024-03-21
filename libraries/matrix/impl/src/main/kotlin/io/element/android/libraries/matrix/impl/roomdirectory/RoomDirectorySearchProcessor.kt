/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.matrix.impl.roomdirectory

import io.element.android.libraries.matrix.api.roomdirectory.RoomDescription
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.RoomDirectorySearchEntryUpdate
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class RoomDirectorySearchProcessor(
    private val roomDescriptions: MutableSharedFlow<List<RoomDescription>>,
    private val coroutineContext: CoroutineContext,
    private val roomDescriptionMapper: RoomDescriptionMapper,
) {

    private val mutex = Mutex()

    suspend fun postUpdates(updates: List<RoomDirectorySearchEntryUpdate>) {
        updateRoomDescriptions {
            Timber.v("Update room descriptions from postUpdates (with ${updates.size} items) on ${Thread.currentThread()}")
            updates.forEach { update ->
                applyUpdate(update)
            }
        }
    }

    private suspend fun MutableList<RoomDescription>.applyUpdate(update: RoomDirectorySearchEntryUpdate) {
        when (update) {
            is RoomDirectorySearchEntryUpdate.Append -> {
                val roomSummaries = update.values.map(roomDescriptionMapper::map)
                addAll(roomSummaries)
            }
            is RoomDirectorySearchEntryUpdate.PushBack -> {
                val roomSummary = roomDescriptionMapper.map(update.value)
                add(roomSummary)
            }
            is RoomDirectorySearchEntryUpdate.PushFront -> {
                val roomSummary = roomDescriptionMapper.map(update.value)
                add(0, roomSummary)
            }
            is RoomDirectorySearchEntryUpdate.Set -> {
                val roomSummary = roomDescriptionMapper.map(update.value)
                this[update.index.toInt()] = roomSummary
            }
            is RoomDirectorySearchEntryUpdate.Insert -> {
                val roomSummary = roomDescriptionMapper.map(update.value)
                add(update.index.toInt(), roomSummary)
            }
            is RoomDirectorySearchEntryUpdate.Remove -> {
                removeAt(update.index.toInt())
            }
            is RoomDirectorySearchEntryUpdate.Reset -> {
                clear()
                addAll(update.values.map(roomDescriptionMapper::map))
            }
            RoomDirectorySearchEntryUpdate.PopBack -> {
                removeLastOrNull()
            }
            RoomDirectorySearchEntryUpdate.PopFront -> {
                removeFirstOrNull()
            }
            RoomDirectorySearchEntryUpdate.Clear -> {
                clear()
            }
            is RoomDirectorySearchEntryUpdate.Truncate -> {
                subList(update.length.toInt(), size).clear()
            }
        }
    }

    private suspend fun updateRoomDescriptions(block: suspend MutableList<RoomDescription>.() -> Unit) = withContext(coroutineContext) {
        mutex.withLock {
            val current = roomDescriptions.replayCache.lastOrNull()
            val mutableRoomSummaries = current.orEmpty().toMutableList()
            block(mutableRoomSummaries)
            roomDescriptions.emit(mutableRoomSummaries)
        }
    }
}
