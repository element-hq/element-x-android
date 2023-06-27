/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.messages.impl.timeline.model.event

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import org.jsoup.Jsoup

class TimelineItemEventContentProvider : PreviewParameterProvider<TimelineItemEventContent> {
    override val values = sequenceOf(
        aTimelineItemEmoteContent(),
        aTimelineItemEncryptedContent(),
        aTimelineItemImageContent(),
        aTimelineItemVideoContent(),
        aTimelineItemFileContent("A file.pdf"),
        aTimelineItemFileContent("A bigger file name which doesn't fit.pdf"),
        aTimelineItemNoticeContent(),
        aTimelineItemRedactedContent(),
        aTimelineItemTextContent(),
        aTimelineItemUnknownContent(),
        aTimelineItemTextContent().copy(isEdited = true),
    )
}

class TimelineItemTextBasedContentProvider : PreviewParameterProvider<TimelineItemTextBasedContent> {
    override val values = sequenceOf(
        aTimelineItemEmoteContent(),
        aTimelineItemEmoteContent().copy(htmlDocument = Jsoup.parse("Emote Document")),
        aTimelineItemNoticeContent(),
        aTimelineItemNoticeContent().copy(htmlDocument = Jsoup.parse("Notice Document")),
        aTimelineItemTextContent(),
        aTimelineItemTextContent().copy(htmlDocument = Jsoup.parse("Text Document")),
    )
}

fun aTimelineItemEmoteContent() = TimelineItemEmoteContent(
    body = "Emote",
    htmlDocument = null,
    isEdited = false,
)

fun aTimelineItemEncryptedContent() = TimelineItemEncryptedContent(
    data = UnableToDecryptContent.Data.Unknown
)

fun aTimelineItemNoticeContent() = TimelineItemNoticeContent(
    body = "Notice",
    htmlDocument = null,
    isEdited = false,
)

fun aTimelineItemRedactedContent() = TimelineItemRedactedContent

fun aTimelineItemTextContent() = TimelineItemTextContent(
    body = "Text",
    htmlDocument = null,
    isEdited = false,
)

fun aTimelineItemUnknownContent() = TimelineItemUnknownContent

fun aTimelineItemStateEventContent() = TimelineItemStateEventContent(
    body = "A state event",
)
