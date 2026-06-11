/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview

import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeAnimatedImage
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeImage
import io.element.android.libraries.mediaviewer.api.MediaInfo
import java.util.Locale

internal fun MediaInfo.canEditImage(): Boolean {
    val resolvedMimeType = resolvedImageMimeType() ?: return false
    return resolvedMimeType.isMimeTypeImage() &&
        !resolvedMimeType.isMimeTypeAnimatedImage() &&
        resolvedMimeType != MimeTypes.Svg
}

internal fun MediaInfo.isImageAttachment(): Boolean {
    return resolvedImageMimeType().isMimeTypeImage()
}

internal fun MediaInfo.resolvedImageMimeType(): String? {
    return mimeType.takeIf { it.isMimeTypeImage() } ?: fileExtension.toImageMimeTypeOrNull()
}

private fun String.toImageMimeTypeOrNull(): String? {
    return when (lowercase(Locale.ROOT)) {
        "png" -> MimeTypes.Png
        "jpg", "jpeg" -> MimeTypes.Jpeg
        "gif" -> MimeTypes.Gif
        "webp" -> MimeTypes.WebP
        "svg" -> MimeTypes.Svg
        "bmp" -> "image/bmp"
        "heic" -> "image/heic"
        "heif" -> "image/heif"
        "avif" -> "image/avif"
        else -> null
    }
}
