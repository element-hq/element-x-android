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

package io.element.android.libraries.mediaplayer.api

import io.element.android.libraries.matrix.api.core.EventId
import kotlinx.coroutines.flow.StateFlow

/**
 * A media player for Element X.
 */
interface MediaPlayer : AutoCloseable {

    /**
     * The current state of the player.
     */
    val state: StateFlow<State>

    /**
     * Acquires control of the player with a given media file.
     *
     * @param uri the uri of the media.
     * @param mediaId the unique id of the media.
     * @param mimeType the mime type of the media.
     * @param playWhenReady whether to start playing the given media.
     */
    fun setMedia(
        uri: String,
        mediaId: String,
        mimeType: String,
        playWhenReady: Boolean,
    )

    /**
     * Plays the current media.
     */
    fun play()

    /**
     * Pauses the current media.
     */
    fun pause()

    /**
     * Seeks the current media to the given position.
     */
    fun seekTo(positionMs: Long)

    /**
     * Releases any resources associated with this player.
     */
    override fun close()

    data class State(
        /**
         * Whether the player is currently playing.
         */
        val playState: PlayState,
        /**
         * The id of the media which is currently playing.
         *
         * NB: This is usually the string representation of the [EventId] of the event
         * which contains the media.
         */
        val mediaId: String?,
        /**
         * The current position of the player.
         */
        val currentPosition: Long,
        /**
         * The duration of the current content.
         */
        val duration: Long,
    ) {
        val isPlaying get() = playState == PlayState.Playing
    }

    enum class PlayState {
        Playing,
        Paused,
        Stopped,
    }
}
