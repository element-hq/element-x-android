/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.core.mimetype

import io.element.android.libraries.core.bool.orFalse

// The Android SDK does not provide constant for mime type, add some of them here
object MimeTypes {
    const val Any: String = "*/*"
    const val OctetStream = "application/octet-stream"
    const val Apk = "application/vnd.android.package-archive"
    const val Pdf = "application/pdf"

    const val Images = "image/*"

    const val Png = "image/png"
    const val BadJpg = "image/jpg"
    const val Jpeg = "image/jpeg"
    const val Gif = "image/gif"
    const val WebP = "image/webp"

    const val Videos = "video/*"
    const val Mp4 = "video/mp4"

    const val Audio = "audio/*"

    const val Ogg = "audio/ogg"
    const val Mp3 = "audio/mp3"

    const val PlainText = "text/plain"

    fun String?.normalizeMimeType() = if (this == BadJpg) Jpeg else this

    fun String?.isMimeTypeImage() = this?.startsWith("image/").orFalse()
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
