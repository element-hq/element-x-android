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
    private var media: Media? = null
    private val playerMediaPath
        get() = mediaPlayer.state.value.mediaId

    val state: Flow<State> = mediaPlayer.state.map { state ->
        val media = media
        if (media == null || media.path != state.mediaId) {
            return@map State.NotPlaying
        }

        State(
            isPlaying = state.isPlaying,
            curPositionMs = state.currentPosition,
            durationMs = state.duration,
        )
    }.distinctUntilChanged()

    fun setMedia(mediaPath: String, mimeType: String) {
        media = Media(mediaPath, mimeType)
        mediaPlayer.acquireControl(
            uri = mediaPath,
            mediaId = mediaPath,
            mimeType = mimeType,
        )
    }


    /**
     * Start playing from the current position.
     *
     * @param mediaPath The path to the media to be played.
     * @param mimeType The mime type of the media file.
     */
    fun play(mediaPath: String, mimeType: String) {
        if (mediaPath != playerMediaPath) {
            setMedia(mediaPath, mimeType)
        }

        mediaPlayer.play()
    }

    /**
     * Pause playback.
     */
    fun pause() {
        if (media?.path != playerMediaPath) return

        mediaPlayer.pause()
    }

    /**
     * Seek playback to a position.
     *
     * If the media is not loaded by this player
     *
     * @param
     */
    fun seekTo(positionPercent: Float) {
        val media = media ?: return
        if (media.path != playerMediaPath) {
            setMedia(media.path, media.mimeType)
        }

        val durationMs = mediaPlayer.state.value.duration
        if (mediaPlayer.state.value.duration <= 0L) return

        mediaPlayer.seekTo((positionPercent * durationMs).toLong())
    }

    data class State(
        /**
         * Whether this player is currently playing.
         */
        val isPlaying: Boolean,
        /**
         * The elapsed time of this player in milliseconds.
         */
        val curPositionMs: Long,
        /**
         * The duration of this player in milliseconds.
         */
        val durationMs: Long,
    ) {
        companion object {
            val NotPlaying = State(
                isPlaying = false,
                curPositionMs = 0L,
                durationMs = 0L,
            )
        }

        val curPositionPercent = if (durationMs > 0 && durationMs >= curPositionMs) {
            curPositionMs.toFloat() / durationMs.toFloat()
        } else {
            0f
        }
    }
}

private data class Media(
    val path: String,
    val mimeType: String,
)
