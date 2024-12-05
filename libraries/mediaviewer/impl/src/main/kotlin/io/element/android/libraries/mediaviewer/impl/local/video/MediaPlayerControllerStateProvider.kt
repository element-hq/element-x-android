/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.video

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
    )
}

private fun aMediaPlayerControllerState(
    isVisible: Boolean = true,
    isPlaying: Boolean = false,
    progressInMillis: Long = 0,
    // Default to 1 minute and 23 seconds
    durationInMillis: Long = 83_000,
    isMuted: Boolean = false,
) = MediaPlayerControllerState(
    isVisible = isVisible,
    isPlaying = isPlaying,
    progressInMillis = progressInMillis,
    durationInMillis = durationInMillis,
    isMuted = isMuted,
)
