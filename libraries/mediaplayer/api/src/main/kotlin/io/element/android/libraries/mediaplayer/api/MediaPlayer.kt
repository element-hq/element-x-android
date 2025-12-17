/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaplayer.api

import io.element.android.libraries.matrix.api.core.EventId
import kotlinx.coroutines.flow.StateFlow

/**
 * A media player for Element X.
 */
interface MediaPlayer : AutoCloseable {
    /**
     * The current state of the player.
     */
    val state: StateFlow<State>

    /**
     * Initialises the player with a new media item, will suspend until the player is ready.
     *
     * @return the ready state of the player.
     */
    suspend fun setMedia(
        uri: String,
        mediaId: String,
        mimeType: String,
        startPositionMs: Long = 0,
    ): State

    /**
     * Plays the current media.
     */
    fun play()

    /**
     * Pauses the current media.
     */
    fun pause()

    /**
     * Seeks the current media to the given position.
     */
    fun seekTo(positionMs: Long)

    /**
     * Releases any resources associated with this player.
     */
    override fun close()

    data class State(
        /**
         * Whether the player is ready to play.
         */
        val isReady: Boolean,
        /**
         * Whether the player is currently playing.
         */
        val isPlaying: Boolean,
        /**
         * Whether the player has reached the end of the current media.
         */
        val isEnded: Boolean,
        /**
         * The id of the media which is currently playing.
         *
         * NB: This is usually the string representation of the [EventId] of the event
         * which contains the media.
         */
        val mediaId: String?,
        /**
         * The current position of the player.
         */
        val currentPosition: Long,
        /**
         * The duration of the current content, if available.
         */
        val duration: Long?,
    )
}
