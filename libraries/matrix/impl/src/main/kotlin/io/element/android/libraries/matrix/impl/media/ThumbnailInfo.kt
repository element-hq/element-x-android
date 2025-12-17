/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
