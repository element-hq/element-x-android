/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.spaces

import io.element.android.libraries.matrix.api.spaces.SpaceServiceFilter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.matrix.rustcomponents.sdk.SpaceFilterUpdate
import timber.log.Timber

internal class SpaceServiceFilterUpdateProcessor(
    private val spaceFiltersFlow: MutableSharedFlow<List<SpaceServiceFilter>>,
    private val mapper: SpaceServiceFilterMapper,
) {
    private val mutex = Mutex()

    suspend fun postUpdates(updates: List<SpaceFilterUpdate>) {
        Timber.v("Update space filters from postUpdates (with ${updates.size} items) on ${Thread.currentThread()}")
        updateSpaceFilters {
            updates.forEach { update -> applyUpdate(update) }
        }
    }

    private suspend fun updateSpaceFilters(block: MutableList<SpaceServiceFilter>.() -> Unit) =
        mutex.withLock {
            val spaceFilters = if (spaceFiltersFlow.replayCache.isNotEmpty()) {
                spaceFiltersFlow.first().toMutableList()
            } else {
                mutableListOf()
            }
            block(spaceFilters)
            spaceFiltersFlow.emit(spaceFilters)
        }

    private fun MutableList<SpaceServiceFilter>.applyUpdate(update: SpaceFilterUpdate) {
        when (update) {
            is SpaceFilterUpdate.Append -> {
                val newFilters = update.values.map(mapper::map)
                addAll(newFilters)
            }
            SpaceFilterUpdate.Clear -> clear()
            is SpaceFilterUpdate.Insert -> {
                val newFilter = mapper.map(update.value)
                add(update.index.toInt(), newFilter)
            }
            SpaceFilterUpdate.PopBack -> {
                removeAt(lastIndex)
            }
            SpaceFilterUpdate.PopFront -> {
                removeAt(0)
            }
            is SpaceFilterUpdate.PushBack -> {
                val newFilter = mapper.map(update.value)
                add(newFilter)
            }
            is SpaceFilterUpdate.PushFront -> {
                val newFilter = mapper.map(update.value)
                add(0, newFilter)
            }
            is SpaceFilterUpdate.Remove -> {
                removeAt(update.index.toInt())
            }
            is SpaceFilterUpdate.Reset -> {
                clear()
                val newFilters = update.values.map(mapper::map)
                addAll(newFilters)
            }
            is SpaceFilterUpdate.Set -> {
                val newFilter = mapper.map(update.value)
                this[update.index.toInt()] = newFilter
            }
            is SpaceFilterUpdate.Truncate -> {
                subList(update.length.toInt(), size).clear()
            }
        }
    }
}
