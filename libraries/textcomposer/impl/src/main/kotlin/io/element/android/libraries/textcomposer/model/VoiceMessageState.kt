/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlin.time.Duration

@Immutable
sealed interface VoiceMessageState {
    data object Idle : VoiceMessageState

    data class Preview(
        val isSending: Boolean,
        val isPlaying: Boolean,
        val showCursor: Boolean,
        val playbackProgress: Float,
        val time: Duration,
        val waveform: ImmutableList<Float>,
    ) : VoiceMessageState

    data class Recording(
        val duration: Duration,
        val levels: ImmutableList<Float>,
    ) : VoiceMessageState
}
