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

package io.element.android.features.messages.timeline.factories

import androidx.recyclerview.widget.DiffUtil
import io.element.android.features.messages.timeline.diff.CacheInvalidator
import io.element.android.features.messages.timeline.diff.MatrixTimelineItemsDiffCallback
import io.element.android.features.messages.timeline.factories.event.TimelineItemEventFactory
import io.element.android.features.messages.timeline.factories.virtual.TimelineItemVirtualFactory
import io.element.android.features.messages.timeline.model.TimelineItem
import io.element.android.features.messages.timeline.util.invalidateLast
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.timeline.MatrixTimelineItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.ArrayList
import javax.inject.Inject
import kotlin.system.measureTimeMillis

class TimelineItemsFactory @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val eventItemFactory: TimelineItemEventFactory,
    private val virtualItemFactory: TimelineItemVirtualFactory,
) {

    private val timelineItems = MutableStateFlow<List<TimelineItem>>(emptyList())
    private val timelineItemsCache = arrayListOf<TimelineItem?>()

    // Items from rust sdk, used for diffing
    private var matrixTimelineItems: List<MatrixTimelineItem> = emptyList()

    private val lock = Mutex()
    private val cacheInvalidator = CacheInvalidator(timelineItemsCache)

    fun flow(): StateFlow<List<TimelineItem>> = timelineItems.asStateFlow()

    suspend fun replaceWith(
        timelineItems: List<MatrixTimelineItem>,
    ) = withContext(dispatchers.computation) {
        lock.withLock {
            calculateAndApplyDiff(timelineItems)
            buildAndEmitTimelineItemStates(timelineItems)
        }
    }

    suspend fun pushItem(
        timelineItem: MatrixTimelineItem,
    ) = withContext(dispatchers.computation) {
        lock.withLock {
            // Makes sure to invalidate last as we need to recompute some data (like groupPosition)
            timelineItemsCache.invalidateLast()
            timelineItemsCache.add(null)
            matrixTimelineItems = matrixTimelineItems + timelineItem
            buildAndEmitTimelineItemStates(matrixTimelineItems)
        }
    }

    private suspend fun buildAndEmitTimelineItemStates(timelineItems: List<MatrixTimelineItem>) {
        val newTimelineItemStates = ArrayList<TimelineItem>()
        for (index in timelineItemsCache.indices.reversed()) {
            val cacheItem = timelineItemsCache[index]
            if (cacheItem == null) {
                buildAndCacheItem(timelineItems, index)?.also { timelineItemState ->
                    newTimelineItemStates.add(timelineItemState)
                }
            } else {
                newTimelineItemStates.add(cacheItem)
            }
        }
        this.timelineItems.emit(newTimelineItemStates)
    }

    private fun calculateAndApplyDiff(newTimelineItems: List<MatrixTimelineItem>) {
        val timeToDiff = measureTimeMillis {
            val diffCallback =
                MatrixTimelineItemsDiffCallback(
                    oldList = matrixTimelineItems,
                    newList = newTimelineItems
                )
            val diffResult = DiffUtil.calculateDiff(diffCallback, false)
            matrixTimelineItems = newTimelineItems
            diffResult.dispatchUpdatesTo(cacheInvalidator)
        }
        Timber.v("Time to apply diff on new list of ${newTimelineItems.size} items: $timeToDiff ms")
    }

    private suspend fun buildAndCacheItem(
        timelineItems: List<MatrixTimelineItem>,
        index: Int
    ): TimelineItem? {
        val timelineItemState =
            when (val currentTimelineItem = timelineItems[index]) {
                is MatrixTimelineItem.Event -> eventItemFactory.create(currentTimelineItem, index, timelineItems)
                is MatrixTimelineItem.Virtual -> virtualItemFactory.create(currentTimelineItem, index, timelineItems)
                MatrixTimelineItem.Other -> null
            }
        timelineItemsCache[index] = timelineItemState
        return timelineItemState
    }
}
