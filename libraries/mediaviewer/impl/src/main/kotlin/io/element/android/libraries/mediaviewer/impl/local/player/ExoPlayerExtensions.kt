/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.player

import androidx.media3.common.Player

fun Player.togglePlay() {
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

fun Player.seekToEnsurePlaying(positionMs: Long) {
    if (isPlaying.not()) {
        play()
    }
    seekTo(positionMs)
}
