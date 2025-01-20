/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
    val formattedFileSize: String,
    val fileExtension: String,
    val senderId: UserId?,
    val senderName: String?,
    val senderAvatar: String?,
    val dateSent: String?,
    val dateSentFull: String?,
    val waveform: List<Float>?,
) : Parcelable

fun anImageMediaInfo(
    senderId: UserId? = UserId("@alice:server.org"),
    caption: String? = null,
    senderName: String? = null,
    dateSent: String? = null,
    dateSentFull: String? = null,
): MediaInfo = MediaInfo(
    filename = "an image file.jpg",
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
)

fun aVideoMediaInfo(
    caption: String? = null,
    senderName: String? = null,
    dateSent: String? = null,
    dateSentFull: String? = null,
): MediaInfo = MediaInfo(
    filename = "a video file.mp4",
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
)

fun aPdfMediaInfo(
    filename: String = "a pdf file.pdf",
    caption: String? = null,
    senderName: String? = null,
    dateSent: String? = null,
    dateSentFull: String? = null,
): MediaInfo = MediaInfo(
    filename = filename,
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
)

fun anApkMediaInfo(
    senderId: UserId? = UserId("@alice:server.org"),
    senderName: String? = null,
    dateSent: String? = null,
    dateSentFull: String? = null,
): MediaInfo = MediaInfo(
    filename = "an apk file.apk",
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
)

fun anAudioMediaInfo(
    filename: String = "an audio file.mp3",
    caption: String? = null,
    senderName: String? = null,
    dateSent: String? = null,
    dateSentFull: String? = null,
    waveForm: List<Float>? = null,
): MediaInfo = MediaInfo(
    filename = filename,
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
)

fun aVoiceMediaInfo(
    filename: String = "a voice file.ogg",
    caption: String? = null,
    senderName: String? = null,
    dateSent: String? = null,
    dateSentFull: String? = null,
    waveForm: List<Float>? = null,
): MediaInfo = MediaInfo(
    filename = filename,
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
)
