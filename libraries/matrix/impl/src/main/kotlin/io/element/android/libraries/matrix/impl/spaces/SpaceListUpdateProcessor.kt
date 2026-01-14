/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.spaces

import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.matrix.rustcomponents.sdk.SpaceListUpdate
import timber.log.Timber

internal class SpaceListUpdateProcessor(
    private val spaceRoomsFlow: MutableSharedFlow<List<SpaceRoom>>,
    private val mapper: SpaceRoomMapper,
    private val analyticsService: AnalyticsService,
) {
    private val mutex = Mutex()

    suspend fun postUpdates(updates: List<SpaceListUpdate>) {
        Timber.v("Update space rooms from postUpdates (with ${updates.size} items) on ${Thread.currentThread()}")
        updateSpaceRooms(updates) {
            updates.forEach { update -> applyUpdate(update) }
        }
    }

    private suspend fun updateSpaceRooms(updates: List<SpaceListUpdate>, block: MutableList<SpaceRoom>.() -> Unit) =
        mutex.withLock {
            val spaceRooms = if (spaceRoomsFlow.replayCache.isNotEmpty()) {
                spaceRoomsFlow.first().toMutableList()
            } else {
                mutableListOf()
            }
            block(spaceRooms)
            val uniqueRooms = spaceRooms.distinctBy { it.roomId }

            // TODO remove once https://github.com/element-hq/element-x-android/issues/5031 has been confirmed as fixed
            if (spaceRooms.size != uniqueRooms.size) {
                val duplicateKeys = spaceRooms.groupBy { it.roomId }.filter { it.value.size > 1 }.keys
                analyticsService.trackError(
                    IllegalStateException("Found duplicate keys in space rooms list ($duplicateKeys) after SDK updates: ${updates.description()}")
                )
            }

            spaceRoomsFlow.emit(uniqueRooms)
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

private fun List<SpaceListUpdate>.description(): String = joinToString { it.description() }

private fun SpaceListUpdate.description(): String = when (this) {
    is SpaceListUpdate.Append -> "Append(${values.map { it.roomId }})"
    SpaceListUpdate.Clear -> "Clear"
    is SpaceListUpdate.Insert -> "Insert($index, ${value.roomId})"
    SpaceListUpdate.PopBack -> "PopBack"
    SpaceListUpdate.PopFront -> "PopFront"
    is SpaceListUpdate.PushBack -> "PushBack(${value.roomId})"
    is SpaceListUpdate.PushFront -> "PushFront(${value.roomId})"
    is SpaceListUpdate.Remove -> "Remove($index)"
    is SpaceListUpdate.Reset -> "Reset(${values.map { it.roomId }})"
    is SpaceListUpdate.Set -> "Set($index, ${value.roomId})"
    is SpaceListUpdate.Truncate -> "Truncate($length)"
}
