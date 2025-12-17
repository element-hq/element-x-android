/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import io.element.android.libraries.core.mimetype.MimeTypes

data class Asset(
    val filename: String,
    val mimeType: String,
    val size: Long,
    val width: Long?,
    val height: Long?,
)

/**
 * "image.png" is a 1_818 x 1_178 PNG image with a size of 1_856_786 bytes.
 */
val assetImagePng = Asset(
    filename = "image.png",
    mimeType = MimeTypes.Png,
    size = 1_856_786,
    width = 1_818,
    height = 1_178,
)

/**
 * "image.jpeg" is a 12_024 x 3_916, JPEG image with a size of 9_986_336 bytes.
 */
val assetImageJpeg = Asset(
    filename = "image.jpeg",
    mimeType = MimeTypes.Jpeg,
    size = 9_986_336,
    width = 12_024,
    height = 3_916,
)

/**
 *  "video.mp4" is a 1_280 x 720, MP4 video with a size of 1_673_712 bytes.
 */
val assetVideo = Asset(
    filename = "video.mp4",
    mimeType = MimeTypes.Mp4,
    size = 1_673_712,
    width = 1_280,
    height = 720,
)

/**
 * "sample3s.mp3" is a 3 seconds MP3 audio file with a size of 52_079 bytes.
 */
val assetAudio = Asset(
    filename = "sample3s.mp3",
    mimeType = MimeTypes.Mp3,
    size = 52_079,
    width = null,
    height = null,
)

/**
 * "text.txt" is a 13 bytes text file.
 */
val assetText = Asset(
    filename = "text.txt",
    mimeType = MimeTypes.PlainText,
    size = 13,
    width = null,
    height = null,
)

/**
 * "animated_gif.gif" is a 800 x 600, GIF image with a size of 687_979 bytes.
 */
val assetAnimatedGif = Asset(
    filename = "animated_gif.gif",
    mimeType = MimeTypes.Gif,
    size = 687_979,
    width = 800,
    height = 600,
)
