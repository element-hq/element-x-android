/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.media

import io.element.android.libraries.matrix.api.media.FileInfo
import org.matrix.rustcomponents.sdk.FileInfo as RustFileInfo

fun RustFileInfo.map(): FileInfo = FileInfo(
    mimetype = mimetype,
    size = size?.toLong(),
    thumbnailInfo = thumbnailInfo?.map(),
    thumbnailSource = thumbnailSource?.map()
)

fun FileInfo.map(): RustFileInfo = RustFileInfo(
    mimetype = mimetype,
    size = size?.toULong(),
    thumbnailInfo = thumbnailInfo?.map(),
    thumbnailSource = null
)
