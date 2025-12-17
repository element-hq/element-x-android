/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomdirectory

import io.element.android.libraries.matrix.api.roomdirectory.RoomDescription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.RoomDirectorySearchEntryUpdate
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class RoomDirectorySearchProcessor(
    private val coroutineContext: CoroutineContext,
) {
    private val roomDescriptions: MutableSharedFlow<List<RoomDescription>> = MutableSharedFlow(replay = 1)
    val roomDescriptionsFlow: Flow<List<RoomDescription>> = roomDescriptions

    private val roomDescriptionMapper: RoomDescriptionMapper = RoomDescriptionMapper()
    private val mutex = Mutex()

    suspend fun postUpdates(updates: List<RoomDirectorySearchEntryUpdate>) {
        updateRoomDescriptions {
            Timber.v("Update room descriptions from postUpdates (with ${updates.size} items) on ${Thread.currentThread()}")
            updates.forEach { update ->
                applyUpdate(update)
            }
        }
    }

    private fun MutableList<RoomDescription>.applyUpdate(update: RoomDirectorySearchEntryUpdate) {
        when (update) {
            is RoomDirectorySearchEntryUpdate.Append -> {
                val roomSummaries = update.values.map(roomDescriptionMapper::map)
                addAll(roomSummaries)
            }
            is RoomDirectorySearchEntryUpdate.PushBack -> {
                val roomDescription = roomDescriptionMapper.map(update.value)
                add(roomDescription)
            }
            is RoomDirectorySearchEntryUpdate.PushFront -> {
                val roomDescription = roomDescriptionMapper.map(update.value)
                add(0, roomDescription)
            }
            is RoomDirectorySearchEntryUpdate.Set -> {
                val roomDescription = roomDescriptionMapper.map(update.value)
                this[update.index.toInt()] = roomDescription
            }
            is RoomDirectorySearchEntryUpdate.Insert -> {
                val roomDescription = roomDescriptionMapper.map(update.value)
                add(update.index.toInt(), roomDescription)
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
