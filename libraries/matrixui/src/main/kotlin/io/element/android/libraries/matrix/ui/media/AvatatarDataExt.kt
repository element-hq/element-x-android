/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media

import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.media.MediaSource

/**
 * The size in pixel of the thumbnail to generate for the avatar.
 * This is not the size of the avatar displayed in the UI but the size to get from the servers.
 * Servers SHOULD produce thumbnails with the following dimensions and methods:
 *
 * 32x32, crop
 * 96x96, crop
 * 320x240, scale
 * 640x480, scale
 * 800x600, scale
 *
 * Let's always use the same size so coil caching works properly.
 */
const val AVATAR_THUMBNAIL_SIZE_IN_PIXEL = 240L

internal fun AvatarData.toMediaRequestData(): MediaRequestData {
    return MediaRequestData(
        source = url?.let { MediaSource(it) },
        kind = MediaRequestData.Kind.Thumbnail(AVATAR_THUMBNAIL_SIZE_IN_PIXEL)
    )
}
