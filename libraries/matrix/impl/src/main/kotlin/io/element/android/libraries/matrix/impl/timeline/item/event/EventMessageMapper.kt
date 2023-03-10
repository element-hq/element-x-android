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

package io.element.android.libraries.matrix.impl.timeline.item.event

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.TimelineEventMessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnknownMessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageContent
import io.element.android.libraries.matrix.impl.media.map
import io.element.android.libraries.matrix.impl.media.useUrl
import org.matrix.rustcomponents.sdk.Message
import org.matrix.rustcomponents.sdk.MessageType
import org.matrix.rustcomponents.sdk.use
import org.matrix.rustcomponents.sdk.FormattedBody as RustFormattedBody
import org.matrix.rustcomponents.sdk.MessageFormat as RustMessageFormat

class EventMessageMapper {

    fun map(message: Message): TimelineEventMessageContent = message.use {
        val content = message.msgtype().use { type ->
            when (type) {
                is MessageType.Audio -> {
                    AudioMessageContent(type.content.body, type.content.source.useUrl(), type.content.info?.map())
                }
                is MessageType.File -> {
                    FileMessageContent(type.content.body, type.content.source.useUrl(), type.content.info?.map())
                }
                is MessageType.Image -> {
                    ImageMessageContent(type.content.body, type.content.source.useUrl(), type.content.info?.map())
                }
                is MessageType.Notice -> {
                    NoticeMessageContent(type.content.body, type.content.formatted?.map())
                }
                is MessageType.Text -> {
                    TextMessageContent(type.content.body, type.content.formatted?.map())
                }
                is MessageType.Emote -> {
                    EmoteMessageContent(type.content.body, type.content.formatted?.map())
                }
                is MessageType.Video -> {
                    VideoMessageContent(type.content.body, type.content.source.useUrl(), type.content.info?.map())
                }
                null -> {
                    UnknownMessageContent
                }
            }
        }
        TimelineEventMessageContent(
            body = message.body(),
            inReplyTo = message.inReplyTo()?.let { UserId(it) },
            isEdited = message.isEdited(),
            content = content
        )
    }
}

private fun RustFormattedBody.map(): FormattedBody = FormattedBody(
    format = format.map(),
    body = body
)

private fun RustMessageFormat.map(): MessageFormat {
    return when (this) {
        RustMessageFormat.HTML -> MessageFormat.HTML
        RustMessageFormat.UNKNOWN -> MessageFormat.UNKNOWN
    }
}
