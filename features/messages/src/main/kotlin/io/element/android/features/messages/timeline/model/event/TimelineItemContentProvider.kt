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

package io.element.android.features.messages.timeline.model.event

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import org.jsoup.Jsoup
import org.matrix.rustcomponents.sdk.EncryptedMessage

class TimelineItemContentProvider : PreviewParameterProvider<TimelineItemEventContent> {
    override val values = sequenceOf(
        aTimelineItemEmoteContent(),
        aTimelineItemEncryptedContent(),
        // TODO MessagesTimelineItemImageContent(),
        aTimelineItemNoticeContent(),
        aTimelineItemRedactedContent(),
        aTimelineItemTextContent(),
        aTimelineItemUnknownContent(),
    )
}

class TimelineItemTextBasedContentProvider : PreviewParameterProvider<TimelineItemTextBasedContent> {
    override val values = sequenceOf(
        aTimelineItemEmoteContent(),
        aTimelineItemEmoteContent().copy(htmlDocument = Jsoup.parse("Emote")),
        aTimelineItemNoticeContent(),
        aTimelineItemNoticeContent().copy(htmlDocument = Jsoup.parse("Notice")),
        aTimelineItemTextContent(),
        aTimelineItemTextContent().copy(htmlDocument = Jsoup.parse("Text")),
    )
}

fun aTimelineItemEmoteContent() = TimelineItemEmoteContent(
    body = "Emote",
    htmlDocument = null
)

fun aTimelineItemEncryptedContent() = TimelineItemEncryptedContent(
    encryptedMessage = EncryptedMessage.Unknown
)

fun aTimelineItemNoticeContent() = TimelineItemNoticeContent(
    body = "Notice",
    htmlDocument = null
)

fun aTimelineItemRedactedContent() = TimelineItemRedactedContent

fun aTimelineItemTextContent() = TimelineItemTextContent(
    body = "Text",
    htmlDocument = null
)

fun aTimelineItemUnknownContent() = TimelineItemUnknownContent
