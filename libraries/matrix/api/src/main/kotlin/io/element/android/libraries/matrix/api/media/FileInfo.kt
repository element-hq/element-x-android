/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.media

data class FileInfo(
    val mimetype: String?,
    val size: Long?,
    val thumbnailInfo: ThumbnailInfo?,
    val thumbnailSource: MediaSource?
)
