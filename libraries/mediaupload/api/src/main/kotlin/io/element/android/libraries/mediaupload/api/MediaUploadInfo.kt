/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.api

import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.GalleryItemInfo
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

/**
 * The mime type the media was actually processed into. For images, this reflects whatever [MediaPreProcessor]
 * decided the real format was (which may differ from what was declared when [MediaPreProcessor.process] was
 * called) - this matters for avatars, where the caller intentionally lets the processor resolve the real type
 * (e.g. to detect an animated GIF/WebP/PNG and skip compressing it) rather than assuming a fixed format upfront.
 */
fun MediaUploadInfo.resolvedMimeType(): String = when (this) {
    is MediaUploadInfo.Image -> imageInfo.mimetype ?: MimeTypes.Jpeg
    is MediaUploadInfo.Video -> videoInfo.mimetype ?: MimeTypes.Mp4
    is MediaUploadInfo.Audio -> audioInfo.mimetype ?: MimeTypes.OctetStream
    is MediaUploadInfo.VoiceMessage -> audioInfo.mimetype ?: MimeTypes.OctetStream
    is MediaUploadInfo.AnyFile -> fileInfo.mimetype ?: MimeTypes.OctetStream
}


fun MediaUploadInfo.allFiles(): List<File> {
    return listOfNotNull(
        file,
        (this@allFiles as? MediaUploadInfo.Image)?.thumbnailFile,
        (this@allFiles as? MediaUploadInfo.Video)?.thumbnailFile,
    )
}

fun MediaUploadInfo.toGalleryItemInfo(): GalleryItemInfo {
    return when (this) {
        is MediaUploadInfo.Image -> GalleryItemInfo.Image(
            file = file,
            imageInfo = imageInfo,
            thumbnailFile = thumbnailFile,
        )
        is MediaUploadInfo.Video -> GalleryItemInfo.Video(
            file = file,
            videoInfo = videoInfo,
            thumbnailFile = thumbnailFile,
        )
        is MediaUploadInfo.Audio -> GalleryItemInfo.Audio(
            file = file,
            audioInfo = audioInfo,
        )
        is MediaUploadInfo.VoiceMessage -> GalleryItemInfo.Audio(
            file = file,
            audioInfo = audioInfo,
        )
        is MediaUploadInfo.AnyFile -> GalleryItemInfo.MediaFile(
            file = file,
            fileInfo = fileInfo,
        )
    }
}
