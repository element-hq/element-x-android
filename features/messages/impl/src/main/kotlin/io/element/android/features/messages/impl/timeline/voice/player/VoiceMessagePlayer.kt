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

package io.element.android.features.messages.impl.timeline.voice.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.RoomScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

interface VoiceMessagePlayer : AutoCloseable {
    val isPlaying: StateFlow<Boolean>
    val progress: Progress
    fun playMediaUri(uri: String)
    fun play()
    fun pause()
    fun seekTo(percentage: Float)

    data class Progress(
        val elapsedMinutes: Int,
        val elapsedSeconds: Int,
        val percentage: Float,
    ) {
        companion object {
            val Zero = Progress(0, 0, 0f)
        }
    }
}

/**
 * Wrapper around [ExoPlayer] to play voice messages.
 *
 * The inner [ExoPlayer] is lazy initialized to avoid creating it when not needed.
 */
@ContributesBinding(RoomScope::class)
class VoiceMessagePlayerImpl @Inject constructor(
    @ApplicationContext context: Context,
) : VoiceMessagePlayer {

    private val playerLazy = lazy {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                addListener(listener)
                playWhenReady = true
            }
    }

    private val player by playerLazy

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }
    }

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    override val progress: VoiceMessagePlayer.Progress
        get() = VoiceMessagePlayer.Progress(
            elapsedMinutes = (player.currentPosition / 1000 / 60).toInt(),
            elapsedSeconds = (player.currentPosition / 1000 % 60).toInt(),
            percentage = player.currentPosition.toFloat() / player.duration,
        )

    override fun playMediaUri(uri: String) {
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()  // Will play right away because of `playWhenReady = true`.
    }

    override fun play() {
        if (player.playbackState == Player.STATE_ENDED) player.seekTo(0)
        player.play()
    }

    override fun pause() {
        player.pause()
    }

    override fun seekTo(percentage: Float) {
        player.seekTo((percentage * player.duration).toLong())
    }

    override fun close() {
        if (playerLazy.isInitialized()) player.release() // Will also remove the listener.
    }
}
