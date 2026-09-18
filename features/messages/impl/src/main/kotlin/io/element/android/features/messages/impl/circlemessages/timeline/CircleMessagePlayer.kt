/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.circlemessages.timeline

import android.content.Context
import android.graphics.Bitmap
import android.view.TextureView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.audio.api.AudioFocus
import io.element.android.libraries.audio.api.AudioFocusRequester
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.annotations.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.File

interface CircleMessagePlayer {
    data class SavedPlayback(
        val positionMs: Long,
        val durationMs: Long?,
        val isEnded: Boolean,
        val lastFrame: Bitmap?,
    )

    data class State(
        val mediaId: String?,
        val isPlaying: Boolean,
        val isEnded: Boolean,
        val currentPositionMs: Long,
        val durationMs: Long?,
        val lastFrame: Bitmap?,
        val savedPlayback: Map<String, SavedPlayback> = emptyMap(),
    )

    val state: StateFlow<State>
    val exoPlayer: ExoPlayer
    fun play(mediaId: String, file: File)
    fun pause()
    fun tick()
    fun bindPlayerView(view: PlayerView)
    fun unbindPlayerView(view: PlayerView)
}

@SingleIn(RoomScope::class)
@ContributesBinding(RoomScope::class)
class DefaultCircleMessagePlayer(
    @ApplicationContext context: Context,
    private val audioFocus: AudioFocus,
) : CircleMessagePlayer {
    override val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        repeatMode = Player.REPEAT_MODE_OFF
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (exoPlayer.playbackState == Player.STATE_ENDED) return
                if (!isPlaying && exoPlayer.playWhenReady) return
                _state.update { current ->
                    if (current.mediaId == null) current else current.copy(isPlaying = isPlaying)
                }
            }

            override fun onRenderedFirstFrame() {
                boundPlayerView?.alpha = 1f
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState != Player.STATE_ENDED) return
                val mediaId = _state.value.mediaId ?: return
                val duration = exoPlayer.duration.takeIf { it > 0 }
                captureFrame()
                audioFocus.releaseAudioFocus()
                _state.update {
                    val saved = CircleMessagePlayer.SavedPlayback(
                        positionMs = duration ?: it.currentPositionMs,
                        durationMs = duration ?: it.durationMs,
                        isEnded = true,
                        lastFrame = lastFrame ?: it.lastFrame,
                    )
                    it.copy(
                        isEnded = true,
                        isPlaying = false,
                        durationMs = duration ?: it.durationMs,
                        currentPositionMs = duration ?: it.currentPositionMs,
                        lastFrame = lastFrame ?: it.lastFrame,
                        savedPlayback = it.savedPlayback + (mediaId to saved),
                    )
                }
            }
        })
    }

    private var boundPlayerView: PlayerView? = null
    private var lastFrame: Bitmap? = null
    private var playbackGeneration: Int = 0

    private val _state = MutableStateFlow(
        CircleMessagePlayer.State(
            mediaId = null,
            isPlaying = false,
            isEnded = false,
            currentPositionMs = 0,
            durationMs = null,
            lastFrame = null,
        )
    )
    override val state: StateFlow<CircleMessagePlayer.State> = _state

    override fun play(mediaId: String, file: File) {
        playbackGeneration += 1
        val generation = playbackGeneration
        audioFocus.requestAudioFocus(AudioFocusRequester.CircleMessage) {
            if (playbackGeneration == generation) {
                pause()
            }
        }
        val previousId = _state.value.mediaId
        val switchingTrack = previousId != mediaId
        if (switchingTrack) {
            if (previousId != null) {
                captureFrame()
                persistPlayback(previousId, ended = _state.value.isEnded || exoPlayer.playbackState == Player.STATE_ENDED)
                detachBoundView()
            }
            lastFrame = null
            val resume = _state.value.savedPlayback[mediaId]
            exoPlayer.setMediaItem(MediaItem.fromUri(file.toURI().toString()))
            exoPlayer.prepare()
            val resumePosition = resume?.takeIf { !it.isEnded }?.positionMs ?: 0L
            if (resumePosition > 0) {
                exoPlayer.seekTo(resumePosition)
            }
            lastFrame = resume?.lastFrame
            _state.update {
                it.copy(
                    mediaId = mediaId,
                    isEnded = false,
                    isPlaying = true,
                    currentPositionMs = resumePosition,
                    durationMs = resume?.durationMs ?: it.durationMs,
                    lastFrame = lastFrame,
                )
            }
        } else {
            val nearEnd = exoPlayer.duration > 0 &&
                exoPlayer.currentPosition >= exoPlayer.duration - 80
            if (_state.value.isEnded || exoPlayer.playbackState == Player.STATE_ENDED || nearEnd) {
                exoPlayer.seekTo(0)
                _state.update { it.copy(currentPositionMs = 0, isEnded = false) }
            }
            _state.update { it.copy(isPlaying = true, isEnded = false) }
        }
        exoPlayer.playWhenReady = true
        exoPlayer.play()
    }

    override fun pause() {
        val mediaId = _state.value.mediaId
        captureFrame()
        exoPlayer.pause()
        audioFocus.releaseAudioFocus()
        val position = exoPlayer.currentPosition
        val duration = exoPlayer.duration.takeIf { duration -> duration > 0 }
        _state.update {
            val saved = mediaId?.let { id ->
                it.savedPlayback + (id to CircleMessagePlayer.SavedPlayback(
                    positionMs = position,
                    durationMs = duration ?: it.durationMs,
                    isEnded = it.isEnded,
                    lastFrame = lastFrame ?: it.lastFrame,
                ))
            } ?: it.savedPlayback
            it.copy(
                isPlaying = false,
                currentPositionMs = position,
                durationMs = duration ?: it.durationMs,
                lastFrame = lastFrame ?: it.lastFrame,
                savedPlayback = saved,
            )
        }
    }

    override fun tick() {
        if (!_state.value.isPlaying) return
        _state.update {
            it.copy(
                currentPositionMs = exoPlayer.currentPosition,
                durationMs = exoPlayer.duration.takeIf { duration -> duration > 0 } ?: it.durationMs,
            )
        }
    }

    override fun bindPlayerView(view: PlayerView) {
        if (boundPlayerView === view) {
            view.player = exoPlayer
            return
        }
        detachBoundView()
        boundPlayerView = view
        view.alpha = 0f
        view.player = exoPlayer
    }

    override fun unbindPlayerView(view: PlayerView) {
        if (boundPlayerView !== view) return
        val mediaId = _state.value.mediaId
        captureFrame()
        if (view.player === exoPlayer) {
            view.player = null
        }
        boundPlayerView = null
        if (mediaId != null && _state.value.mediaId == mediaId) {
            persistPlayback(mediaId, ended = _state.value.isEnded)
            _state.update { it.copy(lastFrame = lastFrame ?: it.lastFrame) }
        }
    }

    private fun detachBoundView() {
        boundPlayerView?.let { previous ->
            if (previous.player === exoPlayer) {
                previous.player = null
            }
        }
        boundPlayerView = null
    }

    private fun persistPlayback(mediaId: String, ended: Boolean) {
        val position = if (ended) {
            exoPlayer.duration.takeIf { it > 0 } ?: _state.value.currentPositionMs
        } else {
            exoPlayer.currentPosition
        }
        val duration = exoPlayer.duration.takeIf { it > 0 } ?: _state.value.durationMs
        val saved = CircleMessagePlayer.SavedPlayback(
            positionMs = position,
            durationMs = duration,
            isEnded = ended,
            lastFrame = lastFrame ?: _state.value.lastFrame,
        )
        _state.update { it.copy(savedPlayback = it.savedPlayback + (mediaId to saved)) }
    }

    private fun captureFrame() {
        val textureView = boundPlayerView?.videoSurfaceView as? TextureView ?: return
        val bitmap = textureView.bitmap ?: return
        lastFrame = bitmap
    }
}
