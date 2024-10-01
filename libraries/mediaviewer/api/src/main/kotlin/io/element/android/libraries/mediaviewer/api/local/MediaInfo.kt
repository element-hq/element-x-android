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
    val filename: String,
    val caption: String?,
    val mimeType: String,
    val formattedFileSize: String,
    val fileExtension: String,
) : Parcelable

fun anImageMediaInfo(): MediaInfo = MediaInfo(
    filename = "an image file.jpg",
    caption = null,
    mimeType = MimeTypes.Jpeg,
    formattedFileSize = "4MB",
    fileExtension = "jpg",
)

fun aVideoMediaInfo(): MediaInfo = MediaInfo(
    filename = "a video file.mp4",
    caption = null,
    mimeType = MimeTypes.Mp4,
    formattedFileSize = "14MB",
    fileExtension = "mp4",
)

fun aPdfMediaInfo(): MediaInfo = MediaInfo(
    filename = "a pdf file.pdf",
    caption = null,
    mimeType = MimeTypes.Pdf,
    formattedFileSize = "23MB",
    fileExtension = "pdf",
)

fun anApkMediaInfo(): MediaInfo = MediaInfo(
    filename = "an apk file.apk",
    caption = null,
    mimeType = MimeTypes.Apk,
    formattedFileSize = "50MB",
    fileExtension = "apk",
)

fun anAudioMediaInfo(): MediaInfo = MediaInfo(
    filename = "an audio file.mp3",
    caption = null,
    mimeType = MimeTypes.Mp3,
    formattedFileSize = "7MB",
    fileExtension = "mp3",
)
