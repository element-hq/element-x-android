/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.messages.impl.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.timeline.di.LocalTimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.di.aFakeTimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.features.messages.impl.typing.aTypingNotificationState
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import kotlinx.collections.immutable.persistentListOf

open class TimelineWithPinnedEventsPreviewDataProvider : PreviewParameterProvider<TimelineWithPinnedEventsPreviewData> {
    override val values: Sequence<TimelineWithPinnedEventsPreviewData>
        get() = sequenceOf(aTimelineItemTextContent(), aTimelineItemImageContent(aspectRatio = 3.0f)).flatMap { content ->
            sequenceOf(false, true).flatMap { isDm ->
                (0..3).map { focusedEventIndex ->
                    TimelineWithPinnedEventsPreviewData(
                        content = content,
                        isDm = isDm,
                        focusedEventIndex = focusedEventIndex,
                    )
                }
            }
        }
}

data class TimelineWithPinnedEventsPreviewData(
    val content: TimelineItemEventContent,
    val isDm: Boolean,
    val focusedEventIndex: Int,
)

@PreviewsDayNight
@Composable
internal fun TimelineWithPinnedEventsPreview(
    @PreviewParameter(TimelineWithPinnedEventsPreviewDataProvider::class) data: TimelineWithPinnedEventsPreviewData
) = ElementPreview {
    val timelineItems = persistentListOf(
        // 2 items (First, Last) with isMine = false
        aTimelineItemEvent(
            isMine = false,
            isPinned = true,
            content = data.content,
            groupPosition = TimelineItemGroupPosition.Last
        ),
        aTimelineItemEvent(
            isMine = false,
            isPinned = true,
            content = data.content,
            groupPosition = TimelineItemGroupPosition.First
        ),
        // 2 items (First, Last) with isMine = true
        aTimelineItemEvent(
            isMine = true,
            isPinned = true,
            content = data.content,
            groupPosition = TimelineItemGroupPosition.Last
        ),
        aTimelineItemEvent(
            isMine = true,
            isPinned = true,
            content = data.content,
            groupPosition = TimelineItemGroupPosition.First
        ),
    )

    CompositionLocalProvider(
        LocalTimelineItemPresenterFactories provides aFakeTimelineItemPresenterFactories(),
    ) {
        TimelineView(
            state = aTimelineState(
                timelineItems = timelineItems,
                focusedEventIndex = data.focusedEventIndex,
                timelineRoomInfo = aTimelineRoomInfo(isDm = data.isDm),
            ),
            typingNotificationState = aTypingNotificationState(),
            onUserDataClick = {},
            onLinkClick = {},
            onMessageClick = {},
            onMessageLongClick = {},
            onSwipeToReply = {},
            onReactionClick = { _, _ -> },
            onReactionLongClick = { _, _ -> },
            onMoreReactionsClick = {},
            onReadReceiptClick = {},
            onJoinCallClick = {},
        )
    }
}
