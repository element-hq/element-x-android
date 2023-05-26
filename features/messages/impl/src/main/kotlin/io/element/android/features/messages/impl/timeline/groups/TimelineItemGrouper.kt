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

package io.element.android.features.messages.impl.timeline.groups

import io.element.android.features.messages.impl.timeline.model.TimelineItem
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class TimelineItemGrouper @Inject constructor() {
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
                    result.addGroup(currentGroup)
                    currentGroup.clear()
                }
                result.add(timelineItem)
            }
        }
        if (currentGroup.isNotEmpty()) {
            result.addGroup(currentGroup)
        }
        return result
    }
}

/**
 * Will add a group if there is more than 1 item, else add the item to the list.
 */
private fun MutableList<TimelineItem>.addGroup(
    group: MutableList<TimelineItem.Event>
) {
    if (group.size == 1) {
        // Do not create a group with just 1 item, just add the item to the result
        add(group.first())
    } else {
        add(
            TimelineItem.GroupedEvents(
                events = group.toImmutableList()
            )
        )
    }
}
