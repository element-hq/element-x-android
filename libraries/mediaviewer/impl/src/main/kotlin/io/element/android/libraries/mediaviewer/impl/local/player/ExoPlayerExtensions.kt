/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.player

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

fun ExoPlayer.togglePlay() {
    if (isPlaying) {
        pause()
    } else {
        if (playbackState == Player.STATE_ENDED) {
            seekTo(0)
        } else {
            play()
        }
    }
}

fun ExoPlayer.seekToEnsurePlaying(positionMs: Long) {
    if (isPlaying.not()) {
        play()
    }
    seekTo(positionMs)
}
