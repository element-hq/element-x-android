/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
