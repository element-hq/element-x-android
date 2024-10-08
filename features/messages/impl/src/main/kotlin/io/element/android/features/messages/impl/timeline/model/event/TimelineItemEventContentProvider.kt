/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import android.graphics.Typeface
import android.text.style.StyleSpan
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent

class TimelineItemEventContentProvider : PreviewParameterProvider<TimelineItemEventContent> {
    override val values = sequenceOf(
        aTimelineItemEmoteContent(),
        aTimelineItemEncryptedContent(),
        aTimelineItemImageContent(),
        aTimelineItemVideoContent(),
        aTimelineItemFileContent(),
        aTimelineItemFileContent("A bigger file name which doesn't fit.pdf"),
        aTimelineItemAudioContent(),
        aTimelineItemAudioContent("An even bigger bigger bigger bigger bigger bigger bigger sound name which doesn't fit .mp3"),
        aTimelineItemVoiceContent(),
        aTimelineItemLocationContent(),
        aTimelineItemLocationContent("Location description"),
        aTimelineItemPollContent(),
        aTimelineItemNoticeContent(),
        aTimelineItemRedactedContent(),
        aTimelineItemTextContent(),
        aTimelineItemUnknownContent(),
        aTimelineItemTextContent().copy(isEdited = true),
        aTimelineItemTextContent(body = AN_EMOJI_ONLY_TEXT)
    )
}

const val AN_EMOJI_ONLY_TEXT = "😁"

class TimelineItemTextBasedContentProvider : PreviewParameterProvider<TimelineItemTextBasedContent> {
    private fun buildSpanned(text: String) = buildSpannedString {
        inSpans(StyleSpan(Typeface.BOLD)) {
            append("Rich Text")
        }
        append(" ")
        append(text)
    }

    override val values = sequenceOf(
        aTimelineItemEmoteContent(),
        aTimelineItemEmoteContent().copy(formattedBody = buildSpanned("Emote")),
        aTimelineItemNoticeContent(),
        aTimelineItemNoticeContent().copy(formattedBody = buildSpanned("Notice")),
        aTimelineItemTextContent(),
        aTimelineItemTextContent().copy(formattedBody = buildSpanned("Text")),
    )
}

fun aTimelineItemEmoteContent() = TimelineItemEmoteContent(
    body = "Emote",
    htmlDocument = null,
    formattedBody = null,
    isEdited = false,
)

fun aTimelineItemEncryptedContent() = TimelineItemEncryptedContent(
    data = UnableToDecryptContent.Data.Unknown
)

fun aTimelineItemNoticeContent() = TimelineItemNoticeContent(
    body = "Notice",
    htmlDocument = null,
    formattedBody = null,
    isEdited = false,
)

fun aTimelineItemRedactedContent() = TimelineItemRedactedContent

fun aTimelineItemTextContent(
    body: String = "Text",
    pillifiedBody: CharSequence = body,
) = TimelineItemTextContent(
    body = body,
    pillifiedBody = pillifiedBody,
    htmlDocument = null,
    formattedBody = null,
    isEdited = false,
)

fun aTimelineItemUnknownContent() = TimelineItemUnknownContent

fun aTimelineItemStateEventContent(
    body: String = "A state event",
) = TimelineItemStateEventContent(
    body = body,
)
