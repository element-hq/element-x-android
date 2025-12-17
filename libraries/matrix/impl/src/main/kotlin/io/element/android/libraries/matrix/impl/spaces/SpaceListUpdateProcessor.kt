/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.spaces

import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.matrix.rustcomponents.sdk.SpaceListUpdate
import timber.log.Timber

internal class SpaceListUpdateProcessor(
    private val spaceRoomsFlow: MutableSharedFlow<List<SpaceRoom>>,
    private val mapper: SpaceRoomMapper,
) {
    private val mutex = Mutex()

    suspend fun postUpdates(updates: List<SpaceListUpdate>) {
        Timber.v("Update space rooms from postUpdates (with ${updates.size} items) on ${Thread.currentThread()}")
        updateSpaceRooms {
            updates.forEach { update -> applyUpdate(update) }
        }
    }

    private suspend fun updateSpaceRooms(block: MutableList<SpaceRoom>.() -> Unit) =
        mutex.withLock {
            val spaceRooms = if (spaceRoomsFlow.replayCache.isNotEmpty()) {
                spaceRoomsFlow.first().toMutableList()
            } else {
                mutableListOf()
            }
            block(spaceRooms)
            spaceRoomsFlow.emit(spaceRooms)
        }

    private fun MutableList<SpaceRoom>.applyUpdate(update: SpaceListUpdate) {
        when (update) {
            is SpaceListUpdate.Append -> {
                val newSpaces = update.values.map(mapper::map)
                addAll(newSpaces)
            }
            SpaceListUpdate.Clear -> clear()
            is SpaceListUpdate.Insert -> {
                val newSpace = mapper.map(update.value)
                add(update.index.toInt(), newSpace)
            }
            SpaceListUpdate.PopBack -> {
                removeAt(lastIndex)
            }
            SpaceListUpdate.PopFront -> {
                removeAt(0)
            }
            is SpaceListUpdate.PushBack -> {
                val newSpace = mapper.map(update.value)
                add(newSpace)
            }
            is SpaceListUpdate.PushFront -> {
                val newSpace = mapper.map(update.value)
                add(0, newSpace)
            }
            is SpaceListUpdate.Remove -> {
                removeAt(update.index.toInt())
            }
            is SpaceListUpdate.Reset -> {
                clear()
                val newSpaces = update.values.map(mapper::map)
                addAll(newSpaces)
            }
            is SpaceListUpdate.Set -> {
                val newSpace = mapper.map(update.value)
                this[update.index.toInt()] = newSpace
            }
            is SpaceListUpdate.Truncate -> {
                subList(update.length.toInt(), size).clear()
            }
        }
    }
}
