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

package io.element.android.libraries.mediaplayer.impl

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.mediaplayer.api.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

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
                    duration = duration,
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
                    duration = duration,
                    mediaId = mediaItem?.mediaId,
                )
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _state.update {
                it.copy(
                    isReady = playbackState == Player.STATE_READY,
                    isEnded = playbackState == Player.STATE_ENDED,
                    currentPosition = player.currentPosition,
                    duration = duration,
                )
            }
        }
    }

    init {
        player.addListener(listener)
    }

    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private var job: Job? = null

    private val _state = MutableStateFlow(
        MediaPlayer.State(
            isReady = false,
            isPlaying = false,
            isEnded = false,
            mediaId = null,
            currentPosition = 0L,
            duration = null,
        )
    )

    override val state: StateFlow<MediaPlayer.State> = _state.asStateFlow()

    @OptIn(FlowPreview::class)
    override suspend fun setMedia(
        uri: String,
        mediaId: String,
        mimeType: String,
        startPositionMs: Long,
    ): MediaPlayer.State {
        player.pause() // Must pause here otherwise if the player was playing it would keep on playing the new media item.
        player.clearMediaItems()
        player.setMediaItem(
            MediaItem.Builder()
                .setUri(uri)
                .setMediaId(mediaId)
                .setMimeType(mimeType)
                .build(),
            startPositionMs,
        )
        player.prepare()
        // Will throw TimeoutCancellationException if the player is not ready after 1 second.
        return state.timeout(1.seconds).first { it.isReady && it.mediaId == mediaId }
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
                player.setMediaItem(it, 0)
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
        _state.update {
            it.copy(currentPosition = player.currentPosition)
        }
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

    private val duration: Long?
        get() = player.duration.let {
            if (it == C.TIME_UNSET) null else it
        }
}
