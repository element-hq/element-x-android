/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import androidx.compose.runtime.Immutable
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Rows rendered by the timeline [androidx.compose.foundation.lazy.LazyColumn].
 *
 * Read receipts / send state are separate rows so that event items keep a stable height when
 * receipts move to a newer message. That avoids the `reverseLayout` + `animateItem()` jump that
 * happens when an item shrinks in the same frame another is inserted.
 */
@Immutable
internal sealed interface TimelineLazyRow {
    val key: Any
    val contentType: String

    data class Item(
        val timelineItem: TimelineItem,
    ) : TimelineLazyRow {
        override val key: Any get() = timelineItem.identifier()
        override val contentType: String get() = timelineItem.contentType()
    }

    data class ReadReceipt(
        val event: TimelineItem.Event,
        val isLastOutgoingMessage: Boolean,
    ) : TimelineLazyRow {
        override val key: Any get() = "rr_${event.identifier()}"
        override val contentType: String get() = "read_receipt"
    }
}

/**
 * Flattened timeline rows plus index maps between lazy indices and [TimelineState.timelineItems].
 *
 * Built incrementally by [TimelineLazyRowsHolder] so unchanged [TimelineItem] references keep the
 * same [TimelineLazyRow] instances across timeline updates.
 */
@Immutable
internal data class TimelineLazyRows(
    val rows: ImmutableList<TimelineLazyRow>,
    val timelineIndexByLazyIndex: IntArray,
    val itemLazyIndexByTimelineIndex: IntArray,
) {
    val size: Int get() = rows.size

    operator fun get(index: Int): TimelineLazyRow = rows[index]

    fun timelineIndexAt(lazyIndex: Int): Int =
        timelineIndexByLazyIndex.getOrElse(lazyIndex) { lazyIndex }

    fun lazyIndexForTimelineIndex(timelineIndex: Int): Int =
        itemLazyIndexByTimelineIndex.getOrElse(timelineIndex) { timelineIndex }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TimelineLazyRows) return false
        return rows == other.rows &&
            timelineIndexByLazyIndex.contentEquals(other.timelineIndexByLazyIndex) &&
            itemLazyIndexByTimelineIndex.contentEquals(other.itemLazyIndexByTimelineIndex)
    }

    override fun hashCode(): Int {
        var result = rows.hashCode()
        result = 31 * result + timelineIndexByLazyIndex.contentHashCode()
        result = 31 * result + itemLazyIndexByTimelineIndex.contentHashCode()
        return result
    }

    companion object {
        val Empty = TimelineLazyRows(
            rows = persistentListOf(),
            timelineIndexByLazyIndex = intArrayOf(),
            itemLazyIndexByTimelineIndex = intArrayOf(),
        )
    }
}

/**
 * Caches flattened lazy rows and only rebuilds when [timelineItems] or the last-outgoing id change.
 * Reuses prior [TimelineLazyRow] instances when the underlying [TimelineItem] reference is unchanged.
 */
internal class TimelineLazyRowsHolder {
    private var timelineItems: ImmutableList<TimelineItem>? = null
    private var lastOutgoingEventId: UniqueId? = null
    private var cached: TimelineLazyRows = TimelineLazyRows.Empty

    fun getOrUpdate(
        timelineItems: ImmutableList<TimelineItem>,
        lastOutgoingEventId: UniqueId?,
    ): TimelineLazyRows {
        if (this.timelineItems === timelineItems && this.lastOutgoingEventId == lastOutgoingEventId) {
            return cached
        }
        cached = flattenTimelineItemsForLazyColumn(
            timelineItems = timelineItems,
            lastOutgoingEventId = lastOutgoingEventId,
            previous = cached,
        )
        this.timelineItems = timelineItems
        this.lastOutgoingEventId = lastOutgoingEventId
        return cached
    }
}

/**
 * Builds lazy rows from timeline items. For each event that shows a receipt/send-state row, that row
 * is emitted *before* the event so that with `reverseLayout` it appears below the bubble on screen.
 */
internal fun flattenTimelineItemsForLazyColumn(
    timelineItems: ImmutableList<TimelineItem>,
    lastOutgoingEventId: UniqueId?,
    previous: TimelineLazyRows = TimelineLazyRows.Empty,
): TimelineLazyRows {
    val previousItems = HashMap<UniqueId, TimelineLazyRow.Item>()
    val previousReceipts = HashMap<UniqueId, TimelineLazyRow.ReadReceipt>()
    for (row in previous.rows) {
        when (row) {
            is TimelineLazyRow.Item -> previousItems[row.timelineItem.identifier()] = row
            is TimelineLazyRow.ReadReceipt -> previousReceipts[row.event.identifier()] = row
        }
    }

    val rows = ArrayList<TimelineLazyRow>(timelineItems.size)
    val timelineIndices = ArrayList<Int>(timelineItems.size)
    val itemLazyIndexByTimelineIndex = IntArray(timelineItems.size)

    timelineItems.forEachIndexed { timelineIndex, timelineItem ->
        when (timelineItem) {
            is TimelineItem.Event -> {
                val isLastOutgoingMessage = timelineItem.id == lastOutgoingEventId
                if (shouldShowReadReceiptRow(timelineItem, isLastOutgoingMessage)) {
                    val cachedReceipt = previousReceipts[timelineItem.id]
                    val receiptRow = if (
                        cachedReceipt != null &&
                        cachedReceipt.event === timelineItem &&
                        cachedReceipt.isLastOutgoingMessage == isLastOutgoingMessage
                    ) {
                        cachedReceipt
                    } else {
                        TimelineLazyRow.ReadReceipt(
                            event = timelineItem,
                            isLastOutgoingMessage = isLastOutgoingMessage,
                        )
                    }
                    timelineIndices.add(timelineIndex)
                    rows.add(receiptRow)
                }
                val cachedItem = previousItems[timelineItem.id]
                val itemRow = if (cachedItem != null && cachedItem.timelineItem === timelineItem) {
                    cachedItem
                } else {
                    TimelineLazyRow.Item(timelineItem)
                }
                itemLazyIndexByTimelineIndex[timelineIndex] = rows.size
                timelineIndices.add(timelineIndex)
                rows.add(itemRow)
            }
            else -> {
                val id = timelineItem.identifier()
                val cachedItem = previousItems[id]
                val itemRow = if (cachedItem != null && cachedItem.timelineItem === timelineItem) {
                    cachedItem
                } else {
                    TimelineLazyRow.Item(timelineItem)
                }
                itemLazyIndexByTimelineIndex[timelineIndex] = rows.size
                timelineIndices.add(timelineIndex)
                rows.add(itemRow)
            }
        }
    }

    return TimelineLazyRows(
        rows = rows.toImmutableList(),
        timelineIndexByLazyIndex = timelineIndices.toIntArray(),
        itemLazyIndexByTimelineIndex = itemLazyIndexByTimelineIndex,
    )
}

/** Whether a dedicated lazy row should be shown for this event's receipts / send state. */
internal fun shouldShowReadReceiptRow(
    event: TimelineItem.Event,
    isLastOutgoingMessage: Boolean,
): Boolean {
    if (event.readReceiptState.receipts.isNotEmpty()) return true
    return when (event.localSendState) {
        is LocalEventSendState.Sending -> true
        is LocalEventSendState.Failed -> false
        null, is LocalEventSendState.Sent -> isLastOutgoingMessage
    }
}
