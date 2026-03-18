/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.aTimelineRoomInfo
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.preferences.api.store.TimelineLayoutMode

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowModernPreview() = ElementPreview {
    val modernRoomInfo = aTimelineRoomInfo(timelineLayoutMode = TimelineLayoutMode.Modern)
    Column {
        // Text message, first in group (shows avatar + sender name)
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                senderDisplayName = "Alice",
                isMine = false,
                content = aTimelineItemTextContent(body = "Hello from modern layout!"),
                groupPosition = TimelineItemGroupPosition.First,
            ),
            timelineRoomInfo = modernRoomInfo,
        )
        // Text message, middle in group (no avatar/name)
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                isMine = false,
                content = aTimelineItemTextContent(body = "This is a follow-up message."),
                groupPosition = TimelineItemGroupPosition.Middle,
            ),
            timelineRoomInfo = modernRoomInfo,
        )
        // Text message, last in group
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                isMine = false,
                content = aTimelineItemTextContent(body = "Last message in the group."),
                groupPosition = TimelineItemGroupPosition.Last,
            ),
            timelineRoomInfo = modernRoomInfo,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowModernMinePreview() = ElementPreview {
    val modernRoomInfo = aTimelineRoomInfo(timelineLayoutMode = TimelineLayoutMode.Modern)
    Column {
        // Own message (still left-aligned in modern mode)
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                isMine = true,
                content = aTimelineItemTextContent(body = "My own message in modern layout, left aligned."),
                groupPosition = TimelineItemGroupPosition.First,
            ),
            timelineRoomInfo = modernRoomInfo,
        )
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                isMine = true,
                content = aTimelineItemTextContent(body = "Another one from me."),
                groupPosition = TimelineItemGroupPosition.Last,
            ),
            timelineRoomInfo = modernRoomInfo,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowModernDmPreview() = ElementPreview {
    val modernDmRoomInfo = aTimelineRoomInfo(
        isDm = true,
        timelineLayoutMode = TimelineLayoutMode.Modern,
    )
    Column {
        // DM mode: no avatar column
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                isMine = false,
                content = aTimelineItemTextContent(body = "A DM message in modern layout, no avatar column."),
                groupPosition = TimelineItemGroupPosition.First,
            ),
            timelineRoomInfo = modernDmRoomInfo,
        )
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                isMine = true,
                content = aTimelineItemTextContent(body = "My reply in the DM."),
                groupPosition = TimelineItemGroupPosition.Last,
            ),
            timelineRoomInfo = modernDmRoomInfo,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowModernImagePreview() = ElementPreview {
    val modernRoomInfo = aTimelineRoomInfo(timelineLayoutMode = TimelineLayoutMode.Modern)
    Column {
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                isMine = false,
                content = aTimelineItemImageContent(aspectRatio = 2.5f),
                groupPosition = TimelineItemGroupPosition.First,
            ),
            timelineRoomInfo = modernRoomInfo,
        )
    }
}
