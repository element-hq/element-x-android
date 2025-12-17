/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline

import androidx.compose.ui.util.fastForEach
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.libraries.matrix.api.timeline.item.event.TimelineItemEventOrigin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.matrix.rustcomponents.sdk.TimelineDiff
import org.matrix.rustcomponents.sdk.TimelineItem
import timber.log.Timber

internal class MatrixTimelineDiffProcessor(
    private val timelineItems: MutableSharedFlow<List<MatrixTimelineItem>>,
    private val membershipChangeEventReceivedFlow: MutableSharedFlow<Unit>,
    private val syncedEventReceivedFlow: MutableSharedFlow<Unit>,
    private val timelineItemMapper: MatrixTimelineItemMapper,
) {
    private val mutex = Mutex()

    suspend fun postDiffs(diffs: List<TimelineDiff>) {
        mutex.withLock {
            Timber.v("Update timeline items from postDiffs (with ${diffs.size} items) on ${Thread.currentThread()}")
            val result = processDiffs(diffs)
            timelineItems.emit(result.items())
            if (result.hasNewEventsFromSync) {
                syncedEventReceivedFlow.emit(Unit)
            }
            if (result.hasMembershipChangeEventFromSync) {
                membershipChangeEventReceivedFlow.emit(Unit)
            }
        }
    }

    private suspend fun processDiffs(diffs: List<TimelineDiff>): DiffingResult {
        val timelineItems = if (timelineItems.replayCache.isNotEmpty()) {
            timelineItems.first()
        } else {
            emptyList()
        }
        val result = DiffingResult(timelineItems)
        diffs.forEach { diff ->
            result.applyDiff(diff)
        }
        return result
    }

    private fun DiffingResult.applyDiff(diff: TimelineDiff) {
        when (diff) {
            is TimelineDiff.Append -> {
                diff.values.fastForEach { item ->
                    add(item.map())
                }
            }
            is TimelineDiff.PushBack -> {
                val item = diff.value.map()
                add(item)
            }
            is TimelineDiff.PushFront -> {
                val item = diff.value.map()
                add(0, item)
            }
            is TimelineDiff.Set -> {
                val item = diff.value.map()
                set(diff.index.toInt(), item)
            }
            is TimelineDiff.Insert -> {
                val item = diff.value.map()
                add(diff.index.toInt(), item)
            }
            is TimelineDiff.Remove -> {
                removeAt(diff.index.toInt())
            }
            is TimelineDiff.Reset -> {
                clear()
                diff.values.fastForEach { item ->
                    add(item.map())
                }
            }
            TimelineDiff.PopFront -> {
                removeFirst()
            }
            TimelineDiff.PopBack -> {
                removeLast()
            }
            TimelineDiff.Clear -> {
                clear()
            }
            is TimelineDiff.Truncate -> {
                truncate(diff.length.toInt())
            }
        }
    }

    private fun TimelineItem.map(): MatrixTimelineItem {
        return timelineItemMapper.map(this)
    }
}

private class DiffingResult(initialItems: List<MatrixTimelineItem>) {
    private val items = initialItems.toMutableList()
    var hasNewEventsFromSync: Boolean = false
        private set
    var hasMembershipChangeEventFromSync: Boolean = false
        private set

    fun items(): List<MatrixTimelineItem> = items

    fun add(item: MatrixTimelineItem) {
        processItem(item)
        items.add(item)
    }

    fun add(index: Int, item: MatrixTimelineItem) {
        processItem(item)
        items.add(index, item)
    }

    fun set(index: Int, item: MatrixTimelineItem) {
        processItem(item)
        items[index] = item
    }

    fun removeAt(index: Int) {
        items.removeAt(index)
    }

    fun removeFirst() {
        items.removeFirstOrNull()
    }

    fun removeLast() {
        items.removeLastOrNull()
    }

    fun truncate(length: Int) {
        items.subList(length, items.size).clear()
    }

    fun clear() {
        items.clear()
    }

    private fun processItem(item: MatrixTimelineItem) {
        if (skipProcessing()) return
        when (item) {
            is MatrixTimelineItem.Event -> {
                if (item.event.origin == TimelineItemEventOrigin.SYNC) {
                    hasNewEventsFromSync = true
                    when (item.event.content) {
                        is RoomMembershipContent -> hasMembershipChangeEventFromSync = true
                        else -> Unit
                    }
                }
            }
            else -> Unit
        }
    }

    private fun skipProcessing(): Boolean {
        return hasNewEventsFromSync && hasMembershipChangeEventFromSync
    }
}
