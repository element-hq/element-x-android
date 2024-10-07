/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories.virtual

import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemLastForwardIndicatorModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemLoadingIndicatorModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemReadMarkerModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemRoomBeginningModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemTypingNotificationModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemVirtualModel
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import javax.inject.Inject

class TimelineItemVirtualFactory @Inject constructor(
    private val daySeparatorFactory: TimelineItemDaySeparatorFactory,
) {
    fun create(
        virtualTimelineItem: MatrixTimelineItem.Virtual,
    ): TimelineItem.Virtual {
        return TimelineItem.Virtual(
            id = virtualTimelineItem.uniqueId,
            model = virtualTimelineItem.computeModel()
        )
    }

    private fun MatrixTimelineItem.Virtual.computeModel(): TimelineItemVirtualModel {
        return when (val inner = virtual) {
            is VirtualTimelineItem.DayDivider -> daySeparatorFactory.create(inner)
            is VirtualTimelineItem.ReadMarker -> TimelineItemReadMarkerModel
            is VirtualTimelineItem.RoomBeginning -> TimelineItemRoomBeginningModel
            is VirtualTimelineItem.LoadingIndicator -> TimelineItemLoadingIndicatorModel(
                direction = inner.direction,
                timestamp = inner.timestamp
            )
            is VirtualTimelineItem.LastForwardIndicator -> TimelineItemLastForwardIndicatorModel
            VirtualTimelineItem.TypingNotification -> TimelineItemTypingNotificationModel
        }
    }
}
