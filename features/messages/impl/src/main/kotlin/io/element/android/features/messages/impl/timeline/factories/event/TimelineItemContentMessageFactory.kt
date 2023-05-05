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

package io.element.android.features.messages.impl.timeline.factories.event

import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEmoteContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemNoticeContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.util.toHtmlDocument
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import javax.inject.Inject

class TimelineItemContentMessageFactory @Inject constructor() {

    fun create(content: MessageContent): TimelineItemEventContent {
        return when (val messageType = content.type) {
            is EmoteMessageType -> TimelineItemEmoteContent(
                body = messageType.body,
                htmlDocument = messageType.formatted?.toHtmlDocument()
            )
            is ImageMessageType -> {
                val height = messageType.info?.height?.toFloat()
                val width = messageType.info?.width?.toFloat()
                val aspectRatio = if (height != null && width != null) {
                    width / height
                } else {
                    0.7f
                }
                TimelineItemImageContent(
                    body = messageType.body,
                    height = messageType.info?.height?.toInt(),
                    width = messageType.info?.width?.toInt(),
                    mediaSource = messageType.source,
                    blurhash = messageType.info?.blurhash,
                    aspectRatio = aspectRatio
                )
            }
            is VideoMessageType -> {
                val height = messageType.info?.height?.toFloat()
                val width = messageType.info?.width?.toFloat()
                val aspectRatio = if (height != null && width != null) {
                    width / height
                } else {
                    0.7f
                }
                TimelineItemVideoContent(
                    body = messageType.body,
                    thumbnailSource = messageType.info?.thumbnailSource,
                    videoSource = messageType.source,
                    mimetype = messageType.info?.mimetype,
                    width = messageType.info?.width?.toInt(),
                    height = messageType.info?.height?.toInt(),
                    duration = messageType.info?.duration ?: 0L,
                    blurhash = messageType.info?.blurhash,
                    aspectRatio = aspectRatio
                )
            }
            is NoticeMessageType -> TimelineItemNoticeContent(
                body = messageType.body,
                htmlDocument = messageType.formatted?.toHtmlDocument()
            )
            is TextMessageType -> TimelineItemTextContent(
                body = messageType.body,
                htmlDocument = messageType.formatted?.toHtmlDocument()
            )
            else -> TimelineItemUnknownContent
        }
    }
}
