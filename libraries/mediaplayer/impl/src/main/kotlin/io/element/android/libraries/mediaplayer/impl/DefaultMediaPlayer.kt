/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaplayer.impl

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.audio.api.AudioFocus
import io.element.android.libraries.audio.api.AudioFocusRequester
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.mediaplayer.api.MediaPlayer
import kotlinx.coroutines.CoroutineScope
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
import kotlin.time.Duration.Companion.seconds

/**
 * Default implementation of [MediaPlayer] backed by a [SimplePlayer].
 */
@ContributesBinding(RoomScope::class)
@SingleIn(RoomScope::class)
class DefaultMediaPlayer(
    private val player: SimplePlayer,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
    private val audioFocus: AudioFocus,
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
                job = sessionCoroutineScope.launch { updateCurrentPosition() }
            } else {
                audioFocus.releaseAudioFocus()
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
        // Must pause here otherwise if the player was playing it would keep on playing the new media item.
        player.pause()
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
        audioFocus.requestAudioFocus(
            requester = AudioFocusRequester.VoiceMessage,
            onFocusLost = {
                if (player.isPlaying()) {
                    player.pause()
                }
            },
        )
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
            }
        }
        player.play()
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
