/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.circlemessages.timeline

import android.graphics.Bitmap
import androidx.compose.runtime.Stable
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Stable
data class CircleMessageState(
    val button: Button,
    val progress: Float,
    val isPlaying: Boolean,
    val durationMs: Long,
    val isExpanded: Boolean,
    val lastFrame: Bitmap?,
    val exoPlayer: ExoPlayer?,
    val eventSink: (CircleMessageEvent) -> Unit,
) {
    enum class Button {
        Play,
        Pause,
        Downloading,
        Retry,
        Disabled,
    }
}

sealed interface CircleMessageEvent {
    data object PlayPause : CircleMessageEvent
    data class BindPlayerView(val view: PlayerView) : CircleMessageEvent
    data class UnbindPlayerView(val view: PlayerView) : CircleMessageEvent
}
