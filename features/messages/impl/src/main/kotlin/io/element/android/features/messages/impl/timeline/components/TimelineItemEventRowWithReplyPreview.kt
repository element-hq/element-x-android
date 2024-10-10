/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.aTimelineItemReactions
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetailsProvider

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowWithReplyPreview(
    @PreviewParameter(InReplyToDetailsProvider::class) inReplyToDetails: InReplyToDetails,
) = ElementPreview {
    TimelineItemEventRowWithReplyContentToPreview(inReplyToDetails)
}

@Composable
internal fun TimelineItemEventRowWithReplyContentToPreview(
    inReplyToDetails: InReplyToDetails,
    displayNameAmbiguous: Boolean = false,
) {
    Column {
        sequenceOf(false, true).forEach {
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = it,
                    timelineItemReactions = aTimelineItemReactions(count = 0),
                    content = aTimelineItemTextContent(body = "A reply."),
                    inReplyTo = inReplyToDetails,
                    displayNameAmbiguous = displayNameAmbiguous,
                    groupPosition = TimelineItemGroupPosition.First,
                ),
            )
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = it,
                    timelineItemReactions = aTimelineItemReactions(count = 0),
                    content = aTimelineItemImageContent(
                        aspectRatio = 2.5f
                    ),
                    inReplyTo = inReplyToDetails,
                    displayNameAmbiguous = displayNameAmbiguous,
                    isThreaded = true,
                    groupPosition = TimelineItemGroupPosition.Last,
                ),
            )
        }
    }
}
