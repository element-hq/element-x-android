/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.impl

import android.Manifest
import androidx.annotation.RequiresPermission
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.appconfig.VoiceMessageConfig
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.coroutine.childScope
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
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
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

@SingleIn(RoomScope::class)
@ContributesBinding(RoomScope::class)
class DefaultVoiceRecorder(
    private val dispatchers: CoroutineDispatchers,
    private val timeSource: TimeSource,
    private val audioReaderFactory: AudioReader.Factory,
    private val encoder: Encoder,
    private val fileManager: VoiceFileManager,
    private val config: AudioConfig,
    private val fileConfig: VoiceFileConfig,
    private val audioLevelCalculator: AudioLevelCalculator,
    @SessionCoroutineScope
    sessionCoroutineScope: CoroutineScope,
) : VoiceRecorder {
    private val voiceCoroutineScope by lazy {
        sessionCoroutineScope.childScope(dispatchers.io, "VoiceRecorder-${UUID.randomUUID()}")
    }

    private var outputFile: File? = null
    private var audioReader: AudioReader? = null
    private var recordingJob: Job? = null

    // List of Float between 0 and 1 representing the audio levels
    private val levels: MutableList<Float> = mutableListOf()
    private val lock = Mutex()

    private val isPaused = AtomicBoolean(false)
    private var totalPausedDuration: Duration = Duration.ZERO
    private var pauseStartMark: TimeMark? = null

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
        isPaused.set(false)
        totalPausedDuration = Duration.ZERO
        pauseStartMark = null

        val audioRecorder = audioReaderFactory.create(config, dispatchers).also { audioReader = it }

        recordingJob = voiceCoroutineScope.launch {
            val startedAt = timeSource.markNow()
            audioRecorder.record { audio ->
                yield()

                // When paused, discard audio data but keep AudioRecord running
                if (isPaused.get()) {
                    return@record
                }

                val elapsedTime = lock.withLock {
                    startedAt.elapsedNow() - totalPausedDuration
                }

                if (elapsedTime > VoiceMessageConfig.maxVoiceMessageDuration) {
                    Timber.w("Voice message time limit reached")
                    stopRecord(false)
                    return@record
                }

                when (audio) {
                    is Audio.Data -> {
                        val audioLevel = audioLevelCalculator.calculateAudioLevel(audio.buffer)

                        lock.withLock {
                            levels.add(audioLevel)
                            _state.emit(VoiceRecorderState.Recording(elapsedTime, levels.toList(), file = outputFile))
                        }
                        encoder.encode(audio.buffer, audio.readSize)
                    }
                    is Audio.Error -> {
                        Timber.e("Voice message error: code=${audio.audioRecordErrorCode}")
                        _state.emit(VoiceRecorderState.Recording(elapsedTime, listOf(), file = outputFile))
                    }
                }
            }
        }
    }

    override suspend fun pauseRecord() {
        Timber.i("Voice recorder paused")
        val currentState = state.value as? VoiceRecorderState.Recording ?: return
        lock.withLock {
            isPaused.set(true)
            pauseStartMark = timeSource.markNow()
        }
        _state.emit(currentState.copy(isPaused = true))
    }

    override suspend fun resumeRecord() {
        Timber.i("Voice recorder resumed")
        val currentState = state.value as? VoiceRecorderState.Recording ?: return
        lock.withLock {
            pauseStartMark?.let { mark ->
                totalPausedDuration += mark.elapsedNow()
            }
            pauseStartMark = null
            isPaused.set(false)
        }
        _state.emit(currentState.copy(isPaused = false))
    }

    /**
     * Stop the current recording.
     *
     * Call [deleteRecording] to delete any recorded audio.
     */
    override suspend fun stopRecord(
        cancelled: Boolean
    ) {
        // If paused, account for the final paused segment
        lock.withLock {
            if (isPaused.get()) {
                pauseStartMark?.let { mark ->
                    totalPausedDuration += mark.elapsedNow()
                }
            }
            isPaused.set(false)
            pauseStartMark = null
        }

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
                    else -> {
                        val duration = (state.value as? VoiceRecorderState.Recording)?.elapsedTime
                        VoiceRecorderState.Finished(
                            file = file,
                            mimeType = fileConfig.mimeType,
                            waveform = levels.resample(100),
                            duration = duration ?: 0.milliseconds
                        )
                    }
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
