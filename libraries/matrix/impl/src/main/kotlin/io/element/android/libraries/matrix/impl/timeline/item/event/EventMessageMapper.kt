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
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.InReplyTo
import io.element.android.libraries.matrix.api.timeline.item.event.LocationMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.OtherMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VoiceMessageType
import io.element.android.libraries.matrix.impl.media.map
import org.matrix.rustcomponents.sdk.Message
import org.matrix.rustcomponents.sdk.MessageType
import org.matrix.rustcomponents.sdk.RepliedToEventDetails
import org.matrix.rustcomponents.sdk.use
import org.matrix.rustcomponents.sdk.FormattedBody as RustFormattedBody
import org.matrix.rustcomponents.sdk.MessageFormat as RustMessageFormat
import org.matrix.rustcomponents.sdk.MessageType as RustMessageType

class EventMessageMapper {
    private val timelineEventContentMapper by lazy { TimelineEventContentMapper() }

    fun map(message: Message): MessageContent = message.use {
        val type = it.msgtype().use(this::mapMessageType)
        val inReplyToEvent: InReplyTo? = it.inReplyTo()?.use { details ->
            val inReplyToId = EventId(details.eventId)
            when (val event = details.event) {
                is RepliedToEventDetails.Ready -> {
                    InReplyTo.Ready(
                        eventId = inReplyToId,
                        content = timelineEventContentMapper.map(event.content),
                        senderId = UserId(event.sender),
                        senderProfile = event.senderProfile.map(),
                    )
                }
                is RepliedToEventDetails.Error -> InReplyTo.Error
                is RepliedToEventDetails.Pending -> InReplyTo.Pending
                is RepliedToEventDetails.Unavailable -> InReplyTo.NotLoaded(inReplyToId)
            }
        }
        MessageContent(
            body = it.body(),
            inReplyTo = inReplyToEvent,
            isEdited = it.isEdited(),
            isThreaded = it.isThreaded(),
            type = type
        )
    }

    fun mapMessageType(type: RustMessageType) = when (type) {
        is RustMessageType.Audio -> {
            when (type.content.voice) {
                null -> {
                    AudioMessageType(
                        body = type.content.body,
                        source = type.content.source.map(),
                        info = type.content.info?.map(),
                    )
                }
                else -> {
                    VoiceMessageType(
                        body = type.content.body,
                        source = type.content.source.map(),
                        info = type.content.info?.map(),
                        details = type.content.audio?.map(),
                    )
                }
            }
        }
        is RustMessageType.File -> {
            FileMessageType(type.content.body, type.content.source.map(), type.content.info?.map())
        }
        is RustMessageType.Image -> {
            ImageMessageType(type.content.body, type.content.formatted?.map(), type.content.filename, type.content.source.map(), type.content.info?.map())
        }
        is RustMessageType.Notice -> {
            NoticeMessageType(type.content.body, type.content.formatted?.map())
        }
        is RustMessageType.Text -> {
            TextMessageType(type.content.body, type.content.formatted?.map())
        }
        is RustMessageType.Emote -> {
            EmoteMessageType(type.content.body, type.content.formatted?.map())
        }
        is RustMessageType.Video -> {
            VideoMessageType(type.content.body, type.content.formatted?.map(), type.content.filename, type.content.source.map(), type.content.info?.map())
        }
        is RustMessageType.Location -> {
            LocationMessageType(type.content.body, type.content.geoUri, type.content.description)
        }
        is MessageType.Other -> {
            OtherMessageType(type.msgtype, type.body)
        }
    }
}

private fun RustFormattedBody.map(): FormattedBody = FormattedBody(
    format = format.map(),
    body = body
)

private fun RustMessageFormat.map(): MessageFormat {
    return when (this) {
        RustMessageFormat.Html -> MessageFormat.HTML
        is RustMessageFormat.Unknown -> MessageFormat.UNKNOWN
    }
}
