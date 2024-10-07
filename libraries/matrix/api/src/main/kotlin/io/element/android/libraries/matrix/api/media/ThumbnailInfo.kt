/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.media

data class ThumbnailInfo(
    val height: Long?,
    val width: Long?,
    val mimetype: String?,
    val size: Long?
)
