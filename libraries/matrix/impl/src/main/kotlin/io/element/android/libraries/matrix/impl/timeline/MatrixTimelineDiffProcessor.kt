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
import org.matrix.rustcomponents.sdk.TimelineChange
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

    suspend fun postItems(items: List<TimelineItem>) {
        updateTimelineItems {
            Timber.v("Update timeline items from postItems (with ${items.size} items) on ${Thread.currentThread()}")
            val mappedItems = items.map { it.asMatrixTimelineItem() }
            addAll(0, mappedItems)
        }
    }

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
        when (diff.change()) {
            TimelineChange.APPEND -> {
                val items = diff.append()?.map { it.asMatrixTimelineItem() } ?: return
                addAll(items)
            }
            TimelineChange.PUSH_BACK -> {
                val item = diff.pushBack()?.asMatrixTimelineItem() ?: return
                if (item is MatrixTimelineItem.Event && item.event.content is RoomMembershipContent) {
                    // TODO - This is a temporary solution to notify the room screen about membership changes
                    // Ideally, this should be implemented by the Rust SDK
                    _membershipChangeEventReceived.tryEmit(Unit)
                }
                add(item)
            }
            TimelineChange.PUSH_FRONT -> {
                val item = diff.pushFront()?.asMatrixTimelineItem() ?: return
                add(0, item)
            }
            TimelineChange.SET -> {
                val updateAtData = diff.set() ?: return
                val item = updateAtData.item.asMatrixTimelineItem()
                set(updateAtData.index.toInt(), item)
            }
            TimelineChange.INSERT -> {
                val insertAtData = diff.insert() ?: return
                val item = insertAtData.item.asMatrixTimelineItem()
                add(insertAtData.index.toInt(), item)
            }
            TimelineChange.REMOVE -> {
                val removeAtData = diff.remove() ?: return
                removeAt(removeAtData.toInt())
            }
            TimelineChange.RESET -> {
                clear()
                val items = diff.reset()?.map { it.asMatrixTimelineItem() } ?: return
                addAll(items)
            }
            TimelineChange.POP_FRONT -> {
                removeFirstOrNull()
            }
            TimelineChange.POP_BACK -> {
                removeLastOrNull()
            }
            TimelineChange.CLEAR -> {
                clear()
            }
            TimelineChange.TRUNCATE -> {
                val index = diff.truncate() ?: return
                subList(index.toInt(), size).clear()
            }
        }
    }

    private fun TimelineItem.asMatrixTimelineItem(): MatrixTimelineItem {
        return timelineItemFactory.map(this)
    }
}
