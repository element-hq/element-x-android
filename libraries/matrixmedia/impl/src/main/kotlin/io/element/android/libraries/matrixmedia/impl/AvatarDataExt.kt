/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrixmedia.impl

import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrixmedia.api.AVATAR_THUMBNAIL_SIZE_IN_PIXEL
import io.element.android.libraries.matrixmedia.api.MediaRequestData

internal fun AvatarData.toMediaRequestData(): MediaRequestData {
    return MediaRequestData(
        source = url?.let { MediaSource(it) },
        kind = MediaRequestData.Kind.Thumbnail(AVATAR_THUMBNAIL_SIZE_IN_PIXEL)
    )
}
