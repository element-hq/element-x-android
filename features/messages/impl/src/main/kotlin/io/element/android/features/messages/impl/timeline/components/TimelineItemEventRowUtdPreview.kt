/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.aTimelineItemReactions
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEncryptedContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import io.element.android.libraries.matrix.api.timeline.item.event.UtdCause

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowUtdPreview() = ElementPreview {
    Column {
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                senderDisplayName = "Alice",
                isMine = false,
                content = TimelineItemEncryptedContent(
                    data = UnableToDecryptContent.Data.MegolmV1AesSha2(
                        sessionId = "sessionId",
                        utdCause = UtdCause.UnsignedDevice,
                    )
                ),
                timelineItemReactions = aTimelineItemReactions(count = 0),
                groupPosition = TimelineItemGroupPosition.First,
            ),
        )
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                senderDisplayName = "Bob",
                isMine = false,
                content = TimelineItemEncryptedContent(
                    data = UnableToDecryptContent.Data.MegolmV1AesSha2(
                        sessionId = "sessionId",
                        utdCause = UtdCause.VerificationViolation,
                    )
                ),
                groupPosition = TimelineItemGroupPosition.First,
                timelineItemReactions = aTimelineItemReactions(count = 0)
            ),
        )

        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                senderDisplayName = "Bob",
                isMine = false,
                content = TimelineItemEncryptedContent(
                    data = UnableToDecryptContent.Data.MegolmV1AesSha2(
                        sessionId = "sessionId",
                        utdCause = UtdCause.SentBeforeWeJoined,
                    )
                ),
                groupPosition = TimelineItemGroupPosition.Last,
                timelineItemReactions = aTimelineItemReactions(count = 0)
            ),
        )
    }
}
