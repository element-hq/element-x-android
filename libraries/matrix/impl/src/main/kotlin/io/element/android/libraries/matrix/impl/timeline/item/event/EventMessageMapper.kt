/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.item.event

import io.element.android.libraries.matrix.api.timeline.item.EventThreadInfo
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
import io.element.android.libraries.matrix.impl.timeline.reply.InReplyToMapper
import org.matrix.rustcomponents.sdk.InReplyToDetails
import org.matrix.rustcomponents.sdk.MessageType
import org.matrix.rustcomponents.sdk.MsgLikeKind
import org.matrix.rustcomponents.sdk.use
import org.matrix.rustcomponents.sdk.FormattedBody as RustFormattedBody
import org.matrix.rustcomponents.sdk.MessageFormat as RustMessageFormat
import org.matrix.rustcomponents.sdk.MessageType as RustMessageType

// https://github.com/Johennes/matrix-spec-proposals/blob/johannes/msgtype-galleries/proposals/4274-inline-media-galleries.md#unstable-prefix
private const val MSG_TYPE_GALLERY_UNSTABLE = "dm.filament.gallery"

class EventMessageMapper {
    private val inReplyToMapper by lazy { InReplyToMapper(TimelineEventContentMapper()) }

    fun map(message: MsgLikeKind.Message, inReplyTo: InReplyToDetails?, threadInfo: EventThreadInfo?): MessageContent = message.use {
        val type = it.content.msgType.use(this::mapMessageType)
        val inReplyToEvent: InReplyTo? = inReplyTo?.use(inReplyToMapper::map)
        MessageContent(
            body = it.content.body,
            inReplyTo = inReplyToEvent,
            isEdited = it.content.isEdited,
            threadInfo = threadInfo,
            type = type
        )
    }

    fun mapMessageType(type: RustMessageType) = when (type) {
        is RustMessageType.Audio -> {
            when (type.content.voice) {
                null -> {
                    AudioMessageType(
                        filename = type.content.filename,
                        caption = type.content.caption,
                        formattedCaption = type.content.formattedCaption?.map(),
                        source = type.content.source.map(),
                        info = type.content.info?.map(),
                    )
                }
                else -> {
                    VoiceMessageType(
                        filename = type.content.filename,
                        caption = type.content.caption,
                        formattedCaption = type.content.formattedCaption?.map(),
                        source = type.content.source.map(),
                        info = type.content.info?.map(),
                        details = type.content.audio?.map(),
                    )
                }
            }
        }
        is RustMessageType.File -> {
            FileMessageType(
                filename = type.content.filename,
                caption = type.content.caption,
                formattedCaption = type.content.formattedCaption?.map(),
                source = type.content.source.map(),
                info = type.content.info?.map(),
            )
        }
        is RustMessageType.Image -> {
            ImageMessageType(
                filename = type.content.filename,
                caption = type.content.caption,
                formattedCaption = type.content.formattedCaption?.map(),
                source = type.content.source.map(),
                info = type.content.info?.map(),
            )
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
            VideoMessageType(
                filename = type.content.filename,
                caption = type.content.caption,
                formattedCaption = type.content.formattedCaption?.map(),
                source = type.content.source.map(),
                info = type.content.info?.map(),
            )
        }
        is RustMessageType.Location -> {
            LocationMessageType(type.content.body, type.content.geoUri, type.content.description)
        }
        is MessageType.Other -> {
            OtherMessageType(type.msgtype, type.body)
        }
        is MessageType.Gallery -> {
            // TODO expose the GalleryType.
            OtherMessageType(MSG_TYPE_GALLERY_UNSTABLE, type.content.body)
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
