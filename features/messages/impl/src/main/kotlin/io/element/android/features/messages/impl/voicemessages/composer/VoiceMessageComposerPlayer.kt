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
import timber.log.Timber
import javax.inject.Inject

/**
 * A media player for the voice message composer.
 *
 * @param mediaPlayer The [MediaPlayer] to use.
 */
class VoiceMessageComposerPlayer @Inject constructor(
    private val mediaPlayer: MediaPlayer,
) {
    companion object {
        const val MIME_TYPE = "audio/ogg"
    }

    private var mediaPath: String? = null
    private val curPlayingMediaId
        get() = mediaPlayer.state.value.mediaId

    val state: Flow<State> = mediaPlayer.state.map { state ->
        if (mediaPath == null || mediaPath != state.mediaId) {
            return@map State.NotLoaded
        }

        State(
            playState = state.playState,
            currentPosition = state.currentPosition,
            duration = state.duration,
        )
    }.distinctUntilChanged()

    /**
     * Set the voice message to be played.
     */
    fun setMedia(mediaPath: String) {
        this.mediaPath = mediaPath
        mediaPlayer.setMedia(
            uri = mediaPath,
            mediaId = mediaPath,
            mimeType = MIME_TYPE,
            playWhenReady = false,
        )
    }

    /**
     * Start playing from the current position.
     *
     * Call [setMedia] before calling this method.
     */
    fun play() {
        val mediaPath = this.mediaPath

        if (mediaPath == null) {
            Timber.e("Set media before playing")
            return
        }

        if (mediaPath == curPlayingMediaId) {
            mediaPlayer.play()
        } else {
            mediaPlayer.setMedia(
                uri = mediaPath,
                mediaId = mediaPath,
                mimeType = MIME_TYPE,
                playWhenReady = true
            )
        }
    }

    /**
     * Pause playback.
     */
    fun pause() {
        if (mediaPath == curPlayingMediaId) {
            mediaPlayer.pause()
        }
    }

    data class State(
        /**
         * Whether this player is currently playing, paused or stopped.
         */
        val playState: MediaPlayer.PlayState,
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
                playState = MediaPlayer.PlayState.Stopped,
                currentPosition = 0L,
                duration = 0L,
            )
        }

        val isLoaded get() = this != NotLoaded

        val isPlaying get() = this.playState == MediaPlayer.PlayState.Playing

        val isStopped get() = this.playState == MediaPlayer.PlayState.Stopped

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
