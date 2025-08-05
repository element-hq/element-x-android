/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline

import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.matrix.rustcomponents.sdk.TimelineDiff
import org.matrix.rustcomponents.sdk.TimelineItem
import timber.log.Timber

internal class MatrixTimelineDiffProcessor(
    private val timelineItems: MutableSharedFlow<List<MatrixTimelineItem>>,
    private val timelineItemFactory: MatrixTimelineItemMapper,
) {
    private val mutex = Mutex()

    private val _membershipChangeEventReceived = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val membershipChangeEventReceived: Flow<Unit> = _membershipChangeEventReceived

    suspend fun postDiffs(diffs: List<TimelineDiff>) {
        updateTimelineItems {
            Timber.v("Update timeline items from postDiffs (with ${diffs.size} items) on ${Thread.currentThread()}")
            diffs.forEach { diff ->
                applyDiff(diff)
            }
        }
    }

    private suspend fun updateTimelineItems(block: MutableList<MatrixTimelineItem>.() -> Unit) =
        mutex.withLock {
            val mutableTimelineItems = if (timelineItems.replayCache.isNotEmpty()) {
                timelineItems.first().toMutableList()
            } else {
                mutableListOf()
            }
            block(mutableTimelineItems)
            timelineItems.tryEmit(mutableTimelineItems)
        }

    private fun MutableList<MatrixTimelineItem>.applyDiff(diff: TimelineDiff) {
        when (diff) {
            is TimelineDiff.Append -> {
                val items = diff.values.map { it.asMatrixTimelineItem() }
                addAll(items)
            }
            is TimelineDiff.PushBack -> {
                val item = diff.value.asMatrixTimelineItem()
                if (item is MatrixTimelineItem.Event && item.event.content is RoomMembershipContent) {
                    // TODO - This is a temporary solution to notify the room screen about membership changes
                    // Ideally, this should be implemented by the Rust SDK
                    _membershipChangeEventReceived.tryEmit(Unit)
                }
                add(item)
            }
            is TimelineDiff.PushFront -> {
                val item = diff.value.asMatrixTimelineItem()
                add(0, item)
            }
            is TimelineDiff.Set -> {
                val item = diff.value.asMatrixTimelineItem()
                set(diff.index.toInt(), item)
            }
            is TimelineDiff.Insert -> {
                val item = diff.value.asMatrixTimelineItem()
                add(diff.index.toInt(), item)
            }
            is TimelineDiff.Remove -> {
                removeAt(diff.index.toInt())
            }
            is TimelineDiff.Reset -> {
                clear()
                val items = diff.values.map { it.asMatrixTimelineItem() }
                addAll(items)
            }
            TimelineDiff.PopFront -> {
                removeFirstOrNull()
            }
            TimelineDiff.PopBack -> {
                removeLastOrNull()
            }
            TimelineDiff.Clear -> {
                clear()
            }
            is TimelineDiff.Truncate -> {
                subList(diff.length.toInt(), size).clear()
            }
        }
    }

    private fun TimelineItem.asMatrixTimelineItem(): MatrixTimelineItem {
        return timelineItemFactory.map(this)
    }
}
