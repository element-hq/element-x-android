/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.media

import io.element.android.libraries.matrix.api.media.ThumbnailInfo
import org.matrix.rustcomponents.sdk.ThumbnailInfo as RustThumbnailInfo

fun RustThumbnailInfo.map(): ThumbnailInfo = ThumbnailInfo(
    height = height?.toLong(),
    width = width?.toLong(),
    mimetype = mimetype,
    size = size?.toLong()
)

fun ThumbnailInfo.map(): RustThumbnailInfo = RustThumbnailInfo(
    height = height?.toULong(),
    width = width?.toULong(),
    mimetype = mimetype,
    size = size?.toULong()
)
