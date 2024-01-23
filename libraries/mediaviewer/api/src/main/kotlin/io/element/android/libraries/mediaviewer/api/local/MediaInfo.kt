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

package io.element.android.libraries.mediaviewer.api.local

import android.os.Parcelable
import io.element.android.libraries.core.mimetype.MimeTypes
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaInfo(
    val name: String,
    val mimeType: String,
    val formattedFileSize: String,
    val fileExtension: String,
) : Parcelable

fun anImageInfo(): MediaInfo = MediaInfo(
    "an image file.jpg",
    MimeTypes.Jpeg,
    "4MB",
    "jpg"
)

fun aVideoInfo(): MediaInfo = MediaInfo(
    "a video file.mp4",
    MimeTypes.Mp4,
    "14MB",
    "mp4"
)

fun aPdfInfo(): MediaInfo = MediaInfo(
    "a pdf file.pdf",
    MimeTypes.Pdf,
    "23MB",
    "pdf"
)

fun aFileInfo(): MediaInfo = MediaInfo(
    "an apk file.apk",
    MimeTypes.Apk,
    "50MB",
    "apk"
)

fun anAudioInfo(): MediaInfo = MediaInfo(
    "an audio file.mp3",
    MimeTypes.Mp3,
    "7MB",
    "mp3"
)
