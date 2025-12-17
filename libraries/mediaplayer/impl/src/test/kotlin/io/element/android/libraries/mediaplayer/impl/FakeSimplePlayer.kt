/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaplayer.impl

import androidx.media3.common.MediaItem
import io.element.android.tests.testutils.lambda.lambdaError

class FakeSimplePlayer(
    private val clearMediaItemsLambda: () -> Unit = { lambdaError() },
    private val setMediaItemLambda: (MediaItem, Long) -> Unit = { _, _ -> lambdaError() },
    private val getCurrentMediaItemLambda: () -> MediaItem? = { lambdaError() },
    private val prepareLambda: () -> Unit = { lambdaError() },
    private val playLambda: () -> Unit = { lambdaError() },
    private val isPlayingLambda: () -> Boolean = { lambdaError() },
    private val pauseLambda: () -> Unit = { lambdaError() },
    private val seekToLambda: (Long) -> Unit = { lambdaError() },
    private val releaseLambda: () -> Unit = { lambdaError() },
) : SimplePlayer {
    private val listeners = mutableListOf<SimplePlayer.Listener>()
    override fun addListener(listener: SimplePlayer.Listener) {
        listeners.add(listener)
    }

    var currentPositionResult: Long = 0
    override val currentPosition: Long get() = currentPositionResult
    var playbackStateResult: Int = 0
    override val playbackState: Int get() = playbackStateResult
    var durationResult: Long = 0
    override val duration: Long get() = durationResult

    override fun clearMediaItems() = clearMediaItemsLambda()
    override fun setMediaItem(mediaItem: MediaItem, startPositionMs: Long) {
        setMediaItemLambda(mediaItem, startPositionMs)
    }

    override fun getCurrentMediaItem(): MediaItem? = getCurrentMediaItemLambda()
    override fun prepare() = prepareLambda()
    override fun play() = playLambda()
    override fun isPlaying() = isPlayingLambda()
    override fun pause() = pauseLambda()
    override fun seekTo(positionMs: Long) = seekToLambda(positionMs)
    override fun release() = releaseLambda()

    fun simulateIsPlayingChanged(isPlaying: Boolean) {
        listeners.forEach { it.onIsPlayingChanged(isPlaying) }
    }

    fun simulateMediaItemTransition(mediaItem: MediaItem?) {
        listeners.forEach { it.onMediaItemTransition(mediaItem) }
    }

    fun simulatePlaybackStateChanged(playbackState: Int) {
        listeners.forEach { it.onPlaybackStateChanged(playbackState) }
    }
}
