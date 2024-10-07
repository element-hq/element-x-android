/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.media

import io.element.android.libraries.matrix.api.media.ImageInfo
import org.matrix.rustcomponents.sdk.ImageInfo as RustImageInfo

fun RustImageInfo.map(): ImageInfo = ImageInfo(
    height = height?.toLong(),
    width = width?.toLong(),
    mimetype = mimetype,
    size = size?.toLong(),
    thumbnailInfo = thumbnailInfo?.map(),
    thumbnailSource = thumbnailSource?.map(),
    blurhash = blurhash
)

fun ImageInfo.map(): RustImageInfo = RustImageInfo(
    height = height?.toULong(),
    width = width?.toULong(),
    mimetype = mimetype,
    size = size?.toULong(),
    thumbnailInfo = thumbnailInfo?.map(),
    thumbnailSource = null,
    blurhash = blurhash,
)
