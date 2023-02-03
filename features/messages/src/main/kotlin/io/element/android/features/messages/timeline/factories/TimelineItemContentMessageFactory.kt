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

package io.element.android.features.messages.timeline.factories

import io.element.android.features.messages.timeline.model.content.TimelineItemContent
import io.element.android.features.messages.timeline.model.content.TimelineItemEmoteContent
import io.element.android.features.messages.timeline.model.content.TimelineItemImageContent
import io.element.android.features.messages.timeline.model.content.TimelineItemNoticeContent
import io.element.android.features.messages.timeline.model.content.TimelineItemTextContent
import io.element.android.features.messages.timeline.model.content.TimelineItemUnknownContent
import io.element.android.features.messages.timeline.util.toHtmlDocument
import io.element.android.libraries.matrix.media.MediaResolver
import org.matrix.rustcomponents.sdk.Message
import org.matrix.rustcomponents.sdk.MessageType
import javax.inject.Inject

class TimelineItemContentMessageFactory @Inject constructor() {

    fun create(contentAsMessage: Message?): TimelineItemContent {
        return when (val messageType = contentAsMessage?.msgtype()) {
            is MessageType.Emote -> TimelineItemEmoteContent(
                body = messageType.content.body,
                htmlDocument = messageType.content.formatted?.toHtmlDocument()
            )
            is MessageType.Image -> {
                val height = messageType.content.info?.height?.toFloat()
                val width = messageType.content.info?.width?.toFloat()
                val aspectRatio = if (height != null && width != null) {
                    width / height
                } else {
                    0.7f
                }
                TimelineItemImageContent(
                    body = messageType.content.body,
                    imageMeta = MediaResolver.Meta(
                        source = messageType.content.source,
                        kind = MediaResolver.Kind.Content
                    ),
                    blurhash = messageType.content.info?.blurhash,
                    aspectRatio = aspectRatio
                )
            }
            is MessageType.Notice -> TimelineItemNoticeContent(
                body = messageType.content.body,
                htmlDocument = messageType.content.formatted?.toHtmlDocument()
            )
            is MessageType.Text -> TimelineItemTextContent(
                body = messageType.content.body,
                htmlDocument = messageType.content.formatted?.toHtmlDocument()
            )
            else -> TimelineItemUnknownContent
        }
    }
}
