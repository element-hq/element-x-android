/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.media

import java.io.File

sealed interface GalleryItemInfo {
    val file: File

    data class Image(
        override val file: File,
        val imageInfo: ImageInfo,
        val thumbnailFile: File?,
    ) : GalleryItemInfo

    data class Video(
        override val file: File,
        val videoInfo: VideoInfo,
        val thumbnailFile: File?,
    ) : GalleryItemInfo

    data class Audio(
        override val file: File,
        val audioInfo: AudioInfo,
    ) : GalleryItemInfo

    data class MediaFile(
        override val file: File,
        val fileInfo: FileInfo,
    ) : GalleryItemInfo
}
