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

package io.element.android.libraries.voicerecorder.api

import java.io.File
import kotlin.time.Duration

sealed class VoiceRecorderState {
    /**
     * The recorder is idle and not recording.
     */
    data object Idle : VoiceRecorderState()

    /**
     * The recorder is currently recording.
     *
     * @property elapsedTime The elapsed time since the recording started.
     * @property levels The current audio levels of the recording as a fraction of 1.
     */
    data class Recording(val elapsedTime: Duration, val levels: List<Float>) : VoiceRecorderState()

    /**
     * The recorder has finished recording.
     *
     * @property file The recorded file.
     * @property mimeType The mime type of the file.
     * @property waveform The waveform of the recording.
     */
    data class Finished(
        val file: File,
        val mimeType: String,
        val waveform: List<Float>,
    ) : VoiceRecorderState()
}
