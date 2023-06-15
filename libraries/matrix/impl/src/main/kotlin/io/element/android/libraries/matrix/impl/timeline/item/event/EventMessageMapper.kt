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
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.UnknownMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.matrix.impl.media.map
import org.matrix.rustcomponents.sdk.Message
import org.matrix.rustcomponents.sdk.MessageType
import org.matrix.rustcomponents.sdk.ProfileDetails
import org.matrix.rustcomponents.sdk.RepliedToEventDetails
import org.matrix.rustcomponents.sdk.use
import org.matrix.rustcomponents.sdk.FormattedBody as RustFormattedBody
import org.matrix.rustcomponents.sdk.MessageFormat as RustMessageFormat

class EventMessageMapper {

    fun map(message: Message): MessageContent = message.use {
        val type = it.msgtype().use { type ->
            when (type) {
                is MessageType.Audio -> {
                    AudioMessageType(type.content.body, type.content.source.map(), type.content.info?.map())
                }
                is MessageType.File -> {
                    FileMessageType(type.content.body, type.content.source.map(), type.content.info?.map())
                }
                is MessageType.Image -> {
                    ImageMessageType(type.content.body, type.content.source.map(), type.content.info?.map())
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
                    VideoMessageType(type.content.body, type.content.source.map(), type.content.info?.map())
                }
                null -> {
                    UnknownMessageType
                }
            }
        }
        val inReplyToId = it.inReplyTo()?.eventId?.let(::EventId)
        val inReplyToEvent: InReplyTo? = (it.inReplyTo()?.event)?.use { details ->
            when (details) {
                is RepliedToEventDetails.Ready -> {
                    val senderProfile = details.senderProfile as? ProfileDetails.Ready
                    InReplyTo.Ready(
                        eventId = inReplyToId!!,
                        content = map(details.message),
                        senderId = UserId(details.sender),
                        senderDisplayName = senderProfile?.displayName,
                        senderAvatarUrl = senderProfile?.avatarUrl,
                    )
                }
                is RepliedToEventDetails.Error -> InReplyTo.Error
                is RepliedToEventDetails.Pending, is RepliedToEventDetails.Unavailable -> InReplyTo.NotLoaded(inReplyToId!!)
            }
        }
        MessageContent(
            body = it.body(),
            inReplyTo = inReplyToEvent,
            isEdited = it.isEdited(),
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
        org.matrix.rustcomponents.sdk.MessageFormat.Html -> MessageFormat.HTML
        is org.matrix.rustcomponents.sdk.MessageFormat.Unknown -> MessageFormat.UNKNOWN
    }
}
