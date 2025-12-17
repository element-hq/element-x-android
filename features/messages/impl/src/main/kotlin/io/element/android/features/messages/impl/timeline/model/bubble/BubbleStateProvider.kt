/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
                aBubbleState(
                    groupPosition = groupPosition,
                    isMine = isMine,
                )
            }
        }
            .flatten()
}

internal fun aBubbleState(
    groupPosition: TimelineItemGroupPosition = TimelineItemGroupPosition.First,
    isMine: Boolean = false,
    timelineRoomInfo: TimelineRoomInfo = aTimelineRoomInfo(),
) = BubbleState(
    groupPosition = groupPosition,
    isMine = isMine,
    timelineRoomInfo = timelineRoomInfo,
)
