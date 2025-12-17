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
        return result
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
