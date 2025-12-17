/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.player

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class MediaPlayerControllerStateProvider : PreviewParameterProvider<MediaPlayerControllerState> {
    override val values: Sequence<MediaPlayerControllerState> = sequenceOf(
        aMediaPlayerControllerState(),
        aMediaPlayerControllerState(
            isPlaying = true,
            progressInMillis = 59_000,
            durationInMillis = 83_000,
            isMuted = true,
        ),
        aMediaPlayerControllerState(
            canMute = false,
        ),
    )
}

private fun aMediaPlayerControllerState(
    isVisible: Boolean = true,
    isPlaying: Boolean = false,
    isReady: Boolean = false,
    progressInMillis: Long = 0,
    // Default to 1 minute and 23 seconds
    durationInMillis: Long = 83_000,
    canMute: Boolean = true,
    isMuted: Boolean = false,
) = MediaPlayerControllerState(
    isVisible = isVisible,
    isPlaying = isPlaying,
    isReady = isReady,
    progressInMillis = progressInMillis,
    durationInMillis = durationInMillis,
    canMute = canMute,
    isMuted = isMuted,
)
