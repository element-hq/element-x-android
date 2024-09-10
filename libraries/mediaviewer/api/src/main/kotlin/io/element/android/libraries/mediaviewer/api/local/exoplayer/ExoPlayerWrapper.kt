/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.api.local.exoplayer

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

/**
 * Wrapper around ExoPlayer to disable some commands.
 * Necessary to hide the settings wheels from the player.
 */
@UnstableApi
class ExoPlayerWrapper(private val exoPlayer: ExoPlayer) : ExoPlayer by exoPlayer {
    override fun isCommandAvailable(command: Int): Boolean {
        return availableCommands.contains(command)
    }

    override fun getAvailableCommands(): Player.Commands {
        return exoPlayer.availableCommands
            .buildUpon()
            .remove(Player.COMMAND_SET_TRACK_SELECTION_PARAMETERS)
            .build()
    }

    companion object {
        fun create(context: Context): ExoPlayer {
            return ExoPlayerWrapper(
                ExoPlayer.Builder(context).build()
            )
        }
    }
}
