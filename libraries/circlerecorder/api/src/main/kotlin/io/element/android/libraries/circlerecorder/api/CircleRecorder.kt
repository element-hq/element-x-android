/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.circlerecorder.api

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.StateFlow

/**
 * Records a circle video (camera + microphone) to an mp4 file.
 */
interface CircleRecorder {
    /**
     * Bind the camera preview to [previewView]. Must be called before or while recording.
     */
    fun bindPreview(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
    )

    /**
     * Switch between the front and rear cameras. An in-progress recording is split into
     * segments and concatenated when stopping, so footage from both cameras is kept.
     */
    fun setUseFrontCamera(useFrontCamera: Boolean)

    /**
     * Set CameraX linear zoom in the range `0f..1f` (min zoom to max zoom).
     */
    fun setLinearZoom(linearZoom: Float)

    /**
     * Start recording. Call [stopRecord] to finish and release the camera.
     */
    @RequiresPermission(allOf = [Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO])
    suspend fun startRecord()

    /**
     * Stop the current recording.
     *
     * @param cancelled If true, the recording is deleted.
     */
    suspend fun stopRecord(
        cancelled: Boolean = false
    )

    /**
     * Delete any recorded file and return to idle.
     */
    suspend fun deleteRecording()

    /**
     * The current state of the recorder.
     */
    val state: StateFlow<CircleRecorderState>
}
