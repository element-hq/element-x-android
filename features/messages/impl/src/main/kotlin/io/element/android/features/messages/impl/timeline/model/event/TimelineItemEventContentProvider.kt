/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import android.graphics.Typeface
import android.text.style.StyleSpan
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import org.jsoup.nodes.Document

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

fun aTimelineItemEmoteContent(
    body: String = "Emote",
    htmlDocument: Document? = null,
    formattedBody: CharSequence = body,
    isEdited: Boolean = false,
) = TimelineItemEmoteContent(
    body = body,
    htmlDocument = htmlDocument,
    formattedBody = formattedBody,
    isEdited = isEdited,
)

fun aTimelineItemEncryptedContent() = TimelineItemEncryptedContent(
    data = UnableToDecryptContent.Data.Unknown
)

fun aTimelineItemNoticeContent(
    body: String = "Notice",
    htmlDocument: Document? = null,
    formattedBody: CharSequence = body,
    isEdited: Boolean = false,
) = TimelineItemNoticeContent(
    body = body,
    htmlDocument = htmlDocument,
    formattedBody = formattedBody,
    isEdited = isEdited,
)

fun aTimelineItemRedactedContent() = TimelineItemRedactedContent

fun aTimelineItemTextContent(
    body: String = "Text",
    htmlDocument: Document? = null,
    formattedBody: CharSequence = body,
    isEdited: Boolean = false,
) = TimelineItemTextContent(
    body = body,
    htmlDocument = htmlDocument,
    formattedBody = formattedBody,
    isEdited = isEdited,
)

fun aTimelineItemUnknownContent() = TimelineItemUnknownContent

fun aTimelineItemStateEventContent(
    body: String = "A state event",
) = TimelineItemStateEventContent(
    body = body,
)
