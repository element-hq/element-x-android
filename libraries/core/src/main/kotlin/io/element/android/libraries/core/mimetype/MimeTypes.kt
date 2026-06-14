/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.mimetype

import io.element.android.libraries.core.bool.orFalse

// The Android SDK does not provide constant for mime type, add some of them here
@Suppress("ktlint:standard:property-naming")
object MimeTypes {
    const val Any: String = "*/*"
    const val Json = "application/json"
    const val OctetStream = "application/octet-stream"
    const val Apk = "application/vnd.android.package-archive"
    const val Pdf = "application/pdf"

    const val Images = "image/*"

    const val Png = "image/png"
    const val BadJpg = "image/jpg"
    const val Jpeg = "image/jpeg"
    const val Gif = "image/gif"
    const val WebP = "image/webp"
    const val Svg = "image/svg+xml"

    const val Videos = "video/*"
    const val Mp4 = "video/mp4"

    const val Audio = "audio/*"

    const val Ogg = "audio/ogg"
    const val Mp3 = "audio/mp3"

    const val PlainText = "text/plain"

    fun String?.normalizeMimeType() = if (this == BadJpg) Jpeg else this

    fun String?.isMimeTypeImage() = this?.startsWith("image/").orFalse()
    fun String?.isMimeTypeAnimatedImage() = this == Gif || this == WebP
    fun String?.isMimeTypeVideo() = this?.startsWith("video/").orFalse()
    fun String?.isMimeTypeAudio() = this?.startsWith("audio/").orFalse()
    fun String?.isMimeTypeApplication() = this?.startsWith("application/").orFalse()
    fun String?.isMimeTypeFile() = this?.startsWith("file/").orFalse()
    fun String?.isMimeTypeText() = this?.startsWith("text/").orFalse()
    fun String?.isMimeTypeAny() = this?.startsWith("*/").orFalse()

    fun fromFileExtension(fileExtension: String): String {
        return when (fileExtension.lowercase()) {
            "apk" -> Apk
            "pdf" -> Pdf
            else -> OctetStream
        }
    }

    fun hasSubtype(mimeType: String): Boolean {
        val components = mimeType.split("/")
        if (components.size != 2) return false
        val subType = components.last()
        return subType.isNotBlank() && subType != "*"
    }
}
