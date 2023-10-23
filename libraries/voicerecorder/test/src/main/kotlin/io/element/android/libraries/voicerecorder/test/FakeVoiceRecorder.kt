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

package io.element.android.libraries.voicerecorder.test

import io.element.android.libraries.voicerecorder.api.VoiceRecorder
import io.element.android.libraries.voicerecorder.api.VoiceRecorderState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class FakeVoiceRecorder(
    private val levels: List<Double> = listOf(0.1, 0.2)
) : VoiceRecorder {
    private val _state = MutableStateFlow<VoiceRecorderState>(VoiceRecorderState.Idle)
    override val state: StateFlow<VoiceRecorderState> = _state

    private var curRecording: File? = null

    private var securityException: SecurityException? = null

    override suspend fun startRecord() {
        securityException?.let { throw it }

        if (curRecording != null) {
            error("Previous recording was not cleared")
        }
        curRecording = File("file.ogg")

        levels.forEach {
            _state.emit(VoiceRecorderState.Recording(it))
        }
    }

    override suspend fun stopRecord(
        cancelled: Boolean
    ) {
        if (cancelled) {
            deleteRecording()
        }

        _state.emit(
            when (curRecording) {
                null -> VoiceRecorderState.Idle
                else -> VoiceRecorderState.Finished(curRecording!!, "audio/ogg")
            }
        )
    }

    override suspend fun deleteRecording() {
        curRecording = null

        _state.emit(
            VoiceRecorderState.Idle
        )
    }

    fun givenThrowsSecurityException(exception: SecurityException) {
        this.securityException = exception
    }
}
