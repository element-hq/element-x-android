/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.media

import io.element.android.libraries.matrix.api.media.GalleryItemInfo
import org.matrix.rustcomponents.sdk.FormattedBody as RustFormattedBody
import org.matrix.rustcomponents.sdk.GalleryItemInfo as RustGalleryItemInfo
import org.matrix.rustcomponents.sdk.UploadSource as RustUploadSource

fun GalleryItemInfo.map(): RustGalleryItemInfo = when (this) {
    is GalleryItemInfo.Image -> {
        RustGalleryItemInfo.Image(
            imageInfo = imageInfo.map(),
            source = RustUploadSource.File(file.path),
            caption = caption,
            formattedCaption = formattedCaption?.map(),
            thumbnailSource = thumbnailFile?.path?.let(RustUploadSource::File),
        )
    }
    is GalleryItemInfo.Video -> {
        RustGalleryItemInfo.Video(
            videoInfo = videoInfo.map(),
            source = RustUploadSource.File(file.path),
            caption = caption,
            formattedCaption = formattedCaption?.map(),
            thumbnailSource = thumbnailFile?.path?.let(RustUploadSource::File),
        )
    }
    is GalleryItemInfo.Audio -> {
        RustGalleryItemInfo.Audio(
            audioInfo = audioInfo.map(),
            source = RustUploadSource.File(file.path),
            caption = caption,
            formattedCaption = formattedCaption?.map(),
        )
    }
    is GalleryItemInfo.MediaFile -> {
        RustGalleryItemInfo.File(
            fileInfo = fileInfo.map(),
            source = RustUploadSource.File(file.path),
            caption = caption,
            formattedCaption = formattedCaption?.map(),
        )
    }
}
