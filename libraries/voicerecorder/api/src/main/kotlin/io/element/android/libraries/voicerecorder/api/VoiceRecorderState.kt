/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.api

import androidx.compose.runtime.Immutable
import java.io.File
import kotlin.time.Duration

@Immutable
sealed interface VoiceRecorderState {
    /**
     * The recorder is idle and not recording.
     */
    data object Idle : VoiceRecorderState

    /**
     * The recorder is currently recording.
     *
     * @property elapsedTime The elapsed time since the recording started.
     * @property levels The current audio levels of the recording as a fraction of 1.
     */
    data class Recording(val elapsedTime: Duration, val levels: List<Float>) : VoiceRecorderState

    /**
     * The recorder has finished recording.
     *
     * @property file The recorded file.
     * @property mimeType The mime type of the file.
     * @property waveform The waveform of the recording.
     * @property duration The total time spent recording.
     */
    data class Finished(
        val file: File,
        val mimeType: String,
        val waveform: List<Float>,
        val duration: Duration,
    ) : VoiceRecorderState
}
