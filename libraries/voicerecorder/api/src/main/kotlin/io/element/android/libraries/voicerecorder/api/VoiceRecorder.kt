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

import android.Manifest
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.StateFlow

/**
 * Audio recorder which records audio to opus/ogg files.
 */
interface VoiceRecorder {
    /**
     * Start a recording.
     *
     * Call [stopRecord] to stop the recording and release resources.
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    suspend fun startRecord()

    /**
     * Stop the current recording.
     *
     * Call [deleteRecording] to delete any recorded audio.
     *
     * @param cancelled If true, the recording is deleted.
     */
    suspend fun stopRecord(
        cancelled: Boolean = false
    )

    /**
     * Stop the current recording and delete the output file.
     */
    suspend fun deleteRecording()

    /**
     * The current state of the recorder.
     */
    val state: StateFlow<VoiceRecorderState>
}
