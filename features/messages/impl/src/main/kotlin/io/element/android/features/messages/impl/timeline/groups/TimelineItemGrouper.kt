/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.groups

import androidx.annotation.VisibleForTesting
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.UniqueId
import kotlinx.collections.immutable.toImmutableList

@SingleIn(RoomScope::class)
@Inject
class TimelineItemGrouper {
    /**
     * Keys are identifier of items in a group, only one by group will be kept.
     * Values are the actual groupIds.
     */
    private val groupIds = HashMap<String, String>()

    /**
     * Create a new list of [TimelineItem] by grouping some of them into [TimelineItem.GroupedEvents].
     */
    fun group(from: List<TimelineItem>): List<TimelineItem> {
        val result = mutableListOf<TimelineItem>()
        val currentGroup = mutableListOf<TimelineItem.Event>()
        from.forEach { timelineItem ->
            if (timelineItem is TimelineItem.Event && timelineItem.canBeGrouped()) {
                currentGroup.add(0, timelineItem)
            } else {
                // timelineItem cannot be grouped
                if (currentGroup.isNotEmpty()) {
                    // There is a pending group, create a TimelineItem.GroupedEvents if there is more than 1 Event in the pending group.
                    result.addGroup(groupIds, currentGroup)
                    currentGroup.clear()
                }
                result.add(timelineItem)
            }
        }
        if (currentGroup.isNotEmpty()) {
            result.addGroup(groupIds, currentGroup)
        }
        // Finally, fold runs of consecutive deleted messages into a single group (element-web style).
        return result.collapseRedactedRuns()
    }
}

/**
 * Will add a group if there is more than 1 item, else add the item to the list.
 */
private fun MutableList<TimelineItem>.addGroup(
    groupIds: MutableMap<String, String>,
    groupOfItems: MutableList<TimelineItem.Event>
) {
    if (groupOfItems.size == 1) {
        // Do not create a group with just 1 item, just add the item to the result
        add(groupOfItems.first())
    } else {
        val groupId = groupIds.getOrPutGroupId(groupOfItems)
        add(
            TimelineItem.GroupedEvents(
                id = UniqueId(groupId),
                events = groupOfItems.toImmutableList(),
                aggregatedReadReceipts = groupOfItems.flatMap { it.readReceiptState.receipts }.toImmutableList()
            )
        )
    }
}

private fun MutableMap<String, String>.getOrPutGroupId(timelineItems: List<TimelineItem>): String {
    assert(timelineItems.isNotEmpty())
    for (item in timelineItems) {
        val itemIdentifier = item.identifier()
        if (this.contains(itemIdentifier.value)) {
            return this[itemIdentifier.value]!!
        }
    }
    val timelineItem = timelineItems.first()
    return computeGroupIdWith(timelineItem).value.also { groupId ->
        this[timelineItem.identifier().value] = groupId
    }
}

@VisibleForTesting
internal fun computeGroupIdWith(timelineItem: TimelineItem): UniqueId = UniqueId("${timelineItem.identifier()}_group")

// Runs shorter than this are left as individual "Message removed" tiles, like element-web.
internal const val MIN_REDACTED_RUN_SIZE = 3

/**
 * Fold runs of [MIN_REDACTED_RUN_SIZE] or more consecutive deleted (redacted) messages into a single
 * [TimelineItem.GroupedEvents], so a long stretch of "Message removed" tiles shows as one expandable
 * "N removed messages" line, like element-web. Shorter runs, and anything that is not a redacted event
 * (day dividers included), are left untouched.
 *
 * This runs as the final step of [TimelineItemGrouper.group], after the state and membership events
 * have been grouped. It is deliberately kept out of the normal grouping: a run needs a higher minimum
 * than an ordinary group, and a redacted group has to stay entirely redacted so its header can show a
 * plain count rather than being mixed in with the surrounding state changes.
 *
 * Events are kept oldest-first to match the other groups, and the group id is taken from the newest
 * event of the run, so it stays stable as older history loads in and the run grows at its older end.
 */
internal fun List<TimelineItem>.collapseRedactedRuns(): List<TimelineItem> {
    val result = mutableListOf<TimelineItem>()
    val run = mutableListOf<TimelineItem.Event>()

    fun flushRun() {
        when {
            run.isEmpty() -> Unit
            run.size < MIN_REDACTED_RUN_SIZE -> result.addAll(run)
            else -> result.add(
                TimelineItem.GroupedEvents(
                    id = computeGroupIdWith(run.first()),
                    events = run.reversed().toImmutableList(),
                    aggregatedReadReceipts = run.flatMap { it.readReceiptState.receipts }.toImmutableList(),
                )
            )
        }
        run.clear()
    }

    for (item in this) {
        if (item is TimelineItem.Event && item.content is TimelineItemRedactedContent) {
            run.add(item)
        } else {
            flushRun()
            result.add(item)
        }
    }
    flushRun()
    return result
}
