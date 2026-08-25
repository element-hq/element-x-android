/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiMediaSource
import io.element.android.libraries.matrix.test.A_MESSAGE
import org.matrix.rustcomponents.sdk.AudioInfo
import org.matrix.rustcomponents.sdk.AudioMessageContent
import org.matrix.rustcomponents.sdk.FileInfo
import org.matrix.rustcomponents.sdk.FileMessageContent
import org.matrix.rustcomponents.sdk.FormattedBody
import org.matrix.rustcomponents.sdk.GalleryItemType
import org.matrix.rustcomponents.sdk.GalleryMessageContent
import org.matrix.rustcomponents.sdk.ImageInfo
import org.matrix.rustcomponents.sdk.ImageMessageContent
import org.matrix.rustcomponents.sdk.MediaSource
import org.matrix.rustcomponents.sdk.MessageLikeEventContent
import org.matrix.rustcomponents.sdk.MessageType
import org.matrix.rustcomponents.sdk.TextMessageContent
import org.matrix.rustcomponents.sdk.TimelineEventContent
import org.matrix.rustcomponents.sdk.UnstableAudioDetailsContent
import org.matrix.rustcomponents.sdk.UnstableVoiceContent
import org.matrix.rustcomponents.sdk.VideoInfo
import org.matrix.rustcomponents.sdk.VideoMessageContent

internal fun aRustTimelineEventContentMessageLike(
    content: MessageLikeEventContent = aRustMessageLikeEventContentRoomMessage(),
) = TimelineEventContent.MessageLike(
    content = content,
)

internal fun aRustMessageLikeEventContentRoomMessage(
    messageType: MessageType = aRustMessageTypeText(),
    inReplyToEventId: String? = null,
) = MessageLikeEventContent.RoomMessage(
    messageType = messageType,
    inReplyToEventId = inReplyToEventId,
)

internal fun aRustMessageTypeText(
    content: TextMessageContent = aRustTextMessageContent(),
) = MessageType.Text(
    content = content,
)

internal fun aRustTextMessageContent(
    body: String = A_MESSAGE,
    formatted: FormattedBody? = null,
) = TextMessageContent(
    body = body,
    formatted = formatted,
)

internal fun aRustMessageTypeGallery(
    content: GalleryMessageContent = aRustGalleryMessageContent(),
) = MessageType.Gallery(content = content)

internal fun aRustGalleryMessageContent(
    body: String = "A gallery",
    formatted: FormattedBody? = null,
    itemTypes: List<GalleryItemType> = listOf(aRustGalleryItemTypeImage()),
) = GalleryMessageContent(body = body, formatted = formatted, itemtypes = itemTypes)

internal fun aRustGalleryItemTypeImage(
    content: ImageMessageContent = aRustImageMessageContent(),
) = GalleryItemType.Image(content = content)

internal fun aRustGalleryItemTypeAudio(
    content: AudioMessageContent = aRustAudioMessageContent(),
) = GalleryItemType.Audio(content = content)

internal fun aRustGalleryItemTypeVideo(
    content: VideoMessageContent = aRustVideoMessageContent(),
) = GalleryItemType.Video(content = content)

internal fun aRustGalleryItemTypeFile(
    content: FileMessageContent = aRustFileMessageContent(),
) = GalleryItemType.File(content = content)

internal fun aRustGalleryItemTypeOther(
    itemType: String = "m.unknown",
    body: String = "unknown item",
) = GalleryItemType.Other(itemtype = itemType, body = body)

internal fun aRustImageMessageContent(
    filename: String = "image.jpg",
    caption: String? = null,
    formattedCaption: FormattedBody? = null,
    source: MediaSource = FakeFfiMediaSource("mxc://server/image"),
    info: ImageInfo? = null,
) = ImageMessageContent(
    filename = filename,
    caption = caption,
    formattedCaption = formattedCaption,
    source = source,
    info = info,
)

internal fun aRustAudioMessageContent(
    filename: String = "audio.mp3",
    caption: String? = null,
    formattedCaption: FormattedBody? = null,
    source: MediaSource = FakeFfiMediaSource("mxc://server/audio"),
    info: AudioInfo? = null,
    audio: UnstableAudioDetailsContent? = null,
    voice: UnstableVoiceContent? = null,
) = AudioMessageContent(
    filename = filename,
    caption = caption,
    formattedCaption = formattedCaption,
    source = source,
    info = info,
    audio = audio,
    voice = voice,
)

internal fun aRustVideoMessageContent(
    filename: String = "video.mp4",
    caption: String? = null,
    formattedCaption: FormattedBody? = null,
    source: MediaSource = FakeFfiMediaSource("mxc://server/video"),
    info: VideoInfo? = null,
) = VideoMessageContent(
    filename = filename,
    caption = caption,
    formattedCaption = formattedCaption,
    source = source,
    info = info,
)

internal fun aRustFileMessageContent(
    filename: String = "document.pdf",
    caption: String? = null,
    formattedCaption: FormattedBody? = null,
    source: MediaSource = FakeFfiMediaSource("mxc://server/file"),
    info: FileInfo? = null,
) = FileMessageContent(
    filename = filename,
    caption = caption,
    formattedCaption = formattedCaption,
    source = source,
    info = info,
)
