/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.dateformatter.api.DaySeparatorFormatter
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import javax.inject.Inject

interface VirtualItemFactory {
    fun create(timelineItem: MatrixTimelineItem.Virtual): MediaItem?
}

@ContributesBinding(AppScope::class)
class DefaultVirtualItemFactory @Inject constructor(
    private val daySeparatorFormatter: DaySeparatorFormatter,
) : VirtualItemFactory {
    override fun create(timelineItem: MatrixTimelineItem.Virtual): MediaItem? {
        return when (val virtual = timelineItem.virtual) {
            is VirtualTimelineItem.DayDivider -> MediaItem.DateSeparator(
                id = timelineItem.uniqueId,
                formattedDate = daySeparatorFormatter.format(virtual.timestamp)
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
