/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.api.DateFormatterMode
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import javax.inject.Inject

class VirtualItemFactory @Inject constructor(
    private val dateFormatter: DateFormatter,
) {
    fun create(timelineItem: MatrixTimelineItem.Virtual): MediaItem? {
        return when (val virtual = timelineItem.virtual) {
            is VirtualTimelineItem.DayDivider -> MediaItem.DateSeparator(
                id = timelineItem.uniqueId,
                formattedDate = dateFormatter.format(
                    timestamp = virtual.timestamp,
                    mode = DateFormatterMode.Month,
                    useRelative = true,
                )
            )
            VirtualTimelineItem.LastForwardIndicator -> null
            is VirtualTimelineItem.LoadingIndicator -> MediaItem.LoadingIndicator(
                id = timelineItem.uniqueId,
                direction = virtual.direction,
                timestamp = virtual.timestamp
            )
            VirtualTimelineItem.ReadMarker -> null
            VirtualTimelineItem.RoomBeginning -> null
            VirtualTimelineItem.TypingNotification -> null
        }
    }
}
