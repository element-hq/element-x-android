/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.impl.audio

import android.Manifest
import android.media.AudioRecord
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import androidx.annotation.RequiresPermission
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.di.RoomScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

class AndroidAudioReader
@RequiresPermission(Manifest.permission.RECORD_AUDIO)
private constructor(
    private val config: AudioConfig,
    private val dispatchers: CoroutineDispatchers,
) : AudioReader {
    private val audioRecord: AudioRecord
    private var noiseSuppressor: NoiseSuppressor? = null
    private var automaticGainControl: AutomaticGainControl? = null
    private val outputBuffer: ShortArray

    init {
        outputBuffer = createOutputBuffer(config.sampleRate)
        audioRecord = AudioRecord.Builder().setAudioSource(config.source).setAudioFormat(config.format).setBufferSizeInBytes(outputBuffer.sizeInBytes()).build()
        noiseSuppressor = requestNoiseSuppressor(audioRecord)
        automaticGainControl = requestAutomaticGainControl(audioRecord)
    }

    /**
     * Record audio data continuously.
     *
     * @param onAudio callback when audio is read.
     */
    override suspend fun record(
        onAudio: suspend (Audio) -> Unit,
    ) {
        audioRecord.startRecording()
        withContext(dispatchers.io) {
            while (isActive) {
                if (audioRecord.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                    break
                }
                onAudio(read())
            }
        }
    }

    private fun read(): Audio {
        val result = audioRecord.read(outputBuffer, 0, outputBuffer.size)

        if (isAudioRecordErrorResult(result)) {
            return Audio.Error(result)
        }

        return Audio.Data(
            result,
            outputBuffer,
        )
    }

    override fun stop() {
        if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
            audioRecord.stop()
        }
        audioRecord.release()

        noiseSuppressor?.release()
        noiseSuppressor = null

        automaticGainControl?.release()
        automaticGainControl = null
    }

    private fun createOutputBuffer(sampleRate: SampleRate): ShortArray {
        val bufferSizeInShorts = AudioRecord.getMinBufferSize(
            sampleRate.HZ,
            config.format.channelMask,
            config.format.encoding
        )
        return ShortArray(bufferSizeInShorts)
    }

    private fun requestNoiseSuppressor(audioRecord: AudioRecord): NoiseSuppressor? {
        if (!NoiseSuppressor.isAvailable()) {
            return null
        }

        return tryOrNull {
            NoiseSuppressor.create(audioRecord.audioSessionId).apply {
                enabled = true
            }
        }
    }

    private fun requestAutomaticGainControl(audioRecord: AudioRecord): AutomaticGainControl? {
        if (!AutomaticGainControl.isAvailable()) {
            return null
        }

        return tryOrNull {
            AutomaticGainControl.create(audioRecord.audioSessionId).apply {
                enabled = true
            }
        }
    }

    @ContributesBinding(RoomScope::class)
    companion object Factory : AudioReader.Factory {
        @RequiresPermission(Manifest.permission.RECORD_AUDIO)
        override fun create(config: AudioConfig, dispatchers: CoroutineDispatchers): AndroidAudioReader {
            return AndroidAudioReader(config, dispatchers)
        }
    }
}

private fun isAudioRecordErrorResult(result: Int): Boolean {
    return result < 0
}

private fun ShortArray.sizeInBytes(): Int = size * Short.SIZE_BYTES
