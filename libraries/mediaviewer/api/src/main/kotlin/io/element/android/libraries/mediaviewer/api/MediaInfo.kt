/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.api

import android.os.Parcelable
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaInfo(
    val filename: String,
    val caption: String?,
    val mimeType: String,
    val fileSize: Long?,
    val formattedFileSize: String,
    val fileExtension: String,
    val senderId: UserId?,
    val senderName: String?,
    val senderAvatar: String?,
    val dateSent: String?,
    val dateSentFull: String?,
    val waveform: List<Float>?,
    val duration: String?,
) : Parcelable

fun anImageMediaInfo(
    senderId: UserId? = UserId("@alice:server.org"),
    caption: String? = null,
    senderName: String? = null,
    dateSent: String? = null,
    dateSentFull: String? = null,
): MediaInfo = MediaInfo(
    filename = "an image file.jpg",
    fileSize = 4 * 1024 * 1024,
    caption = caption,
    mimeType = MimeTypes.Jpeg,
    formattedFileSize = "4MB",
    fileExtension = "jpg",
    senderId = senderId,
    senderName = senderName,
    senderAvatar = null,
    dateSent = dateSent,
    dateSentFull = dateSentFull,
    waveform = null,
    duration = null,
)

fun aVideoMediaInfo(
    caption: String? = null,
    senderName: String? = null,
    dateSent: String? = null,
    dateSentFull: String? = null,
    duration: String? = null,
): MediaInfo = MediaInfo(
    filename = "a video file.mp4",
    fileSize = 14 * 1024 * 1024,
    caption = caption,
    mimeType = MimeTypes.Mp4,
    formattedFileSize = "14MB",
    fileExtension = "mp4",
    senderId = UserId("@alice:server.org"),
    senderName = senderName,
    senderAvatar = null,
    dateSent = dateSent,
    dateSentFull = dateSentFull,
    waveform = null,
    duration = duration,
)

fun aPdfMediaInfo(
    filename: String = "a pdf file.pdf",
    caption: String? = null,
    senderName: String? = null,
    dateSent: String? = null,
    dateSentFull: String? = null,
): MediaInfo = MediaInfo(
    filename = filename,
    fileSize = 23 * 1024 * 1024,
    caption = caption,
    mimeType = MimeTypes.Pdf,
    formattedFileSize = "23MB",
    fileExtension = "pdf",
    senderId = UserId("@alice:server.org"),
    senderName = senderName,
    senderAvatar = null,
    dateSent = dateSent,
    dateSentFull = dateSentFull,
    waveform = null,
    duration = null,
)

fun anApkMediaInfo(
    senderId: UserId? = UserId("@alice:server.org"),
    senderName: String? = null,
    dateSent: String? = null,
    dateSentFull: String? = null,
): MediaInfo = MediaInfo(
    filename = "an apk file.apk",
    fileSize = 50 * 1024 * 1024,
    caption = null,
    mimeType = MimeTypes.Apk,
    formattedFileSize = "50MB",
    fileExtension = "apk",
    senderId = senderId,
    senderName = senderName,
    senderAvatar = null,
    dateSent = dateSent,
    dateSentFull = dateSentFull,
    waveform = null,
    duration = null,
)

fun anAudioMediaInfo(
    filename: String = "an audio file.mp3",
    caption: String? = null,
    senderName: String? = null,
    dateSent: String? = null,
    dateSentFull: String? = null,
    waveForm: List<Float>? = null,
    duration: String? = null,
): MediaInfo = MediaInfo(
    filename = filename,
    fileSize = 7 * 1024 * 1024,
    caption = caption,
    mimeType = MimeTypes.Mp3,
    formattedFileSize = "7MB",
    fileExtension = "mp3",
    senderId = UserId("@alice:server.org"),
    senderName = senderName,
    senderAvatar = null,
    dateSent = dateSent,
    dateSentFull = dateSentFull,
    waveform = waveForm,
    duration = duration,
)

fun aVoiceMediaInfo(
    filename: String = "a voice file.ogg",
    caption: String? = null,
    senderName: String? = null,
    dateSent: String? = null,
    dateSentFull: String? = null,
    waveForm: List<Float>? = null,
    duration: String? = null,
): MediaInfo = MediaInfo(
    filename = filename,
    fileSize = 3 * 1024 * 1024,
    caption = caption,
    mimeType = MimeTypes.Ogg,
    formattedFileSize = "3MB",
    fileExtension = "ogg",
    senderId = UserId("@alice:server.org"),
    senderName = senderName,
    senderAvatar = null,
    dateSent = dateSent,
    dateSentFull = dateSentFull,
    waveform = waveForm,
    duration = duration,
)

fun aTxtMediaInfo(
    filename: String = "a text file.txt",
    caption: String? = null,
    senderName: String? = null,
    dateSent: String? = null,
    dateSentFull: String? = null,
): MediaInfo = MediaInfo(
    filename = filename,
    fileSize = 2 * 1024,
    caption = caption,
    mimeType = MimeTypes.PlainText,
    formattedFileSize = "2kB",
    fileExtension = "txt",
    senderId = UserId("@alice:server.org"),
    senderName = senderName,
    senderAvatar = null,
    dateSent = dateSent,
    dateSentFull = dateSentFull,
    waveform = null,
    duration = null,
)
