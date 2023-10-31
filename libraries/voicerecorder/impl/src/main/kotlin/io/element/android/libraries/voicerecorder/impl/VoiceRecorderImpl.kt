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

package io.element.android.libraries.voicerecorder.impl

import android.Manifest
import androidx.annotation.RequiresPermission
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.coroutine.childScope
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.voicerecorder.api.VoiceRecorder
import io.element.android.libraries.voicerecorder.api.VoiceRecorderState
import io.element.android.libraries.voicerecorder.impl.audio.Audio
import io.element.android.libraries.voicerecorder.impl.audio.AudioConfig
import io.element.android.libraries.voicerecorder.impl.audio.AudioLevelCalculator
import io.element.android.libraries.voicerecorder.impl.audio.AudioReader
import io.element.android.libraries.voicerecorder.impl.audio.Encoder
import io.element.android.libraries.voicerecorder.impl.audio.resample
import io.element.android.libraries.voicerecorder.impl.file.VoiceFileConfig
import io.element.android.libraries.voicerecorder.impl.file.VoiceFileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource

@SingleIn(RoomScope::class)
@ContributesBinding(RoomScope::class)
class VoiceRecorderImpl @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val timeSource: TimeSource,
    private val audioReaderFactory: AudioReader.Factory,
    private val encoder: Encoder,
    private val fileManager: VoiceFileManager,
    private val config: AudioConfig,
    private val fileConfig: VoiceFileConfig,
    private val audioLevelCalculator: AudioLevelCalculator,
    appCoroutineScope: CoroutineScope,
) : VoiceRecorder {
    private val voiceCoroutineScope by lazy {
        appCoroutineScope.childScope(dispatchers.io, "VoiceRecorder-${UUID.randomUUID()}")
    }

    private var outputFile: File? = null
    private var audioReader: AudioReader? = null
    private var recordingJob: Job? = null
    private val levels: MutableList<Float> = mutableListOf()
    private val lock = Mutex()

    private val _state = MutableStateFlow<VoiceRecorderState>(VoiceRecorderState.Idle)
    override val state: StateFlow<VoiceRecorderState> = _state

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override suspend fun startRecord() {
        Timber.i("Voice recorder started recording")
        outputFile = fileManager.createFile()
            .also(encoder::init)

        lock.withLock {
            levels.clear()
        }

        val audioRecorder = audioReaderFactory.create(config, dispatchers).also { audioReader = it }

        recordingJob = voiceCoroutineScope.launch {
            val startedAt = timeSource.markNow()
            audioRecorder.record { audio ->
                yield()

                val elapsedTime = startedAt.elapsedNow()

                if (elapsedTime >= 30.minutes) {
                    Timber.w("Voice message time limit reached")
                    stopRecord(false)
                    return@record
                }

                when (audio) {
                    is Audio.Data -> {
                        val audioLevel = audioLevelCalculator.calculateAudioLevel(audio.buffer)

                        lock.withLock{
                            levels.add(audioLevel)
                            _state.emit(VoiceRecorderState.Recording(elapsedTime, levels.toList()))
                        }
                        encoder.encode(audio.buffer, audio.readSize)
                    }
                    is Audio.Error -> {
                        Timber.e("Voice message error: code=${audio.audioRecordErrorCode}")
                        _state.emit(VoiceRecorderState.Recording(elapsedTime, listOf()))
                    }
                }
            }
        }
    }

    /**
     * Stop the current recording.
     *
     * Call [deleteRecording] to delete any recorded audio.
     */
    override suspend fun stopRecord(
        cancelled: Boolean
    ) {
        recordingJob?.cancel()?.also {
            Timber.i("Voice recorder stopped recording")
        }
        recordingJob = null

        audioReader?.stop()
        audioReader = null
        encoder.release()


        lock.withLock {
            if (cancelled) {
                deleteRecording()
                levels.clear()
            }

            _state.emit(
                when (val file = outputFile) {
                    null -> VoiceRecorderState.Idle
                    else -> VoiceRecorderState.Finished(
                        file = file,
                        mimeType = fileConfig.mimeType,
                        waveform = levels.resample(100),
                    )
                }
            )
        }
    }

    /**
     * Stop the current recording and delete the output file.
     */
    override suspend fun deleteRecording() {
        outputFile?.let(fileManager::deleteFile)?.also {
            Timber.i("Voice recorder deleted recording")
        }
        outputFile = null
        _state.emit(VoiceRecorderState.Idle)
    }
}
