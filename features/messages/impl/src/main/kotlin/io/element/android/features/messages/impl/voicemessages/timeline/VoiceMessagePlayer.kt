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
import io.element.android.features.messages.impl.mediaplayer.MediaPlayer
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
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
         * therefore the media uri is not enough to uniquely identify a voice message.
         * This is why we must provide the eventId as well.
         *
         * @param eventId The id of the voice message event. If null, a dummy
         *        player is returned.
         * @param mediaPath The path to the voice message's media file.
         */
        fun create(eventId: EventId?, mediaPath: String): VoiceMessagePlayer
    }

    /**
     * The current state of this player.
     */
    val state: Flow<State>

    /**
     * Start playing from the beginning acquiring control of the
     * underlying [MediaPlayer].
     */
    fun acquireControlAndPlay()

    /**
     * Start playing from the current position.
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
 * An implementation of [VoiceMessagePlayer] which is backed by a [MediaPlayer]
 * usually shared among different [VoiceMessagePlayer] instances.
 *
 * @param mediaPlayer The [MediaPlayer] to use.
 * @param eventId The id of the voice message event. If null, the player will behave as no-op.
 * @param mediaPath The path to the voice message's media file.
 */
class VoiceMessagePlayerImpl(
    private val mediaPlayer: MediaPlayer,
    private val eventId: EventId?,
    private val mediaPath: String,
) : VoiceMessagePlayer {

    @ContributesBinding(RoomScope::class) // Scoped types can't use @AssistedInject.
    class Factory @Inject constructor(
        private val mediaPlayer: MediaPlayer,
    ) : VoiceMessagePlayer.Factory {
        override fun create(eventId: EventId?, mediaPath: String): VoiceMessagePlayerImpl {
            return VoiceMessagePlayerImpl(
                mediaPlayer = mediaPlayer,
                eventId = eventId,
                mediaPath = mediaPath,
            )
        }
    }

    override val state: Flow<VoiceMessagePlayer.State> = mediaPlayer.state.map { state ->
        VoiceMessagePlayer.State(
            isPlaying = state.mediaId.isMyTrack() && state.isPlaying,
            isMyMedia = state.mediaId.isMyTrack(),
            currentPosition = if (state.mediaId.isMyTrack()) state.currentPosition else 0L
        )
    }.distinctUntilChanged()

    override fun acquireControlAndPlay() {
        eventId?.let { eventId ->
            mediaPlayer.acquireControlAndPlay(
                uri = mediaPath,
                mediaId = eventId.value,
                mimeType = "audio/ogg" // Files in the voice cache have no extension so we need to set the mime type manually.
            )
        }
    }

    override fun play() {
        ifInControl {
            mediaPlayer.play()
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
        if (mediaPlayer.state.value.mediaId.isMyTrack()) block()
    }
}
