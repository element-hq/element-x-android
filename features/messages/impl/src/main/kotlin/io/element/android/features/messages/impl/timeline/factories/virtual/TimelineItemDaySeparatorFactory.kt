/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories.virtual

import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemDaySeparatorModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemVirtualModel
import io.element.android.libraries.dateformatter.api.DaySeparatorFormatter
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import javax.inject.Inject

class TimelineItemDaySeparatorFactory @Inject constructor(private val daySeparatorFormatter: DaySeparatorFormatter) {
    fun create(virtualItem: VirtualTimelineItem.DayDivider): TimelineItemVirtualModel {
        val formattedDate = daySeparatorFormatter.format(virtualItem.timestamp)
        return TimelineItemDaySeparatorModel(
            formattedDate = formattedDate
        )
    }
}
