/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediagallery.impl.gallery

import io.element.android.libraries.androidutils.diff.DiffCacheUpdater
import io.element.android.libraries.androidutils.diff.MutableListDiffCache
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.mediagallery.impl.gallery.diff.TimelineMediaItemsCacheInvalidator
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class TimelineMediaItemsFactory @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val virtualItemFactory: VirtualItemFactory,
    private val eventItemFactory: EventItemFactory,
    private val systemClock: SystemClock,
) {
    private val _timelineItems = MutableSharedFlow<ImmutableList<MediaItem>>(replay = 1)
    private val lock = Mutex()
    private val diffCache = MutableListDiffCache<MediaItem>()
    private val diffCacheUpdater = DiffCacheUpdater<MatrixTimelineItem, MediaItem>(
        diffCache = diffCache,
        detectMoves = false,
        cacheInvalidator = TimelineMediaItemsCacheInvalidator()
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

    /**
     * Update the timestamp of the loading indicator, so that it may trigger a new pagination request.
     */
    suspend fun onCanPaginate() {
        lock.withLock {
            val values = _timelineItems.replayCache.firstOrNull() ?: return@withLock
            val lastItem = values.lastOrNull()
            if (lastItem is MediaItem.LoadingIndicator) {
                val newList = values.toMutableList().apply {
                    removeAt(size - 1)
                    val newTs = systemClock.epochMillis()
                    add(lastItem.copy(timestamp = newTs))
                }
                _timelineItems.emit(newList.toPersistentList())
            } else {
                Timber.w("onCanPaginate called but last item is not a loading indicator")
            }
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
        _timelineItems.emit(newTimelineItemStates.toPersistentList())
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
