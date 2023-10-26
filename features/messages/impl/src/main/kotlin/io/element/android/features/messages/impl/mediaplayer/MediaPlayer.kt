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

package io.element.android.features.messages.impl.mediaplayer

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.core.EventId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A media player for Element X.
 */
interface MediaPlayer : AutoCloseable {

    /**
     * The current state of the player.
     */
    val state: StateFlow<State>

    /**
     * Acquires control of the player and starts playing the given media.
     */
    fun acquireControlAndPlay(
        uri: String,
        mediaId: String,
        mimeType: String,
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
        val isPlaying: Boolean,
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
    )
}

/**
 * Default implementation of [MediaPlayer] backed by a [SimplePlayer].
 */
@ContributesBinding(RoomScope::class)
@SingleIn(RoomScope::class)
class MediaPlayerImpl @Inject constructor(
    private val player: SimplePlayer,
) : MediaPlayer {

    private val listener = object : SimplePlayer.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.update {
                it.copy(
                    currentPosition = player.currentPosition,
                    isPlaying = isPlaying,
                )
            }
            if (isPlaying) {
                job = scope.launch { updateCurrentPosition() }
            } else {
                job?.cancel()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?) {
            _state.update {
                it.copy(
                    currentPosition = player.currentPosition,
                    mediaId = mediaItem?.mediaId,
                )
            }
        }
    }

    init {
        player.addListener(listener)
    }

    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private var job: Job? = null

    private val _state = MutableStateFlow(MediaPlayer.State(false, null, 0L))

    override val state: StateFlow<MediaPlayer.State> = _state.asStateFlow()

    override fun acquireControlAndPlay(uri: String, mediaId: String, mimeType: String) {
        player.clearMediaItems()
        player.setMediaItem(
            MediaItem.Builder()
                .setUri(uri)
                .setMediaId(mediaId)
                .setMimeType(mimeType)
                .build()
        )
        player.prepare()
        player.play()
    }

    override fun play() {
        if (player.playbackState == Player.STATE_ENDED) {
            // There's a bug with some ogg files that somehow report to
            // have no duration.
            // With such files, once playback has ended once, calling
            // player.seekTo(0) and then player.play() results in the
            // player starting and stopping playing immediately effectively
            // playing no sound.
            // This is a workaround which will reload the media file.
            player.getCurrentMediaItem()?.let {
                player.setMediaItem(it)
                player.prepare()
                player.play()
            }
        } else {
            player.play()
        }
    }

    override fun pause() {
        player.pause()
    }

    override fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    override fun close() {
        player.release()
    }

    private suspend fun updateCurrentPosition() {
        while (true) {
            if (!_state.value.isPlaying) return
            delay(100)
            _state.update {
                it.copy(currentPosition = player.currentPosition)
            }
        }
    }
}
