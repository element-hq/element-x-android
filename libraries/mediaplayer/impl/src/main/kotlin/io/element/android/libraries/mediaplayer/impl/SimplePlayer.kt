/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaplayer.impl

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.annotations.ApplicationContext

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
    fun isPlaying(): Boolean
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
@BindingContainer
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

    override fun isPlaying() = p.isPlaying

    override fun pause() = p.pause()

    override fun seekTo(positionMs: Long) = p.seekTo(positionMs)

    override fun release() = p.release()
}
