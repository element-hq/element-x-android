/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.circlerecorder.test

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import io.element.android.libraries.circlerecorder.api.CircleRecorder
import io.element.android.libraries.circlerecorder.api.CircleRecorderState
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.tests.testutils.lambda.lambdaError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class FakeCircleRecorder(
    private val recordingDuration: Duration = 1.seconds,
    private val startRecordResult: () -> Unit = { lambdaError() },
    private val stopRecordResult: (Boolean) -> Unit = { lambdaError() },
    private val deleteRecordingResult: () -> Unit = { lambdaError() },
) : CircleRecorder {
    private val _state = MutableStateFlow<CircleRecorderState>(CircleRecorderState.Idle)
    override val state: StateFlow<CircleRecorderState> = _state

    var useFrontCamera: Boolean = true
        private set
    var bindPreviewCount: Int = 0
        private set

    private var currentRecording: File? = null

    override fun bindPreview(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        bindPreviewCount++
    }

    override fun setUseFrontCamera(useFrontCamera: Boolean) {
        this.useFrontCamera = useFrontCamera
        val recording = _state.value as? CircleRecorderState.Recording ?: return
        _state.value = recording.copy(useFrontCamera = useFrontCamera)
    }

    override fun setLinearZoom(linearZoom: Float) = Unit

    override suspend fun startRecord() {
        startRecordResult()
        currentRecording = File("circle.mp4")
        _state.emit(
            CircleRecorderState.Recording(
                elapsedTime = recordingDuration,
                useFrontCamera = useFrontCamera,
            )
        )
    }

    override suspend fun stopRecord(cancelled: Boolean) {
        stopRecordResult(cancelled)
        if (cancelled) {
            deleteRecording()
            return
        }
        _state.emit(
            when (val file = currentRecording) {
                null -> CircleRecorderState.Idle
                else -> CircleRecorderState.Finished(
                    file = file,
                    mimeType = MimeTypes.Mp4,
                    duration = recordingDuration,
                )
            }
        )
    }

    override suspend fun deleteRecording() {
        deleteRecordingResult()
        currentRecording = null
        _state.emit(CircleRecorderState.Idle)
    }
}
