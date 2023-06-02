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
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemNoticeContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.util.FileSizeFormatter
import io.element.android.features.messages.impl.timeline.util.toHtmlDocument
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import javax.inject.Inject

class TimelineItemContentMessageFactory @Inject constructor(
    private val fileSizeFormatter: FileSizeFormatter
) {

    fun create(content: MessageContent): TimelineItemEventContent {
        return when (val messageType = content.type) {
            is EmoteMessageType -> TimelineItemEmoteContent(
                body = messageType.body,
                htmlDocument = messageType.formatted?.toHtmlDocument(),
                isEdited = content.isEdited,
            )
            is ImageMessageType -> {
                val aspectRatio = aspectRatioOf(messageType.info?.width, messageType.info?.height)
                TimelineItemImageContent(
                    body = messageType.body,
                    mediaSource = messageType.source,
                    mimeType = messageType.info?.mimetype,
                    blurhash = messageType.info?.blurhash,
                    width = messageType.info?.width?.toInt(),
                    height = messageType.info?.height?.toInt(),
                    aspectRatio = aspectRatio
                )
            }
            is VideoMessageType -> {
                val aspectRatio = aspectRatioOf(messageType.info?.width, messageType.info?.height)
                TimelineItemVideoContent(
                    body = messageType.body,
                    thumbnailSource = messageType.info?.thumbnailSource,
                    videoSource = messageType.source,
                    mimeType = messageType.info?.mimetype,
                    width = messageType.info?.width?.toInt(),
                    height = messageType.info?.height?.toInt(),
                    duration = messageType.info?.duration ?: 0L,
                    blurHash = messageType.info?.blurhash,
                    aspectRatio = aspectRatio
                )
            }
            is FileMessageType -> TimelineItemFileContent(
                body = messageType.body,
                thumbnailSource = messageType.info?.thumbnailSource,
                fileSource = messageType.source,
                mimeType = messageType.info?.mimetype,
                formattedFileSize = messageType.info?.size?.let {
                    fileSizeFormatter.format(it)
                },
            )
            is NoticeMessageType -> TimelineItemNoticeContent(
                body = messageType.body,
                htmlDocument = messageType.formatted?.toHtmlDocument(),
                isEdited = content.isEdited,
            )
            is TextMessageType -> TimelineItemTextContent(
                body = messageType.body,
                htmlDocument = messageType.formatted?.toHtmlDocument(),
                isEdited = content.isEdited,
            )
            else -> TimelineItemUnknownContent
        }
    }

    private fun aspectRatioOf(width: Long?, height: Long?): Float {
        return if (height != null && width != null) {
            width.toFloat() / height.toFloat()
        } else {
            0.7f
        }
    }
}
