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

package io.element.android.features.messages.impl.voicemessages.timeline

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.mediaplayer.api.MediaPlayer
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
         * @param body The body of the voice message.
         */
        fun create(
            eventId: EventId?,
            mediaSource: MediaSource,
            mimeType: String?,
            body: String?,
        ): VoiceMessagePlayer
    }

    /**
     * The current state of this player.
     */
    val state: Flow<State>

    /**
     * Starts playing from the beginning
     * acquiring control of the underlying [MediaPlayer].
     * If already in control of the underlying [MediaPlayer], starts playing from the
     * current position.
     *
     * Will suspend whilst the media file is being downloaded.
     */
    suspend fun play(): Result<Unit>

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
         * Whether this player is currently playing.
         */
        val isPlaying: Boolean,
        /**
         * Whether this player has control of the underlying [MediaPlayer].
         */
        val isMyMedia: Boolean,
        /**
         * The elapsed time of this player in milliseconds.
         */
        val currentPosition: Long,
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
    body: String?,
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
            body: String?,
        ): DefaultVoiceMessagePlayer = DefaultVoiceMessagePlayer(
            mediaPlayer = mediaPlayer,
            voiceMessageMediaRepoFactory = voiceMessageMediaRepoFactory,
            eventId = eventId,
            mediaSource = mediaSource,
            mimeType = mimeType,
            body = body,
        )
    }

    private val repo = voiceMessageMediaRepoFactory.create(
        mediaSource = mediaSource,
        mimeType = mimeType,
        body = body
    )

    override val state: Flow<VoiceMessagePlayer.State> = mediaPlayer.state.map { state ->
        VoiceMessagePlayer.State(
            isPlaying = state.mediaId.isMyTrack() && state.isPlaying,
            isMyMedia = state.mediaId.isMyTrack(),
            currentPosition = if (state.mediaId.isMyTrack()) state.currentPosition else 0L
        )
    }.distinctUntilChanged()

    override suspend fun play(): Result<Unit> = if (inControl()) {
        mediaPlayer.play()
        Result.success(Unit)
    } else {
        if (eventId != null) {
            repo.getMediaFile().mapCatching { mediaFile ->
                mediaPlayer.acquireControlAndPlay(
                    uri = mediaFile.path,
                    mediaId = eventId.value,
                    mimeType = "audio/ogg" // Files in the voice cache have no extension so we need to set the mime type manually.
                )
            }
        } else {
            Result.failure(IllegalStateException("Cannot play a voice message with no eventId"))
        }
    }

    override fun pause() {
        ifInControl {
            mediaPlayer.pause()
        }
    }

    override fun seekTo(positionMs: Long) {
        ifInControl {
            mediaPlayer.seekTo(positionMs)
        }
    }

    private fun String?.isMyTrack(): Boolean = if (eventId == null) false else this == eventId.value

    private inline fun ifInControl(block: () -> Unit) {
        if (inControl()) block()
    }

    private fun inControl(): Boolean = mediaPlayer.state.value.mediaId.isMyTrack()
}
