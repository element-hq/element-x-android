/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import android.graphics.Bitmap
import io.element.android.libraries.core.mimetype.MimeTypes

fun mimeTypeToCompressFormat(mimeType: String) = when (mimeType) {
    MimeTypes.Png -> Bitmap.CompressFormat.PNG
    else -> Bitmap.CompressFormat.JPEG
}

fun mimeTypeToCompressFileExtension(mimeType: String) = when (mimeType) {
    MimeTypes.Png -> "png"
    else -> "jpeg"
}

fun mimeTypeToThumbnailMimeType(mimeType: String) = when (mimeType) {
    MimeTypes.Png -> MimeTypes.Png
    else -> MimeTypes.Jpeg
}
