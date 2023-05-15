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
import io.element.android.libraries.matrix.api.media.ThumbnailInfo
import io.element.android.libraries.matrix.api.media.VideoInfo
import java.io.File

sealed interface MediaUploadInfo {
    data class Image(val file: File, val info: ImageInfo, val thumbnailInfo: ThumbnailProcessingInfo) : MediaUploadInfo
    data class Video(val file: File, val info: VideoInfo, val thumbnailInfo: ThumbnailProcessingInfo) : MediaUploadInfo
    data class Audio(val file: File, val info: AudioInfo) : MediaUploadInfo
    data class AnyFile(val file: File, val info: FileInfo) : MediaUploadInfo
}

data class ThumbnailProcessingInfo(
    val file: File,
    val info: ThumbnailInfo,
    val blurhash: String,
)
