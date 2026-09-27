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
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import kotlinx.collections.immutable.toImmutableList
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
        aTimelineItemLocationContent(mode = aStaticLocationMode()),
        aTimelineItemPollContent(),
        aTimelineItemNoticeContent(),
        aTimelineItemRedactedContent(),
        aTimelineItemTextContent(),
        aTimelineItemUnknownContent(),
        aTimelineItemTextContent().copy(isEdited = true),
        aTimelineItemTextContent(body = AN_EMOJI_ONLY_TEXT),
        aTimelineItemLocationContent(
            mode = aLiveLocationMode(isActive = true, endsAt = "Ends at 12:34", endTimestamp = 0L, lastKnownLocation = null)
        ),
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

fun aTimelineItemAttachmentsContent(
    body: String = "Attachments",
    caption: String? = null,
    attachments: List<AttachmentItem> = listOf(
        anAttachmentItem(filename = "document.pdf", fileExtension = "pdf"),
        anAttachmentItem(filename = "recording.mp3", fileExtension = "mp3", fileSize = 4_500_000L, formattedFileSize = "4.5MB"),
    ),
) = TimelineItemAttachmentsContent(
    body = body,
    caption = caption,
    formattedCaption = null,
    isEdited = false,
    attachments = attachments.toImmutableList(),
)

fun anAttachmentItem(
    filename: String = "file.pdf",
    fileExtension: String = "pdf",
    fileSize: Long? = 1_000_000L,
    formattedFileSize: String = "1MB",
    mimeType: String? = null,
    thumbnailSource: MediaSource? = null,
    hasThumbnail: Boolean = false,
) = AttachmentItem(
    filename = filename,
    mimeType = mimeType ?: when {
        hasThumbnail -> "image/jpeg"
        else -> "application/$fileExtension"
    },
    mediaSource = MediaSource(url = "", json = ""),
    thumbnailSource = thumbnailSource ?: if (hasThumbnail) MediaSource(url = "", json = "") else null,
    fileSize = fileSize,
    formattedFileSize = formattedFileSize,
    fileExtension = fileExtension,
)
