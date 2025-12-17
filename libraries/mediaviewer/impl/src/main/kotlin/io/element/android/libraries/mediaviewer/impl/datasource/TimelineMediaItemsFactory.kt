/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.datasource

import dev.zacsweers.metro.Inject
import io.element.android.libraries.androidutils.diff.DefaultDiffCacheInvalidator
import io.element.android.libraries.androidutils.diff.DiffCacheUpdater
import io.element.android.libraries.androidutils.diff.MutableListDiffCache
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Inject
class TimelineMediaItemsFactory(
    private val dispatchers: CoroutineDispatchers,
    private val virtualItemFactory: VirtualItemFactory,
    private val eventItemFactory: EventItemFactory,
) {
    private val _timelineItems = MutableSharedFlow<ImmutableList<MediaItem>>(replay = 1)
    private val lock = Mutex()
    private val diffCache = MutableListDiffCache<MediaItem>()
    private val diffCacheUpdater = DiffCacheUpdater<MatrixTimelineItem, MediaItem>(
        diffCache = diffCache,
        detectMoves = false,
        cacheInvalidator = DefaultDiffCacheInvalidator()
    ) { old, new ->
        if (old is MatrixTimelineItem.Event && new is MatrixTimelineItem.Event) {
            old.uniqueId == new.uniqueId
        } else {
            false
        }
    }

    val timelineItems: Flow<ImmutableList<MediaItem>> = _timelineItems.distinctUntilChanged()

    suspend fun replaceWith(
        timelineItems: List<MatrixTimelineItem>,
    ) = withContext(dispatchers.computation) {
        lock.withLock {
            diffCacheUpdater.updateWith(timelineItems)
            buildAndEmitTimelineItemStates(timelineItems)
        }
    }

    private suspend fun buildAndEmitTimelineItemStates(
        timelineItems: List<MatrixTimelineItem>,
    ) {
        val newTimelineItemStates = ArrayList<MediaItem>()
        for (index in diffCache.indices().reversed()) {
            val cacheItem = diffCache.get(index)
            if (cacheItem == null) {
                buildAndCacheItem(timelineItems, index)?.also { timelineItemState ->
                    newTimelineItemStates.add(timelineItemState)
                }
            } else {
                newTimelineItemStates.add(cacheItem)
            }
        }
        _timelineItems.emit(newTimelineItemStates.toImmutableList())
    }

    private fun buildAndCacheItem(
        timelineItems: List<MatrixTimelineItem>,
        index: Int,
    ): MediaItem? {
        val timelineItem =
            when (val currentTimelineItem = timelineItems[index]) {
                is MatrixTimelineItem.Event -> eventItemFactory.create(currentTimelineItem)
                is MatrixTimelineItem.Virtual -> virtualItemFactory.create(currentTimelineItem)
                MatrixTimelineItem.Other -> null
            }
        diffCache[index] = timelineItem
        return timelineItem
    }
}
