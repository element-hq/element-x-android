/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.test

import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.voicerecorder.api.VoiceRecorder
import io.element.android.libraries.voicerecorder.api.VoiceRecorderState
import io.element.android.tests.testutils.lambda.lambdaError
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class FakeVoiceRecorder(
    private val timeSource: TestTimeSource = TestTimeSource(),
    private val recordingDuration: Duration = 0.seconds,
    private val levels: List<Float> = listOf(0.1f, 0.2f),
    val waveform: List<Float> = A_WAVEFORM,
    private val startRecordResult: () -> Unit = { lambdaError() },
    private val stopRecordResult: (Boolean) -> Unit = { lambdaError() },
    private val deleteRecordingResult: () -> Unit = { lambdaError() },
) : VoiceRecorder {
    private val _state = MutableStateFlow<VoiceRecorderState>(VoiceRecorderState.Idle)
    override val state: StateFlow<VoiceRecorderState> = _state

    private val levelInterval = if (levels.isEmpty()) Duration.ZERO else recordingDuration / levels.size

    private var currentRecording: File? = null
    private var lastRecordingId = 0
    private var activeRecordingId = 0

    override suspend fun startRecord() {
        startRecordResult()
        if (activeRecordingId != 0) return
        val recordingId = ++lastRecordingId
        activeRecordingId = recordingId
        currentRecording = File("file.ogg")
        val startedAt = timeSource.markNow()
        for (i in 1..levels.size) {
            delay(levelInterval)
            if (activeRecordingId != recordingId) return
            timeSource += levelInterval
            _state.emit(VoiceRecorderState.Recording(startedAt.elapsedNow(), levels.take(i)))
        }
    }

    override suspend fun stopRecord(cancelled: Boolean) {
        stopRecordResult(cancelled)
        activeRecordingId = 0
        if (cancelled) {
            deleteRecording()
        }
        _state.emit(
            when (val file = currentRecording) {
                null -> VoiceRecorderState.Idle
                else -> VoiceRecorderState.Finished(
                    file = file,
                    mimeType = MimeTypes.Ogg,
                    duration = recordingDuration,
                    waveform = waveform,
                )
            }
        )
    }

    override suspend fun deleteRecording() {
        deleteRecordingResult()
        activeRecordingId = 0
        currentRecording = null
        _state.emit(VoiceRecorderState.Idle)
    }
}

private val A_WAVEFORM = listOf(0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 8f, 7f, 6f, 5f, 4f, 3f, 2f, 1f, 0f)
