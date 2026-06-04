/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.postprocessor

import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem

/**
 * Post-processor to filter out day separators for days that don't contain any events.
 */
class FilterEmptyDayPostProcessor {
    /**
     * Filters out day separators from [items] for days that don't contain any events.
     */
    fun process(items: List<MatrixTimelineItem>): List<MatrixTimelineItem> = buildList {
        // The timeline is ordered by ascending timestamp, so events that happened during a day appear after the day separator for that day.
        // We can use this to determine if a day separator should be kept or not by traversing the list in reverse.
        var hasEvent = false
        for (item in items.asReversed()) {
            if (item is MatrixTimelineItem.Event) {
                hasEvent = true
                add(item)
            } else if (item is MatrixTimelineItem.Virtual && item.virtual is VirtualTimelineItem.DayDivider) {
                if (hasEvent) {
                    add(item)
                    hasEvent = false
                }
            } else {
                add(item)
            }
        }
    }
        // Then reverse the result to restore the original order, minus the empty day separators.
        .asReversed()
}
