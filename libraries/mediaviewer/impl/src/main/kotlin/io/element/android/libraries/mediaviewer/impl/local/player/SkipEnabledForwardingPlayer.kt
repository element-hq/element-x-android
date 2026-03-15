/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.player

import android.annotation.SuppressLint
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi

/**
 * A [ForwardingPlayer] that adds skip next/previous media item support.
 *
 * When [canSkipNext]/[canSkipPrev] are true, the corresponding skip commands
 * are added to available commands (making Media3 show buttons on the notification)
 * and seek calls are delegated to the skip callbacks.
 *
 * When skip is not available, default player behavior is preserved (e.g.,
 * seekToPrevious seeks to the beginning of the current track).
 */
@SuppressLint("UnsafeOptInUsageError")
@OptIn(UnstableApi::class)
class SkipEnabledForwardingPlayer(
    player: Player,
    private val onSkipToNext: () -> Unit,
    private val onSkipToPrevious: () -> Unit,
) : ForwardingPlayer(player) {
    var canSkipNext: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                notifyAvailableCommandsChanged()
            }
        }

    var canSkipPrev: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                notifyAvailableCommandsChanged()
            }
        }

    private fun notifyAvailableCommandsChanged() {
        val commands = availableCommands
        for (listener in registeredListeners) {
            listener.onAvailableCommandsChanged(commands)
        }
    }

    private val registeredListeners = mutableListOf<Player.Listener>()

    override fun addListener(listener: Player.Listener) {
        registeredListeners.add(listener)
        super.addListener(listener)
    }

    override fun removeListener(listener: Player.Listener) {
        registeredListeners.remove(listener)
        super.removeListener(listener)
    }

    override fun getAvailableCommands(): Player.Commands {
        val builder = super.getAvailableCommands().buildUpon()
        if (canSkipNext) {
            builder.add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
            builder.add(Player.COMMAND_SEEK_TO_NEXT)
        }
        if (canSkipPrev) {
            builder.add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
            builder.add(Player.COMMAND_SEEK_TO_PREVIOUS)
        }
        return builder.build()
    }

    override fun isCommandAvailable(command: Int): Boolean {
        return when (command) {
            Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM -> canSkipNext
            Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM -> canSkipPrev
            else -> super.isCommandAvailable(command)
        }
    }

    override fun hasNextMediaItem(): Boolean = canSkipNext

    override fun hasPreviousMediaItem(): Boolean = canSkipPrev

    override fun seekToNext() {
        if (canSkipNext) {
            onSkipToNext()
        } else {
            super.seekToNext()
        }
    }

    override fun seekToNextMediaItem() {
        if (canSkipNext) {
            onSkipToNext()
        } else {
            super.seekToNextMediaItem()
        }
    }

    override fun seekToPrevious() {
        if (canSkipPrev) {
            onSkipToPrevious()
        } else {
            super.seekToPrevious()
        }
    }

    override fun seekToPreviousMediaItem() {
        if (canSkipPrev) {
            onSkipToPrevious()
        } else {
            super.seekToPreviousMediaItem()
        }
    }
}
