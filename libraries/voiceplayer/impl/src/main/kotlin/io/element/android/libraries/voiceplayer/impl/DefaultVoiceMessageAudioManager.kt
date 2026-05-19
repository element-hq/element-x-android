/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voiceplayer.impl

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.getSystemService
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.voiceplayer.api.VoiceMessageAudioManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

/**
 * Implementation of [VoiceMessageAudioManager].
 *
 * - Defaults to speaker for voice messages (or Bluetooth SCO device if available)
 * - Proximity sensor always active (switches to earpiece when device is held to ear)
 * - Turns off screen when device is held to ear
 * - Uses TYPE_BLUETOOTH_SCO for Bluetooth communication (TYPE_BLUETOOTH_A2DP is not valid for
 *   setCommunicationDevice() as it's for media playback only)
 * - Proximity sensor remains active even when Bluetooth is connected
 */
@ContributesBinding(RoomScope::class)
public class DefaultVoiceMessageAudioManager constructor(
    @ApplicationContext private val context: Context,
) : VoiceMessageAudioManager {
    private val audioManager = context.getSystemService<AudioManager>()
    private val sensorManager = context.getSystemService<SensorManager>()
    private val powerManager = context.getSystemService<PowerManager>()

    private val proximitySensorWakeLock by lazy {
        powerManager
            ?.takeIf { it.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK) }
            ?.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "${context.packageName}:ProximitySensorVoiceMessageWakeLock")
    }

    private val proximitySensorMutex = Mutex()
    private val audioDeviceCallbackMutex = Mutex()
    private val proximityListenerMutex = Mutex()
    private val deviceSelectionMutex = Mutex()

    private var isRoutingActive = false
    private var currentDeviceType: Int? = null

    private val mainScope = MainScope()
    private val audioDeviceCallbackScope = CoroutineScope(Dispatchers.Main)

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            if (addedDevices == null) return

            audioDeviceCallbackScope.launch {
                audioDeviceCallbackMutex.withLock {
                    if (!isRoutingActive) return@withLock

                    val onBuiltInSpeaker = currentDeviceType == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                    val hasBluetooth = addedDevices.any { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO }
                    val hasWiredAudio = addedDevices.any {
                        it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                        it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                        it.type == AudioDeviceInfo.TYPE_USB_HEADSET
                    }

                    if (onBuiltInSpeaker) {
                        when {
                            hasBluetooth -> {
                                selectAudioDevice(AudioDeviceInfo.TYPE_BLUETOOTH_SCO)
                                currentDeviceType = AudioDeviceInfo.TYPE_BLUETOOTH_SCO
                            }
                            hasWiredAudio -> {
                                val wiredDevice = addedDevices.firstOrNull {
                                    it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                                    it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                                    it.type == AudioDeviceInfo.TYPE_USB_HEADSET
                                }
                                wiredDevice?.let {
                                    selectAudioDevice(it.type)
                                    currentDeviceType = it.type
                                }
                            }
                        }
                    }
                }
            }
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            if (removedDevices == null) return

            audioDeviceCallbackScope.launch {
                audioDeviceCallbackMutex.withLock {
                    if (!isRoutingActive) return@withLock

                    val usingBluetooth = currentDeviceType == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
                    val bluetoothRemoved = removedDevices.any { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO }

                    val usingWiredAudio = currentDeviceType in listOf(
                        AudioDeviceInfo.TYPE_WIRED_HEADSET,
                        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                        AudioDeviceInfo.TYPE_USB_HEADSET
                    )
                    val wiredAudioRemoved = removedDevices.any {
                        it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                        it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                        it.type == AudioDeviceInfo.TYPE_USB_HEADSET
                    }

                    when {
                        usingBluetooth && bluetoothRemoved -> {
                            val wiredAudio = findWiredAudioDevice()
                            if (wiredAudio != null) {
                                selectAudioDevice(wiredAudio.type)
                                currentDeviceType = wiredAudio.type
                            } else {
                                selectAudioDevice(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
                                currentDeviceType = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                            }
                        }
                        usingWiredAudio && wiredAudioRemoved -> {
                            val bluetooth = findBluetoothDevice()
                            if (bluetooth != null) {
                                selectAudioDevice(AudioDeviceInfo.TYPE_BLUETOOTH_SCO)
                                currentDeviceType = AudioDeviceInfo.TYPE_BLUETOOTH_SCO
                            } else {
                                selectAudioDevice(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
                                currentDeviceType = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Start audio routing for voice message playback.
     * This will:
     * 1. Default to speaker for voice messages (or external device if available)
     * 2. Always start proximity sensor monitoring (regardless of connected device)
     * 3. Register audio device callback to detect external audio device connections
     */
    override fun startRouting() {
        if (isRoutingActive) return
        isRoutingActive = true

        mainScope.launch(Dispatchers.Main) {
            audioManager?.let { manager ->
                val externalDevice = findCommunicationDevice()

                if (externalDevice != null) {
                    selectAudioDevice(externalDevice.type)
                    currentDeviceType = externalDevice.type
                } else {
                    selectAudioDevice(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
                    currentDeviceType = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                }

                // Always start proximity sensor monitoring, regardless of connected devices
                startProximityMonitoring()

                // Register audio device callback to detect external audio device connections
                manager.registerAudioDeviceCallback(audioDeviceCallback, null)
            }
        }
    }

    /**
     * Find a wired/USB audio device for communication.
     * Checks multiple device types as different devices may report differently.
     */
    private fun findWiredAudioDevice(): AudioDeviceInfo? {
        val audioManager = audioManager ?: return null

        val deviceTypes = listOf(
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_USB_HEADSET
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val allDevices = audioManager.availableCommunicationDevices +
                audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            return allDevices.find { it.type in deviceTypes }
        } else {
            return audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                .find { it.type in deviceTypes }
        }
    }

    /**
     * Find a Bluetooth device for audio output.
     * Returns null if no Bluetooth device is available.
     *
     * Note: For voice messages (communication), we use TYPE_BLUETOOTH_SCO which is valid
     * for setCommunicationDevice(). TYPE_BLUETOOTH_A2DP is for media playback and cannot
     * be used as a communication device.
     */
    private fun findBluetoothDevice(): AudioDeviceInfo? {
        val audioManager = audioManager ?: return null

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioManager.availableCommunicationDevices.find {
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
            }
        } else {
            audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).find {
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
            }
        }
    }

    /**
     * Find any available communication device (Bluetooth SCO or wired/USB audio).
     * Bluetooth is preferred over wired audio.
     */
    private fun findCommunicationDevice(): AudioDeviceInfo? {
        return findBluetoothDevice() ?: findWiredAudioDevice()
    }

    /**
     * Stop audio routing for voice message playback.
     * This will:
     * 1. Release proximity sensor wake lock
     * 2. Stop proximity sensor monitoring
     * 3. Unregister audio device callback
     * 4. Reset audio to normal mode
     */
    override fun stopRouting() {
        if (!isRoutingActive) return
        isRoutingActive = false

        mainScope.launch(Dispatchers.Main) {
            audioManager?.unregisterAudioDeviceCallback(audioDeviceCallback)
            stopProximityMonitoring()
            releaseWakeLock()
            currentDeviceType = null
        }
    }

    /**
     * Select a specific audio device for output.
     * For voice messages, we only switch between speaker and earpiece.
     * Uses mutex to prevent concurrent device selections that could cause audio routing issues.
     */
    private suspend fun selectAudioDevice(deviceType: Int) {
        deviceSelectionMutex.withLock {
            val audioManager = audioManager ?: return@withLock

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val device = audioManager.availableCommunicationDevices
                    .find { it.type == deviceType }
                    ?: audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                        .find { it.type == deviceType }

                device?.let {
                    try {
                        audioManager.setCommunicationDevice(it)
                    } catch (e: IllegalArgumentException) {
                        Timber.e(e, "Failed to set communication device: ${deviceType}")
                        if (deviceType != AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                            Timber.w("Falling back to built-in speaker")
                            selectAudioDeviceInternal(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
                        }
                    }
                } ?: run {
                    Timber.w("Audio device not found: ${deviceType}")
                    if (deviceType != AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                        Timber.w("Falling back to built-in speaker")
                        selectAudioDeviceInternal(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                when (deviceType) {
                    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> {
                        audioManager.isSpeakerphoneOn = true
                    }
                    AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> {
                        audioManager.isSpeakerphoneOn = false
                    }
                    else -> {
                        Timber.w("Unsupported device type for Android < S: ${deviceType}")
                    }
                }
            }
        }
    }

    /**
     * Internal device selection without mutex lock (used for fallback within withLock).
     */
    private fun selectAudioDeviceInternal(deviceType: Int) {
        val audioManager = audioManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val device = audioManager.availableCommunicationDevices
                .find { it.type == deviceType }
                ?: audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                    .find { it.type == deviceType }

            device?.let {
                try {
                    audioManager.setCommunicationDevice(it)
                } catch (e: IllegalArgumentException) {
                    Timber.e(e, "Failed to set communication device: ${deviceType}")
                }
            }
        } else {
            @Suppress("DEPRECATION")
            when (deviceType) {
                AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> {
                    audioManager.isSpeakerphoneOn = true
                }
                AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> {
                    audioManager.isSpeakerphoneOn = false
                }
                else -> {
                    Timber.w("Unsupported device type for Android < S: ${deviceType}")
                }
            }
        }
    }

    /**
     * Start proximity sensor monitoring.
     */
    private fun startProximityMonitoring() {
        val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY) ?: run {
            Timber.w("Proximity sensor not available")
            return
        }

        mainScope.launch(Dispatchers.Main) {
            sensorManager.registerListener(
                proximityListener,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    /**
     * Stop proximity sensor monitoring.
     */
    private fun stopProximityMonitoring() {
        mainScope.launch(Dispatchers.Main) {
            sensorManager?.unregisterListener(proximityListener)
        }
    }

    /**
     * Acquire proximity sensor wake lock (turns off screen when device is held to ear).
     */
    private fun acquireWakeLock() {
        mainScope.launch {
            proximitySensorMutex.withLock {
                if (proximitySensorWakeLock?.isHeld == false) {
                    proximitySensorWakeLock?.acquire()
                }
            }
        }
    }

    /**
     * Release proximity sensor wake lock.
     */
    private fun releaseWakeLock() {
        mainScope.launch {
            proximitySensorMutex.withLock {
                if (proximitySensorWakeLock?.isHeld == true) {
                    proximitySensorWakeLock?.release()
                }
            }
        }
    }

    /**
     * Proximity sensor listener.
     * When held to ear: switches to earpiece
     * When moved away: switches back to the device that was active before earpiece (speaker or Bluetooth)
     */
    private val proximityListener = object : SensorEventListener {
        private var originalDeviceType: Int? = null

        override fun onSensorChanged(event: SensorEvent?) {
            if (!isRoutingActive) return

            mainScope.launch {
                proximityListenerMutex.withLock {
                    if (!isRoutingActive) return@launch

                    event?.let {
                        val distance = it.values[0]
                        val isClose = distance < 5.0f

                        if (isClose) {
                            if (originalDeviceType == null) {
                                originalDeviceType = currentDeviceType
                            }
                            selectAudioDevice(AudioDeviceInfo.TYPE_BUILTIN_EARPIECE)
                            currentDeviceType = AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
                            acquireWakeLock()
                        } else {
                            val externalDevice = findCommunicationDevice()
                            val deviceToRestore = externalDevice?.type
                                ?: originalDeviceType
                                ?: AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                            selectAudioDevice(deviceToRestore)
                            currentDeviceType = deviceToRestore
                            originalDeviceType = null
                            releaseWakeLock()
                        }
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // No-op
        }
    }
}
