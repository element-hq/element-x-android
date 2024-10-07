/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.media

import kotlin.time.Duration

data class VideoInfo(
    val duration: Duration?,
    val height: Long?,
    val width: Long?,
    val mimetype: String?,
    val size: Long?,
    val thumbnailInfo: ThumbnailInfo?,
    val thumbnailSource: MediaSource?,
    val blurhash: String?
)
