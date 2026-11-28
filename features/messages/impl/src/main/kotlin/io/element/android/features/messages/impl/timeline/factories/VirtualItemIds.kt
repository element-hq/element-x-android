/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories

import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemDaySeparatorModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemLoadingIndicatorModel
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.Timeline

/**
 * Gives the day separators and loading indicators an id based on what they separate, since the timeline uses the ids as item keys, and animates an
 * item whose key is kept as an item moving to its new position.
 *
 * - The SDK keeps the id of a day separator when it changes its date, i.e. when older events are paginated, the separator of the oldest day moves to
 * the top and displays the date of the new oldest day, while a new separator is created for the previous day. The SDK also keeps the separator of
 * the oldest day at the top when older events of the same day are paginated. Keying them by their date and the event below them, i.e. the oldest
 * loaded event of their day, keeps a separator in place while it stays above the same event, and replaces it by a new one otherwise. An item moving
 * out of the screen would be drawn on top of the other items until the end of its animation.
 * - The loading indicators always have the same id, so they would move with the edge of the timeline. Keying them by the event they are next to
 * replaces them by a new one after each pagination.
 *
 * [this] must be ordered from the newest item to the oldest one.
 */
internal fun List<TimelineItem>.withVirtualItemIdsBasedOnContent(): List<TimelineItem> {
    val newestEventId = firstOrNull { it !is TimelineItem.Virtual }?.identifier()
    val oldestEventId = lastOrNull { it !is TimelineItem.Virtual }?.identifier()
    val newIds = HashSet<UniqueId>()
    // The id of the event below the current item, i.e. the newest event before it in the list
    var eventBelowId: UniqueId? = null
    return map { item ->
        if (item !is TimelineItem.Virtual) {
            eventBelowId = item.identifier()
            return@map item
        }
        val newId = when (val model = item.model) {
            is TimelineItemDaySeparatorModel -> UniqueId(listOfNotNull("day_separator", model.timestamp, eventBelowId).joinToString("_"))
            is TimelineItemLoadingIndicatorModel -> when (model.direction) {
                Timeline.PaginationDirection.BACKWARDS -> oldestEventId
                Timeline.PaginationDirection.FORWARDS -> newestEventId
            }?.let { eventId -> UniqueId("${item.id}_$eventId") }
            else -> null
        }
        // Keys must be unique: keep the original id of an item whose new id is already used
        if (newId != null && newIds.add(newId)) item.copy(id = newId) else item
    }
}
