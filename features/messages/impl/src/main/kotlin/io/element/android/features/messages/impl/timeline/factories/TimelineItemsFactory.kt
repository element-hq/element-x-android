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

package io.element.android.features.messages.impl.timeline.factories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import io.element.android.features.messages.impl.timeline.diff.TimelineItemsCacheInvalidator
import io.element.android.features.messages.impl.timeline.factories.event.TimelineItemEventFactory
import io.element.android.features.messages.impl.timeline.factories.virtual.TimelineItemVirtualFactory
import io.element.android.features.messages.impl.timeline.groups.TimelineItemGrouper
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.androidutils.diff.DiffCacheUpdater
import io.element.android.libraries.androidutils.diff.MutableListDiffCache
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TimelineItemsFactory @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val eventItemFactory: TimelineItemEventFactory,
    private val virtualItemFactory: TimelineItemVirtualFactory,
    private val timelineItemGrouper: TimelineItemGrouper,
) {
    private val timelineItems = MutableStateFlow(persistentListOf<TimelineItem>())

    private val lock = Mutex()
    private val diffCache = MutableListDiffCache<TimelineItem>()
    private val diffCacheUpdater = DiffCacheUpdater<MatrixTimelineItem, TimelineItem>(
        diffCache = diffCache,
        detectMoves = false,
        cacheInvalidator = TimelineItemsCacheInvalidator()
    ) { old, new ->
        if (old is MatrixTimelineItem.Event && new is MatrixTimelineItem.Event) {
            old.uniqueId == new.uniqueId
        } else {
            false
        }
    }

    @Composable
    fun collectItemsAsState(): State<ImmutableList<TimelineItem>> {
        return timelineItems.collectAsState()
    }

    suspend fun replaceWith(
        timelineItems: List<MatrixTimelineItem>,
    ) = withContext(dispatchers.computation) {
        lock.withLock {
            diffCacheUpdater.updateWith(timelineItems)
            buildAndEmitTimelineItemStates(timelineItems)
        }
    }

    private suspend fun buildAndEmitTimelineItemStates(timelineItems: List<MatrixTimelineItem>) {
        val newTimelineItemStates = ArrayList<TimelineItem>()
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
        val result = timelineItemGrouper.group(newTimelineItemStates).toPersistentList()
        this.timelineItems.emit(result)
    }

    private suspend fun buildAndCacheItem(
        timelineItems: List<MatrixTimelineItem>,
        index: Int
    ): TimelineItem? {
        val timelineItemState =
            when (val currentTimelineItem = timelineItems[index]) {
                is MatrixTimelineItem.Event -> eventItemFactory.create(currentTimelineItem, index, timelineItems)
                is MatrixTimelineItem.Virtual -> virtualItemFactory.create(currentTimelineItem)
                MatrixTimelineItem.Other -> null
            }
        diffCache[index] = timelineItemState
        return timelineItemState
    }
}
