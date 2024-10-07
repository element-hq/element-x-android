/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.bubble

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.aTimelineRoomInfo
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition

open class BubbleStateProvider : PreviewParameterProvider<BubbleState> {
    override val values: Sequence<BubbleState>
        get() = sequenceOf(
            TimelineItemGroupPosition.First,
            TimelineItemGroupPosition.Middle,
            TimelineItemGroupPosition.Last,
            TimelineItemGroupPosition.None,
        ).map { groupPosition ->
            sequenceOf(false, true).map { isMine ->
                sequenceOf(false, true).map { isHighlighted ->
                    aBubbleState(
                        groupPosition = groupPosition,
                        isMine = isMine,
                        isHighlighted = isHighlighted,
                    )
                }
            }
                .flatten()
        }
            .flatten()
}

internal fun aBubbleState(
    groupPosition: TimelineItemGroupPosition = TimelineItemGroupPosition.First,
    isMine: Boolean = false,
    isHighlighted: Boolean = false,
    timelineRoomInfo: TimelineRoomInfo = aTimelineRoomInfo(),
) = BubbleState(
    groupPosition = groupPosition,
    isMine = isMine,
    isHighlighted = isHighlighted,
    timelineRoomInfo = timelineRoomInfo,
)
