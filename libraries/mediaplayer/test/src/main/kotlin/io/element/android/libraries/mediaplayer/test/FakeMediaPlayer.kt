/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaplayer.test

import io.element.android.libraries.mediaplayer.api.MediaPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Fake implementation of [MediaPlayer] for testing purposes.
 */
class FakeMediaPlayer(
    private val fakeTotalDurationMs: Long = 10_000L,
    private val fakePlayedDurationMs: Long = 1000L,
) : MediaPlayer {
    private val _state = MutableStateFlow(
        MediaPlayer.State(
            isReady = false,
            isPlaying = false,
            isEnded = false,
            mediaId = null,
            currentPosition = 0L,
            duration = null
        )
    )

    override val state: StateFlow<MediaPlayer.State> = _state.asStateFlow()

    override suspend fun setMedia(
        uri: String,
        mediaId: String,
        mimeType: String,
        startPositionMs: Long,
    ): MediaPlayer.State {
        _state.update {
            it.copy(
                isReady = false,
                isPlaying = false,
                isEnded = false,
                mediaId = mediaId,
                currentPosition = startPositionMs,
                duration = null,
            )
        }
        delay(1) // fake delay to simulate prepare() call.
        _state.update {
            it.copy(
                isReady = true,
                duration = fakeTotalDurationMs,
            )
        }
        return _state.value
    }

    override fun play() {
        _state.update {
            val newPosition = it.currentPosition + fakePlayedDurationMs
            if (newPosition < fakeTotalDurationMs) {
                it.copy(
                    isPlaying = true,
                    currentPosition = newPosition,
                )
            } else {
                it.copy(
                    isReady = false,
                    isPlaying = false,
                    isEnded = true,
                    currentPosition = fakeTotalDurationMs,
                )
            }
        }
    }

    override fun pause() {
        _state.update {
            it.copy(
                isPlaying = false,
            )
        }
    }

    override fun seekTo(positionMs: Long) {
        _state.update {
            it.copy(
                currentPosition = positionMs,
            )
        }
    }

    override fun close() {
        // no-op
    }
}
