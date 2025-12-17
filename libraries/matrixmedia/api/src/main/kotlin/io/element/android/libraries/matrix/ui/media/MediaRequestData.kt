/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media

import io.element.android.libraries.matrix.api.media.MediaSource

/**
 * Can be use with [coil3.compose.AsyncImage] to load a [MediaSource].
 * This will go internally through our CoilMediaFetcher.
 *
 * Example of usage:
 *  AsyncImage(
 *      model = MediaRequestData(mediaSource, MediaRequestData.Kind.Content),
 *      contentScale = ContentScale.Fit,
 *  )
 *
 */
data class MediaRequestData(
    val source: MediaSource?,
    val kind: Kind
) {
    sealed interface Kind {
        data object Content : Kind

        data class File(
            val fileName: String,
            val mimeType: String,
        ) : Kind

        data class Thumbnail(val width: Long, val height: Long) : Kind {
            constructor(size: Long) : this(size, size)
        }
    }
}

/** Max width a thumbnail can have according to [the spec](https://spec.matrix.org/v1.10/client-server-api/#thumbnails). */
const val MAX_THUMBNAIL_WIDTH = 800L

/** Max height a thumbnail can have according to [the spec](https://spec.matrix.org/v1.10/client-server-api/#thumbnails). */
const val MAX_THUMBNAIL_HEIGHT = 600L
