/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.media

import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import java.io.File

sealed interface GalleryItemInfo {
    val file: File
    val caption: String?
    val formattedCaption: FormattedBody?

    data class Image(
        override val file: File,
        val imageInfo: ImageInfo,
        val thumbnailFile: File?,
        override val caption: String?,
        override val formattedCaption: FormattedBody?,
    ) : GalleryItemInfo

    data class Video(
        override val file: File,
        val videoInfo: VideoInfo,
        val thumbnailFile: File?,
        override val caption: String?,
        override val formattedCaption: FormattedBody?,
    ) : GalleryItemInfo

    data class Audio(
        override val file: File,
        val audioInfo: AudioInfo,
        override val caption: String?,
        override val formattedCaption: FormattedBody?,
    ) : GalleryItemInfo

    data class MediaFile(
        override val file: File,
        val fileInfo: FileInfo,
        override val caption: String?,
        override val formattedCaption: FormattedBody?,
    ) : GalleryItemInfo
}
