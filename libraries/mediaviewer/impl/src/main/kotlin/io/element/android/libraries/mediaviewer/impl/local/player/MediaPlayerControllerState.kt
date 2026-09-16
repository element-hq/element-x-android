/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.player

import androidx.annotation.FloatRange

data class MediaPlayerControllerState(
    val isVisible: Boolean,
    val isPlaying: Boolean,
    val isReady: Boolean,
    val progressInMillis: Long,
    val durationInMillis: Long,
    val canMute: Boolean,
    val isMuted: Boolean,
    val seekingToMillis: Long?,
) {
    /**
     * The progress in milliseconds to display. When [seekingToMillis] is non-null (during a seek operation),
     * this returns the target seek position. Once the player catches up to the seek position,
     * [seekingToMillis] is cleared (set to null) and this returns [progressInMillis] again.
     */
    val displayProgressInMillis: Long
        get() = seekingToMillis ?: progressInMillis

    @FloatRange(from = 0.0, to = 1.0)
    val progressAsFloat = (displayProgressInMillis.toFloat() / durationInMillis.toFloat()).coerceIn(0f, 1f)
}
