/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.audio

import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.webkit.WebView
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds

/**
 * This class manages the audio devices for a WebView.
 *
 * It listens for audio device changes and updates the WebView with the available devices.
 * It also handles the selection of the audio device by the user in the WebView and the default audio device based on the device type.
 *
 * See also: [Element Call controls docs.](https://github.com/element-hq/element-call/blob/livekit/docs/controls.md#audio-devices)
 */
class WebViewAudioManager(
    webView: WebView,
    private val coroutineScope: CoroutineScope,
) {
    private val webViewAudioController = WebViewAudioController(coroutineScope, webView)
    private val webViewProximitySensor = WebViewProximitySensor(webView.context)
    private val audioManager = requireNotNull(webView.context.getSystemService<AudioManager>())

    private val communicationDeviceChangedHandler = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        CommunicationDeviceChangedHandlerImpl31(audioManager)
    } else {
        CommunicationDeviceChangedHandler()
    }

    private val communicationDeviceChangedHandlerListener = object : CommunicationDeviceChangedHandlerListener {
        override fun onCommunicationDeviceChange(deviceId: Int?) {
            if (deviceId != null) {
                if (deviceId == expectedNewCommunicationDeviceId) {
                    expectedNewCommunicationDeviceId = null
                    webViewAudioController.updateSelectedAudioDevice(deviceId.toString())
                } else {
                    // We were expecting a device change but it didn't happen, so we should retry
                    val expectedDeviceId = expectedNewCommunicationDeviceId
                    if (expectedDeviceId != null) {
                        // Remove the expected id so we only retry once
                        expectedNewCommunicationDeviceId = null
                        selectAudioDevice(expectedDeviceId)
                    }
                }
            } else {
                expectedNewCommunicationDeviceId = null
                selectAudioDevice(null)
            }
        }
    }

    /**
     * This callback is used to listen for audio device changes coming from the OS.
     */
    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            val validNewDevices = addedDevices.orEmpty().filter { it.type in wantedDeviceTypes && it.isSink }
            if (validNewDevices.isEmpty()) return

            // We need to calculate the available devices ourselves, since calling `listAudioDevices` will return an outdated list
            val audioDevices = (listAudioDevices() + validNewDevices).distinctBy { it.id }
            webViewAudioController.setAvailableAudioDevices(audioDevices)
            // This should automatically switch to a new device if it has a higher priority than the current one
            selectDefaultAudioDevice(audioDevices)
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            // Update the available devices
            webViewAudioController.setAvailableAudioDevices(listAudioDevices())

            // Unless the removed device is the current one, we don't need to do anything else
            val removedCurrentDevice = removedDevices.orEmpty().any { it.id == currentDeviceId }
            if (!removedCurrentDevice) return

            val previousDevice = previousSelectedDevice
            if (previousDevice != null) {
                previousSelectedDevice = null
                // If we have a previous device, we should select it again
                selectAudioDevice(previousDevice.id)
            } else {
                // If we don't have a previous device, we should select the default one
                selectDefaultAudioDevice()
            }
        }
    }

    /**
     * The currently used audio device id.
     */
    private var currentDeviceId: Int? = null

    /**
     * When a new audio device is selected but not yet set as the communication device by the OS, this id is used to check if the device is the expected one.
     */
    private var expectedNewCommunicationDeviceId: Int? = null

    /**
     * Previously selected device, used to restore the selection when the selected device is removed.
     */
    private var previousSelectedDevice: AudioDeviceInfo? = null

    private var hasRegisteredCallbacks = false

    /**
     * Marks if the WebView audio is in call mode or not.
     */
    val isInCallMode = AtomicBoolean(false)

    init {
        // Apparently, registering the javascript interface takes a while, so registering and immediately using it doesn't work
        // We register it ahead of time to avoid this issue
        registerWebViewDeviceSelectedCallback()
    }

    /**
     * Call this method when the call starts to enable in-call audio mode.
     *
     * It'll set the audio mode to [AudioManager.MODE_IN_COMMUNICATION] if possible, register the audio device callback and set the available audio devices.
     */
    fun onCallStarted() {
        if (!isInCallMode.compareAndSet(false, true)) {
            Timber.w("Audio: tried to enable webview in-call audio mode while already in it")
            return
        }

        Timber.d("Audio: enabling webview in-call audio mode")

        audioManager.mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Set 'voice call' mode so volume keys actually control the call volume
            AudioManager.MODE_IN_COMMUNICATION
        } else {
            // Workaround for Android 12 and lower, otherwise changing the audio device doesn't work
            AudioManager.MODE_NORMAL
        }

        webViewAudioController.setWebViewAndroidNativeBridge()
    }

    /**
     * Call this method when the call stops to disable in-call audio mode.
     *
     * It's the counterpart of [onCallStarted], and should be called as a pair with it once the call has ended.
     */
    fun onCallStopped() {
        if (!isInCallMode.compareAndSet(true, false)) {
            Timber.w("Audio: tried to disable webview in-call audio mode while already disabled")
            return
        }

        webViewProximitySensor.release()

        audioManager.mode = AudioManager.MODE_NORMAL

        if (!hasRegisteredCallbacks) {
            Timber.w("Audio: tried to disable webview in-call audio mode without registering callbacks")
            return
        }
        communicationDeviceChangedHandler.stop()
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
    }

    /**
     * Registers the WebView audio device selected callback.
     *
     * This should be called when the WebView is created to ensure that the callback is set before any audio device selection is made.
     */
    private fun registerWebViewDeviceSelectedCallback() {
        val webViewAudioDeviceSelectedCallback = AndroidWebViewAudioBridge(
            onAudioDeviceSelected = { selectedDeviceId ->
                val selectedDeviceIdInt = selectedDeviceId.toIntOrNull() ?: run {
                    Timber.w("Audio device selected in webview with invalid id: $selectedDeviceId")
                    return@AndroidWebViewAudioBridge
                }
                Timber.d("Audio device selected in webview, id: $selectedDeviceId")
                previousSelectedDevice = listAudioDevices().find { it.id == selectedDeviceIdInt }
                selectAudioDevice(selectedDeviceIdInt)
            },
            onAudioPlaybackStarted = {
                coroutineScope.launch(Dispatchers.Main) {
                    // Even with the callback, it seems like starting the audio takes a bit on the webview side,
                    // so we add an extra delay here to make sure it's ready
                    delay(500.milliseconds)
                    // Calling this ahead of time makes the default audio device to not use the right audio stream
                    webViewAudioController.setAvailableAudioDevices(listAudioDevices())
                    // Registering the audio devices changed callback will also set the default audio device
                    audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
                    communicationDeviceChangedHandler.start(communicationDeviceChangedHandlerListener)
                    hasRegisteredCallbacks = true
                }
            }
        )
        webViewAudioController.init(webViewAudioDeviceSelectedCallback)
    }

    /**
     * Returns the list of available audio devices.
     *
     * On Android 11 ([Build.VERSION_CODES.R]) and lower, it returns the list of output devices as a fallback.
     */
    private fun listAudioDevices(): List<AudioDeviceInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioManager.availableCommunicationDevices
        } else {
            val rawAudioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            rawAudioDevices.filter { it.type in wantedDeviceTypes && it.isSink }
        }
    }

    /**
     * Selects the default audio device based on the available devices.
     *
     * @param availableDevices The list of available audio devices to select from. If not provided, it will use the current list of audio devices.
     */
    private fun selectDefaultAudioDevice(availableDevices: List<AudioDeviceInfo> = listAudioDevices()) {
        val selectedDevice = availableDevices.minByOrNull {
            wantedDeviceTypes.indexOf(it.type).let { index ->
                // If the device type is not in the wantedDeviceTypes list, we give it a low priority
                if (index == -1) Int.MAX_VALUE else index
            }
        }
        expectedNewCommunicationDeviceId = selectedDevice?.id
        selectAudioDevice(selectedDevice)
        selectedDevice?.let {
            webViewAudioController.updateSelectedAudioDevice(it.id.toString())
        } ?: run {
            Timber.w("Audio: unable to select default audio device")
        }
    }

    /**
     * Selects the audio device on the OS based on the provided device id.
     *
     * It will select the device only if it is available in the list of audio devices.
     *
     * @param deviceId The id of the audio device to select.
     */
    private fun selectAudioDevice(deviceId: Int) {
        val audioDevice = listAudioDevices().find { it.id == deviceId }
        selectAudioDevice(audioDevice)
    }

    /**
     * Selects the audio device on the OS based on the provided device info.
     *
     * @param device The info of the audio device to select, or none to clear the selected device.
     */
    private fun selectAudioDevice(device: AudioDeviceInfo?) {
        currentDeviceId = device?.id
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (device != null) {
                Timber.d("Setting communication device: ${device.id} - ${deviceName(device.type, device.productName)}")
                audioManager.setCommunicationDevice(device)
            } else {
                audioManager.clearCommunicationDevice()
            }
        } else {
            // On Android 11 and lower, we don't have the concept of communication devices
            // We have to call the right methods based on the device type
            if (device != null) {
                @Suppress("DEPRECATION")
                audioManager.isSpeakerphoneOn = device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                audioManager.isBluetoothScoOn = device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
            } else {
                @Suppress("DEPRECATION")
                audioManager.isSpeakerphoneOn = false
                audioManager.isBluetoothScoOn = false
            }
        }
        expectedNewCommunicationDeviceId = null
        if (device?.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE) {
            // If the device is the built-in earpiece, we need to acquire the proximity sensor wake lock
            webViewProximitySensor.acquire()
        } else {
            // If the device is no longer the earpiece, we need to release the wake lock
            webViewProximitySensor.release()
        }
    }

    companion object {
        // The list of device types that are considered as communication devices, sorted by likelihood of it being used for communication.
        private val wantedDeviceTypes = listOf(
            // Paired bluetooth device with microphone
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
            // USB devices which can play or record audio
            AudioDeviceInfo.TYPE_USB_HEADSET,
            AudioDeviceInfo.TYPE_USB_DEVICE,
            AudioDeviceInfo.TYPE_USB_ACCESSORY,
            // Wired audio devices
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            // The built-in speaker of the device
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
            // The built-in earpiece of the device
            AudioDeviceInfo.TYPE_BUILTIN_EARPIECE,
        )
    }
}
