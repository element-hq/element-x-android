/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media

import io.element.android.libraries.matrix.api.media.MediaSource

/**
 * Can be use with [coil.compose.AsyncImage] to load a [MediaSource].
 * This will go internally through our [CoilMediaFetcher].
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
