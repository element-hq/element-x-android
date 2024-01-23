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
