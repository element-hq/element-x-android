/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.datasource

import dev.zacsweers.metro.Inject
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

    val timelineItems: Flow<ImmutableList<MediaItem>> = _timelineItems.distinctUntilChanged()

    suspend fun replaceWith(
        timelineItems: List<MatrixTimelineItem>,
    ) = withContext(dispatchers.computation) {
        lock.withLock {
            val newTimelineItemStates = ArrayList<MediaItem>()
            for (index in timelineItems.indices.reversed()) {
                when (val currentTimelineItem = timelineItems[index]) {
                    is MatrixTimelineItem.Event -> {
                        // create() returns a list to support gallery expansion into multiple items
                        // Reverse the list since we're iterating timeline items in reverse
                        val items = eventItemFactory.create(currentTimelineItem)
                        newTimelineItemStates.addAll(items.asReversed())
                    }
                    is MatrixTimelineItem.Virtual -> {
                        virtualItemFactory.create(currentTimelineItem)?.also {
                            newTimelineItemStates.add(it)
                        }
                    }
                    MatrixTimelineItem.Other -> Unit
                }
            }
            _timelineItems.emit(newTimelineItemStates.toImmutableList())
        }
    }
}
