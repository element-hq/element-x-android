/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media

import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.media.MediaSource
import kotlin.math.roundToLong

internal fun AvatarData.toMediaRequestData(): MediaRequestData {
    return MediaRequestData(
        source = url?.let { MediaSource(it) },
        kind = MediaRequestData.Kind.Thumbnail(size.dp.value.roundToLong())
    )
}
