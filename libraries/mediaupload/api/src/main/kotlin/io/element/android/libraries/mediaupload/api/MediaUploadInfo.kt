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

package io.element.android.libraries.mediaupload.api

import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.VideoInfo
import java.io.File

sealed interface MediaUploadInfo {

    val file: File

    data class Image(override val file: File, val imageInfo: ImageInfo, val thumbnailFile: File?) : MediaUploadInfo
    data class Video(override val file: File, val videoInfo: VideoInfo, val thumbnailFile: File?) : MediaUploadInfo
    data class Audio(override val file: File, val audioInfo: AudioInfo) : MediaUploadInfo
    data class VoiceMessage(override val file: File, val audioInfo: AudioInfo, val waveform: List<Float>) : MediaUploadInfo
    data class AnyFile(override val file: File, val fileInfo: FileInfo) : MediaUploadInfo
}
