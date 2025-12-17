/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.voicemessages.composer

import dev.zacsweers.metro.Inject
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.mediaplayer.api.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * A media player for the voice message composer.
 *
 * @param mediaPlayer The [MediaPlayer] to use.
 * @param sessionCoroutineScope
 */
@Inject
class VoiceMessageComposerPlayer(
    private val mediaPlayer: MediaPlayer,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
) {
    companion object {
        const val MIME_TYPE = MimeTypes.Ogg
    }

    private var mediaPath: String? = null

    private var seekJob: Job? = null
    private val seekingTo = MutableStateFlow<Float?>(null)

    val state: Flow<State> = combine(mediaPlayer.state, seekingTo) { state, seekingTo ->
        state to seekingTo
    }.scan(InternalState.NotLoaded) { prevState, (state, seekingTo) ->
        if (mediaPath == null || mediaPath != state.mediaId) {
            return@scan InternalState.NotLoaded
        }

        InternalState(
            playState = calcPlayState(prevState.playState, seekingTo, state),
            currentPosition = state.currentPosition,
            duration = state.duration,
            seekingTo = seekingTo,
        )
    }.map {
        State(
            playState = it.playState,
            currentPosition = it.currentPosition,
            progress = calcProgress(it),
        )
    }.distinctUntilChanged()

    /**
     * Set the voice message to be played.
     */
    suspend fun setMedia(mediaPath: String) {
        this.mediaPath = mediaPath
        mediaPlayer.setMedia(
            uri = mediaPath,
            mediaId = mediaPath,
            mimeType = MIME_TYPE,
        )
    }

    /**
     * Start playing from the current position.
     *
     * Call [setMedia] before calling this method.
     */
    suspend fun play() {
        val mediaPath = this.mediaPath
        if (mediaPath == null) {
            Timber.e("Set media before playing")
            return
        }

        mediaPlayer.ensureMediaReady(mediaPath)

        mediaPlayer.play()
    }

    /**
     * Pause playback.
     */
    fun pause() {
        if (mediaPath == mediaPlayer.state.value.mediaId) {
            mediaPlayer.pause()
        }
    }

    /**
     * Seek to a given position in the current media.
     *
     * Call [setMedia] before calling this method.
     *
     * @param position The position to seek to between 0 and 1.
     */
    suspend fun seek(position: Float) {
        val mediaPath = this.mediaPath
        if (mediaPath == null) {
            Timber.e("Set media before seeking")
            return
        }

        seekJob?.cancelAndJoin()
        seekingTo.value = position
        seekJob = sessionCoroutineScope.launch {
            val mediaState = mediaPlayer.ensureMediaReady(mediaPath)
            val duration = mediaState.duration ?: return@launch
            val positionMs = (duration * position).toLong()
            mediaPlayer.seekTo(positionMs)
        }.apply {
            invokeOnCompletion {
                seekingTo.value = null
            }
        }
    }

    private suspend fun MediaPlayer.ensureMediaReady(mediaPath: String): MediaPlayer.State {
        val state = state.value
        if (state.mediaId == mediaPath && state.isReady) {
            return state
        }

        return setMedia(
            uri = mediaPath,
            mediaId = mediaPath,
            mimeType = MIME_TYPE,
        )
    }

    private fun calcPlayState(prevPlayState: PlayState, seekingTo: Float?, state: MediaPlayer.State): PlayState {
        if (state.mediaId == null || state.mediaId != mediaPath) {
            return PlayState.Stopped
        }

        // If we were stopped and the player didn't start playing or seeking, we are still stopped.
        if (prevPlayState == PlayState.Stopped && !state.isPlaying && seekingTo == null) {
            return PlayState.Stopped
        }

        return if (state.isPlaying) {
            PlayState.Playing
        } else {
            PlayState.Paused
        }
    }

    private fun calcProgress(state: InternalState): Float {
        if (state.seekingTo != null) {
            return state.seekingTo
        }

        if (state.playState == PlayState.Stopped) {
            return 0f
        }

        if (state.duration == null) {
            return 0f
        }

        return (state.currentPosition.toFloat() / state.duration.toFloat())
            .coerceAtMost(1f) // Current position may exceed reported duration
    }

    /**
     * @property playState Whether this player is currently playing. See [PlayState].
     * @property currentPosition The elapsed time of this player in milliseconds.
     * @property progress The progress of this player between 0 and 1.
     */
    data class State(
        val playState: PlayState,
        val currentPosition: Long,
        val progress: Float,
    ) {
        companion object {
            val Initial = State(
                playState = PlayState.Stopped,
                currentPosition = 0L,
                progress = 0f,
            )
        }

        /**
         * Whether this player is currently playing.
         */
        val isPlaying get() = this.playState == PlayState.Playing

        /**
         * Whether this player is currently stopped.
         */
        val isStopped get() = this.playState == PlayState.Stopped
    }

    enum class PlayState {
        /**
         * The player is stopped, i.e. it has just been initialised.
         */
        Stopped,

        /**
         * The player is playing.
         */
        Playing,

        /**
         * The player has been paused. The player can also enter the paused state after seeking to a position.
         */
        Paused,
    }

    private data class InternalState(
        val playState: PlayState,
        val currentPosition: Long,
        val duration: Long?,
        val seekingTo: Float?,
    ) {
        companion object {
            val NotLoaded = InternalState(
                playState = PlayState.Stopped,
                currentPosition = 0L,
                duration = null,
                seekingTo = null,
            )
        }
    }
}
