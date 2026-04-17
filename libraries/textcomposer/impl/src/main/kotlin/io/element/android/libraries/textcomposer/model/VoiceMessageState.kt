/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlin.time.Duration

/**
 * The mode of a voice recording interaction.
 */
enum class RecordingMode {
    /** User is pressing and holding the record button. */
    Hold,

    /** Recording is locked — user tapped once or swiped up to lock. */
    Locked,
}

@Immutable
sealed interface VoiceMessageState {
    data object Idle : VoiceMessageState

    data class Recording(
        val duration: Duration,
        // Values are between 0 and 1
        val levels: ImmutableList<Float>,
        val mode: RecordingMode = RecordingMode.Hold,
        val isPaused: Boolean = false,
        // Playback review fields (used when paused and user taps play)
        val isPlayingBack: Boolean = false,
        val isPlaying: Boolean = false,
        val playbackProgress: Float = 0f,
        val playbackTime: Duration = Duration.ZERO,
        val waveform: ImmutableList<Float>? = null,
    ) : VoiceMessageState
}
