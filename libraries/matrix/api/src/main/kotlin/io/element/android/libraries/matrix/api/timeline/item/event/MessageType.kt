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

data class EmoteMessageType(
    val body: String,
    val formatted: FormattedBody?
) : MessageType

data class ImageMessageType(
    val body: String,
    val formatted: FormattedBody?,
    val filename: String?,
    val source: MediaSource,
    val info: ImageInfo?
) : MessageType

data class StickerMessageType(
    val body: String,
    val source: MediaSource,
    val info: ImageInfo?
) : MessageType

data class LocationMessageType(
    val body: String,
    val geoUri: String,
    val description: String?,
) : MessageType

data class AudioMessageType(
    val body: String,
    val source: MediaSource,
    val info: AudioInfo?,
) : MessageType

data class VoiceMessageType(
    val body: String,
    val source: MediaSource,
    val info: AudioInfo?,
    val details: AudioDetails?,
) : MessageType

data class VideoMessageType(
    val body: String,
    val formatted: FormattedBody?,
    val filename: String?,
    val source: MediaSource,
    val info: VideoInfo?
) : MessageType

data class FileMessageType(
    val body: String,
    val source: MediaSource,
    val info: FileInfo?
) : MessageType

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
