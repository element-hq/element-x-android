/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.api

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

fun MediaUploadInfo.allFiles(): List<File> {
    return listOfNotNull(
        file,
        (this@allFiles as? MediaUploadInfo.Image)?.thumbnailFile,
        (this@allFiles as? MediaUploadInfo.Video)?.thumbnailFile,
    )
}

fun MediaUploadInfo.toGalleryItemInfo(caption: String?, formattedCaption: String?): GalleryItemInfo {
    return when (this) {
        is MediaUploadInfo.Image -> GalleryItemInfo.Image(
            file = file,
            imageInfo = imageInfo,
            thumbnailFile = thumbnailFile,
            caption = caption,
            formattedCaption = null,
        )
        is MediaUploadInfo.Video -> GalleryItemInfo.Video(
            file = file,
            videoInfo = videoInfo,
            thumbnailFile = thumbnailFile,
            caption = caption,
            formattedCaption = null,
        )
        is MediaUploadInfo.Audio -> GalleryItemInfo.Audio(
            file = file,
            audioInfo = audioInfo,
            caption = caption,
            formattedCaption = null,
        )
        is MediaUploadInfo.VoiceMessage -> GalleryItemInfo.Audio(
            file = file,
            audioInfo = audioInfo,
            caption = caption,
            formattedCaption = null,
        )
        is MediaUploadInfo.AnyFile -> GalleryItemInfo.MediaFile(
            file = file,
            fileInfo = fileInfo,
            caption = caption,
            formattedCaption = null,
        )
    }
}
