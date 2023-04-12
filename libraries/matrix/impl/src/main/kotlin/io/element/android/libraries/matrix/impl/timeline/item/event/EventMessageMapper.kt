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

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.UnknownMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.matrix.impl.media.map
import io.element.android.libraries.matrix.impl.media.useUrl
import org.matrix.rustcomponents.sdk.Message
import org.matrix.rustcomponents.sdk.MessageType
import org.matrix.rustcomponents.sdk.use
import org.matrix.rustcomponents.sdk.FormattedBody as RustFormattedBody
import org.matrix.rustcomponents.sdk.MessageFormat as RustMessageFormat

class EventMessageMapper {

    fun map(message: Message): MessageContent = message.use {
        val type = message.msgtype().use { type ->
            when (type) {
                is MessageType.Audio -> {
                    AudioMessageType(type.content.body, type.content.source.useUrl(), type.content.info?.map())
                }
                is MessageType.File -> {
                    FileMessageType(type.content.body, type.content.source.useUrl(), type.content.info?.map())
                }
                is MessageType.Image -> {
                    ImageMessageType(type.content.body, type.content.source.useUrl(), type.content.info?.map())
                }
                is MessageType.Notice -> {
                    NoticeMessageType(type.content.body, type.content.formatted?.map())
                }
                is MessageType.Text -> {
                    TextMessageType(type.content.body, type.content.formatted?.map())
                }
                is MessageType.Emote -> {
                    EmoteMessageType(type.content.body, type.content.formatted?.map())
                }
                is MessageType.Video -> {
                    VideoMessageType(type.content.body, type.content.source.useUrl(), type.content.info?.map())
                }
                null -> {
                    UnknownMessageType
                }
            }
        }
        MessageContent(
            body = message.body(),
            inReplyTo = message.inReplyTo()?.eventId?.let { EventId(it) },
            isEdited = message.isEdited(),
            type = type
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
