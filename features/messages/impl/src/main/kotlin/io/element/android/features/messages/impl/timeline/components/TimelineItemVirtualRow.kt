/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.components.virtual.TimelineItemDaySeparatorView
import io.element.android.features.messages.impl.timeline.components.virtual.TimelineItemReadMarkerView
import io.element.android.features.messages.impl.timeline.components.virtual.TimelineItemRoomBeginningView
import io.element.android.features.messages.impl.timeline.components.virtual.TimelineLoadingMoreIndicator
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemDaySeparatorModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemLastForwardIndicatorModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemLoadingIndicatorModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemReadMarkerModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemRoomBeginningModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemTypingNotificationModel
import io.element.android.features.messages.impl.typing.TypingNotificationView

@Composable
fun TimelineItemVirtualRow(
    virtual: TimelineItem.Virtual,
    timelineRoomInfo: TimelineRoomInfo,
    eventSink: (TimelineEvents.EventFromTimelineItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when (virtual.model) {
            is TimelineItemDaySeparatorModel -> TimelineItemDaySeparatorView(virtual.model)
            TimelineItemReadMarkerModel -> TimelineItemReadMarkerView()
            TimelineItemRoomBeginningModel -> TimelineItemRoomBeginningView(roomName = timelineRoomInfo.name)
            is TimelineItemLoadingIndicatorModel -> {
                TimelineLoadingMoreIndicator(virtual.model.direction)
                val latestEventSink by rememberUpdatedState(eventSink)
                LaunchedEffect(virtual.model.timestamp) {
                    latestEventSink(TimelineEvents.LoadMore(virtual.model.direction))
                }
            }
            // Empty model trick to avoid timeline jumping during forward pagination.
            is TimelineItemLastForwardIndicatorModel -> {
                Spacer(modifier = Modifier)
            }
            is TimelineItemTypingNotificationModel -> {
                TypingNotificationView(
                    state = timelineRoomInfo.typingNotificationState,
                )
            }
        }
    }
}
