/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline.item.event

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.media.AudioDetails
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.media.VideoInfo

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

data class OtherMessageType(
    val msgType: String,
    val body: String,
) : MessageType
