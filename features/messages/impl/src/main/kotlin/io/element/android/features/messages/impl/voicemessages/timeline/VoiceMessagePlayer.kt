/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.voicemessages.timeline

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaplayer.api.MediaPlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import java.io.File
import javax.inject.Inject

/**
 * A media player specialized in playing a single voice message.
 */
interface VoiceMessagePlayer {
    fun interface Factory {
        /**
         * Creates a [VoiceMessagePlayer].
         *
         * NB: Different voice messages can use the same content uri (e.g. in case of
         * a forward of a voice message),
         * therefore the mxc:// uri in [mediaSource] is not enough to uniquely identify
         * a voice message. This is why we must provide the eventId as well.
         *
         * @param eventId The eventId of the voice message event.
         * @param mediaSource The media source of the voice message.
         * @param mimeType The mime type of the voice message.
         * @param filename The filename of the voice message.
         */
        fun create(
            eventId: EventId?,
            mediaSource: MediaSource,
            mimeType: String?,
            filename: String?,
        ): VoiceMessagePlayer
    }

    /**
     * The current state of this player.
     */
    val state: Flow<State>

    /**
     * Acquires control of the underlying [MediaPlayer] and prepares it
     * to play the media file.
     *
     * Will suspend whilst the media file is being downloaded and/or
     * the underlying [MediaPlayer] is loading the media file.
     */
    suspend fun prepare(): Result<Unit>

    /**
     * Play the media.
     */
    fun play()

    /**
     * Pause playback.
     */
    fun pause()

    /**
     * Seek to a specific position.
     *
     * @param positionMs The position in milliseconds.
     */
    fun seekTo(positionMs: Long)

    data class State(
        /**
         * Whether the player is ready to play.
         */
        val isReady: Boolean,
        /**
         * Whether this player is currently playing.
         */
        val isPlaying: Boolean,
        /**
         * Whether the player has reached the end of the media.
         */
        val isEnded: Boolean,
        /**
         * The elapsed time of this player in milliseconds.
         */
        val currentPosition: Long,
        /**
         * The duration of the current content, if available.
         */
        val duration: Long?,
    )
}

/**
 * An implementation of [VoiceMessagePlayer] which is backed by a
 * [VoiceMessageMediaRepo] to fetch and cache the media file and
 * which uses a global [MediaPlayer] instance to play the media.
 */
class DefaultVoiceMessagePlayer(
    private val mediaPlayer: MediaPlayer,
    voiceMessageMediaRepoFactory: VoiceMessageMediaRepo.Factory,
    private val eventId: EventId?,
    mediaSource: MediaSource,
    mimeType: String?,
    filename: String?,
) : VoiceMessagePlayer {
    @ContributesBinding(RoomScope::class) // Scoped types can't use @AssistedInject.
    class Factory @Inject constructor(
        private val mediaPlayer: MediaPlayer,
        private val voiceMessageMediaRepoFactory: VoiceMessageMediaRepo.Factory,
    ) : VoiceMessagePlayer.Factory {
        override fun create(
            eventId: EventId?,
            mediaSource: MediaSource,
            mimeType: String?,
            filename: String?,
        ): DefaultVoiceMessagePlayer = DefaultVoiceMessagePlayer(
            mediaPlayer = mediaPlayer,
            voiceMessageMediaRepoFactory = voiceMessageMediaRepoFactory,
            eventId = eventId,
            mediaSource = mediaSource,
            mimeType = mimeType,
            filename = filename,
        )
    }

    private val repo = voiceMessageMediaRepoFactory.create(
        mediaSource = mediaSource,
        mimeType = mimeType,
        filename = filename,
    )

    private var internalState = MutableStateFlow(
        VoiceMessagePlayer.State(
            isReady = false,
            isPlaying = false,
            isEnded = false,
            currentPosition = 0L,
            duration = null
        )
    )

    override val state: Flow<VoiceMessagePlayer.State> = combine(mediaPlayer.state, internalState) { mediaPlayerState, internalState ->
        if (mediaPlayerState.isMyTrack) {
            this.internalState.update {
                it.copy(
                    isReady = mediaPlayerState.isReady,
                    isPlaying = mediaPlayerState.isPlaying,
                    isEnded = mediaPlayerState.isEnded,
                    currentPosition = mediaPlayerState.currentPosition,
                    duration = mediaPlayerState.duration,
                )
            }
        } else {
            this.internalState.update {
                it.copy(
                    isReady = false,
                    isPlaying = false,
                )
            }
        }
        VoiceMessagePlayer.State(
            isReady = internalState.isReady,
            isPlaying = internalState.isPlaying,
            isEnded = internalState.isEnded,
            currentPosition = internalState.currentPosition,
            duration = internalState.duration,
        )
    }.distinctUntilChanged()

    override suspend fun prepare(): Result<Unit> = if (eventId != null) {
        repo.getMediaFile().mapCatching<Unit, File> { mediaFile ->
            val state = internalState.value
            mediaPlayer.setMedia(
                uri = mediaFile.path,
                mediaId = eventId.value,
                // Files in the voice cache have no extension so we need to set the mime type manually.
                mimeType = MimeTypes.Ogg,
                startPositionMs = if (state.isEnded) 0L else state.currentPosition,
            )
        }
    } else {
        Result.failure(IllegalStateException("Cannot acquireControl on a voice message with no eventId"))
    }

    override fun play() {
        if (inControl()) {
            mediaPlayer.play()
        }
    }

    override fun pause() {
        if (inControl()) {
            mediaPlayer.pause()
        }
    }

    override fun seekTo(positionMs: Long) {
        if (inControl()) {
            mediaPlayer.seekTo(positionMs)
        } else {
            internalState.update {
                it.copy(currentPosition = positionMs)
            }
        }
    }

    private val MediaPlayer.State.isMyTrack: Boolean
        get() = if (eventId == null) false else this.mediaId == eventId.value

    private fun inControl(): Boolean = mediaPlayer.state.value.let {
        it.isMyTrack && (it.isReady || it.isEnded)
    }
}
