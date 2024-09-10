/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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

fun anImageMediaInfo(): MediaInfo = MediaInfo(
    "an image file.jpg",
    MimeTypes.Jpeg,
    "4MB",
    "jpg"
)

fun aVideoMediaInfo(): MediaInfo = MediaInfo(
    "a video file.mp4",
    MimeTypes.Mp4,
    "14MB",
    "mp4"
)

fun aPdfMediaInfo(): MediaInfo = MediaInfo(
    "a pdf file.pdf",
    MimeTypes.Pdf,
    "23MB",
    "pdf"
)

fun anApkMediaInfo(): MediaInfo = MediaInfo(
    "an apk file.apk",
    MimeTypes.Apk,
    "50MB",
    "apk"
)

fun anAudioMediaInfo(): MediaInfo = MediaInfo(
    "an audio file.mp3",
    MimeTypes.Mp3,
    "7MB",
    "mp3"
)
