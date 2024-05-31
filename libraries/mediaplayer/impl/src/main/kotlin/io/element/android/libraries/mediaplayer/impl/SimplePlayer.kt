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

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.RoomScope

/**
 * A subset of media3 [Player] that only exposes the few methods we need making it easier to mock.
 */
interface SimplePlayer {
    fun addListener(listener: Listener)
    val currentPosition: Long
    val playbackState: Int
    val duration: Long
    fun clearMediaItems()
    fun setMediaItem(mediaItem: MediaItem, startPositionMs: Long)
    fun getCurrentMediaItem(): MediaItem?
    fun prepare()
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun release()
    interface Listener {
        fun onIsPlayingChanged(isPlaying: Boolean)
        fun onMediaItemTransition(mediaItem: MediaItem?)
        fun onPlaybackStateChanged(playbackState: Int)
    }
}

@ContributesTo(RoomScope::class)
@Module
object SimplePlayerModule {
    @Provides
    fun simplePlayerProvider(
        @ApplicationContext context: Context,
    ): SimplePlayer = DefaultSimplePlayer(ExoPlayer.Builder(context).build())
}

/**
 * Default implementation of [SimplePlayer] backed by a media3 [Player].
 */
class DefaultSimplePlayer(
    private val p: Player
) : SimplePlayer {
    override fun addListener(listener: SimplePlayer.Listener) {
        p.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) = listener.onIsPlayingChanged(isPlaying)
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = listener.onMediaItemTransition(mediaItem)
            override fun onPlaybackStateChanged(playbackState: Int) = listener.onPlaybackStateChanged(playbackState)
        })
    }

    override val currentPosition: Long
        get() = p.currentPosition
    override val playbackState: Int
        get() = p.playbackState
    override val duration: Long
        get() = p.duration

    override fun clearMediaItems() = p.clearMediaItems()

    override fun setMediaItem(mediaItem: MediaItem, startPositionMs: Long) = p.setMediaItem(mediaItem, startPositionMs)

    override fun getCurrentMediaItem(): MediaItem? = p.currentMediaItem

    override fun prepare() = p.prepare()

    override fun play() = p.play()

    override fun pause() = p.pause()

    override fun seekTo(positionMs: Long) = p.seekTo(positionMs)

    override fun release() = p.release()
}
