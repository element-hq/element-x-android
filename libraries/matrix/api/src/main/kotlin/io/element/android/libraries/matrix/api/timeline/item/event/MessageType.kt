/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline.item.event

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.media.AudioDetails
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.room.location.AssetType

@Immutable
sealed interface MessageType

@Immutable
sealed interface MessageTypeWithAttachment : MessageType {
    val filename: String
    val caption: String?
    val formattedCaption: FormattedBody?

    val bestDescription: String
        get() = caption ?: filename
}

data class EmoteMessageType(
    val body: String,
    val formatted: FormattedBody?
) : MessageType

data class ImageMessageType(
    override val filename: String,
    override val caption: String?,
    override val formattedCaption: FormattedBody?,
    val source: MediaSource,
    val info: ImageInfo?
) : MessageTypeWithAttachment

// FIXME This is never used in production code.
data class StickerMessageType(
    override val filename: String,
    override val caption: String?,
    override val formattedCaption: FormattedBody?,
    val source: MediaSource,
    val info: ImageInfo?
) : MessageTypeWithAttachment

data class LocationMessageType(
    val body: String,
    val geoUri: String,
    val description: String?,
    val assetType: AssetType?,
) : MessageType

data class AudioMessageType(
    override val filename: String,
    override val caption: String?,
    override val formattedCaption: FormattedBody?,
    val source: MediaSource,
    val info: AudioInfo?,
) : MessageTypeWithAttachment

data class VoiceMessageType(
    override val filename: String,
    override val caption: String?,
    override val formattedCaption: FormattedBody?,
    val source: MediaSource,
    val info: AudioInfo?,
    val details: AudioDetails?,
) : MessageTypeWithAttachment

data class VideoMessageType(
    override val filename: String,
    override val caption: String?,
    override val formattedCaption: FormattedBody?,
    val source: MediaSource,
    val info: VideoInfo?
) : MessageTypeWithAttachment

data class FileMessageType(
    override val filename: String,
    override val caption: String?,
    override val formattedCaption: FormattedBody?,
    val source: MediaSource,
    val info: FileInfo?
) : MessageTypeWithAttachment

data class NoticeMessageType(
    val body: String,
    val formatted: FormattedBody?
) : MessageType

data class TextMessageType(
    val body: String,
    val formatted: FormattedBody?
) : MessageType

data class GalleryMessageType(
    val body: String,
    val formatted: FormattedBody?,
    val items: List<GalleryItemType>,
) : MessageType

@Immutable
sealed interface GalleryItemType {
    data class Image(val content: ImageMessageType) : GalleryItemType
    data class Audio(val content: AudioMessageType) : GalleryItemType
    data class Video(val content: VideoMessageType) : GalleryItemType
    data class File(val content: FileMessageType) : GalleryItemType
    data class Other(val itemType: String, val body: String) : GalleryItemType

    fun mediaSources(): List<MediaSource> = when (this) {
        is Image -> listOfNotNull(content.source, content.info?.thumbnailSource)
        is Audio -> listOf(content.source)
        is Video -> listOfNotNull(content.source, content.info?.thumbnailSource)
        is File -> listOfNotNull(content.source, content.info?.thumbnailSource)
        is Other -> emptyList()
    }
}

data class OtherMessageType(
    val msgType: String,
    val body: String,
) : MessageType {
    /**
     * A `m.key.verification.request` carries a body meant for clients that cannot handle in-chat
     * verification, so it must not be rendered as a message.
     */
    val isKeyVerificationRequest: Boolean
        get() = msgType == "m.key.verification.request"
}
