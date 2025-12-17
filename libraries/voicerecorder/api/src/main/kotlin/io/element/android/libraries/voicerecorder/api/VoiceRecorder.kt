/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
