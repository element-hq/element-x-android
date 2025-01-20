/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.player

import androidx.annotation.FloatRange

data class MediaPlayerControllerState(
    val isVisible: Boolean,
    val isPlaying: Boolean,
    val progressInMillis: Long,
    val durationInMillis: Long,
    val canMute: Boolean,
    val isMuted: Boolean,
) {
    @FloatRange(from = 0.0, to = 1.0)
    val progressAsFloat = (progressInMillis.toFloat() / durationInMillis.toFloat()).coerceIn(0f, 1f)
}
