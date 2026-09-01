/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.impl.audio

import android.Manifest
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
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
import timber.log.Timber

class AndroidAudioReader
@RequiresPermission(Manifest.permission.RECORD_AUDIO)
private constructor(
    private val context: Context,
    private val config: AudioConfig,
    private val dispatchers: CoroutineDispatchers,
) : AudioReader {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val audioRecord: AudioRecord
    private var noiseSuppressor: NoiseSuppressor? = null
    private var automaticGainControl: AutomaticGainControl? = null
    private val outputBuffer: ShortArray

    // Set while a Bluetooth SCO connection was requested for this recording, so it can be torn down again in [stop].
    private var isUsingBluetoothSco = false

    // The audio mode in place before we (potentially) switched it to MODE_IN_COMMUNICATION to route to a Bluetooth device.
    private var previousAudioMode: Int? = null

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
        routeToCurrentlyConnectedInputDevice()
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

        stopBluetoothScoRoutingIfNeeded()
    }

    /**
     * By default, [AudioRecord] records from the device's main built-in microphone, ignoring any other microphone
     * (wired or Bluetooth headset, USB microphone, etc.) that may currently be connected.
     *
     * This looks at the currently attached input devices and, if one of them is preferable to the built-in
     * microphone (e.g. a paired Bluetooth headset), explicitly routes the recording to it.
     */
    private fun routeToCurrentlyConnectedInputDevice() {
        val audioManager = audioManager ?: return
        val preferredDevice = findPreferredInputDevice(audioManager) ?: return

        if (preferredDevice.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
            // A Bluetooth headset's microphone is only reachable once a SCO (voice) connection has been
            // established with it, the audio framework won't route to it otherwise, even if it's the
            // preferred device.
            startBluetoothScoRouting(audioManager)
        }

        val wasDeviceSet = tryOrNull(
            onException = { Timber.e(it, "Voice recorder: failed to set preferred input device") }
        ) {
            audioRecord.setPreferredDevice(preferredDevice)
        }
        if (wasDeviceSet != true) {
            Timber.w("Voice recorder: could not route recording to ${preferredDevice.type}")
        }
    }

    private fun findPreferredInputDevice(audioManager: AudioManager): AudioDeviceInfo? {
        val inputDevices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
        return inputDevices
            .filter { it.type in preferredInputDeviceTypesByPriority }
            .minByOrNull { preferredInputDeviceTypesByPriority.indexOf(it.type) }
    }

    private fun startBluetoothScoRouting(audioManager: AudioManager) {
        val started = tryOrNull(
            onException = { Timber.e(it, "Voice recorder: failed to start Bluetooth SCO connection") }
        ) {
            previousAudioMode = audioManager.mode
            // Required for the audio framework to route audio to/from the SCO device.
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isBluetoothScoOn = true
            audioManager.startBluetoothSco()
            true
        }
        isUsingBluetoothSco = started == true
        if (!isUsingBluetoothSco) {
            previousAudioMode?.let { audioManager.mode = it }
            previousAudioMode = null
        }
    }

    private fun stopBluetoothScoRoutingIfNeeded() {
        val audioManager = audioManager
        if (!isUsingBluetoothSco || audioManager == null) return

        tryOrNull(
            onException = { Timber.e(it, "Voice recorder: failed to stop Bluetooth SCO connection") }
        ) {
            audioManager.stopBluetoothSco()
            audioManager.isBluetoothScoOn = false
            previousAudioMode?.let { audioManager.mode = it }
        }
        previousAudioMode = null
        isUsingBluetoothSco = false
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
        override fun create(context: Context, config: AudioConfig, dispatchers: CoroutineDispatchers): AndroidAudioReader {
            return AndroidAudioReader(context, config, dispatchers)
        }
    }
}

/**
 * Input device types that we consider preferable to the built-in microphone, ordered from most to least preferred.
 * Any device not present here (or the absence of any of these devices) means we let the system pick the default,
 * which is the built-in microphone.
 */
private val preferredInputDeviceTypesByPriority = listOf(
    AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
    AudioDeviceInfo.TYPE_USB_HEADSET,
    AudioDeviceInfo.TYPE_USB_DEVICE,
    AudioDeviceInfo.TYPE_WIRED_HEADSET,
    AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
)

private fun isAudioRecordErrorResult(result: Int): Boolean {
    return result < 0
}

private fun ShortArray.sizeInBytes(): Int = size * Short.SIZE_BYTES
