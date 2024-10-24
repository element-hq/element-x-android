/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import io.element.android.libraries.matrix.api.timeline.item.event.UtdCause

open class TimelineItemEncryptedContentProvider : PreviewParameterProvider<TimelineItemEncryptedContent> {
    override val values: Sequence<TimelineItemEncryptedContent>
        get() = sequenceOf(
            aTimelineItemEncryptedContent(),
            aTimelineItemEncryptedContent(
                data = UnableToDecryptContent.Data.MegolmV1AesSha2(
                    sessionId = "sessionId",
                    utdCause = UtdCause.SentBeforeWeJoined,
                )
            ),
            aTimelineItemEncryptedContent(
                data = UnableToDecryptContent.Data.MegolmV1AesSha2(
                    sessionId = "sessionId",
                    utdCause = UtdCause.VerificationViolation,
                )
            ),
            aTimelineItemEncryptedContent(
                data = UnableToDecryptContent.Data.MegolmV1AesSha2(
                    sessionId = "sessionId",
                    utdCause = UtdCause.UnsignedDevice,
                )
            ),
            aTimelineItemEncryptedContent(
                data = UnableToDecryptContent.Data.MegolmV1AesSha2(
                    sessionId = "sessionId",
                    utdCause = UtdCause.Unknown,
                )
            ),
        )
}

private fun aTimelineItemEncryptedContent(
    data: UnableToDecryptContent.Data = UnableToDecryptContent.Data.Unknown
) = TimelineItemEncryptedContent(
    data = data
)
