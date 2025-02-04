/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.test

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.voicerecorder.api.VoiceRecorder
import io.element.android.libraries.voicerecorder.api.VoiceRecorderState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class FakeVoiceRecorder(
    private val timeSource: TestTimeSource = TestTimeSource(),
    private val recordingDuration: Duration = 0.seconds,
    private val levels: List<Float> = listOf(0.1f, 0.2f)
) : VoiceRecorder {
    private val _state = MutableStateFlow<VoiceRecorderState>(VoiceRecorderState.Idle)
    override val state: StateFlow<VoiceRecorderState> = _state

    private var curRecording: File? = null

    private var securityException: SecurityException? = null

    private var startedCount = 0
    private var stoppedCount = 0
    private var deletedCount = 0

    var waveform: List<Float> = listOf(0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 8f, 7f, 6f, 5f, 4f, 3f, 2f, 1f, 0f)
    override suspend fun startRecord() {
        startedCount += 1
        val startedAt = timeSource.markNow()
        securityException?.let { throw it }

        if (curRecording != null) {
            error("Previous recording was not cleared")
        }
        curRecording = File("file.ogg")

        timeSource += recordingDuration
        for (i in 1..levels.size) {
            _state.emit(VoiceRecorderState.Recording(startedAt.elapsedNow(), levels.take(i)))
        }
    }

    override suspend fun stopRecord(
        cancelled: Boolean
    ) {
        stoppedCount++

        if (cancelled) {
            deleteRecording()
        }

        _state.emit(
            when (curRecording) {
                null -> VoiceRecorderState.Idle
                else -> VoiceRecorderState.Finished(
                    file = curRecording!!,
                    mimeType = MimeTypes.Ogg,
                    duration = recordingDuration,
                    waveform = waveform,
                )
            }
        )
    }

    override suspend fun deleteRecording() {
        deletedCount++
        curRecording = null

        _state.emit(
            VoiceRecorderState.Idle
        )
    }

    fun assertCalls(
        started: Int = 0,
        stopped: Int = 0,
        deleted: Int = 0,
    ) {
        assertThat(startedCount).isEqualTo(started)
        assertThat(stoppedCount).isEqualTo(stopped)
        assertThat(deletedCount).isEqualTo(deleted)
    }

    fun givenThrowsSecurityException(exception: SecurityException) {
        this.securityException = exception
    }
}
