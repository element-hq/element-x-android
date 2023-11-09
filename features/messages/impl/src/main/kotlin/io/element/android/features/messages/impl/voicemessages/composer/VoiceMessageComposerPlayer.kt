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

package io.element.android.features.messages.impl.voicemessages.composer

import io.element.android.libraries.mediaplayer.api.MediaPlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * A media player for the voice message composer.
 *
 * @param mediaPlayer The [MediaPlayer] to use.
 */
class VoiceMessageComposerPlayer @Inject constructor(
    private val mediaPlayer: MediaPlayer,
) {
    private var lastPlayedMediaPath: String? = null
    private val curPlayingMediaId
        get() = mediaPlayer.state.value.mediaId

    val state: Flow<State> = mediaPlayer.state.map { state ->
        if (lastPlayedMediaPath == null || lastPlayedMediaPath != state.mediaId) {
            return@map State.NotLoaded
        }

        State(
            isPlaying = state.isPlaying,
            currentPosition = state.currentPosition,
            duration = state.duration ?: 0L,
        )
    }.distinctUntilChanged()

    /**
     * Start playing from the current position.
     *
     * @param mediaPath The path to the media to be played.
     * @param mimeType The mime type of the media file.
     */
    suspend fun play(mediaPath: String, mimeType: String) {
        if (mediaPath == curPlayingMediaId) {
            mediaPlayer.play()
        } else {
            lastPlayedMediaPath = mediaPath
            mediaPlayer.setMedia(
                uri = mediaPath,
                mediaId = mediaPath,
                mimeType = mimeType,
            )
            mediaPlayer.play()
        }
    }

    /**
     * Pause playback.
     */
    fun pause() {
        if (lastPlayedMediaPath == curPlayingMediaId) {
            mediaPlayer.pause()
        }
    }

    data class State(
        /**
         * Whether this player is currently playing.
         */
        val isPlaying: Boolean,
        /**
         * The elapsed time of this player in milliseconds.
         */
        val currentPosition: Long,
        /**
         * The duration of this player in milliseconds.
         */
        val duration: Long,
    ) {
        companion object {
            val NotLoaded = State(
                isPlaying = false,
                currentPosition = 0L,
                duration = 0L,
            )
        }

        val isLoaded get() = this != NotLoaded

        /**
         * The progress of this player between 0 and 1.
         */
        val progress: Float =
            if (duration == 0L)
                0f
            else
                (currentPosition.toFloat() / duration.toFloat())
                    .coerceAtMost(1f) // Current position may exceed reported duration
    }
}
