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
