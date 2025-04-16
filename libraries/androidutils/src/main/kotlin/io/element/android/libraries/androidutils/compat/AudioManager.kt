/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.compat

import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import io.element.android.libraries.core.data.tryOrNull
import timber.log.Timber

fun AudioManager.enableExternalAudioDevice() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // The list of device types that are considered as communication devices, sorted by likelihood of it being used for communication.
        val wantedDeviceTypes = listOf(
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
        val devices = availableCommunicationDevices
        val selectedDevice = devices.minByOrNull {
            wantedDeviceTypes.indexOf(it.type).let { index ->
                // If the device type is not in the wantedDeviceTypes list, we give it a low priority
                if (index == -1) Int.MAX_VALUE else index
            }
        }
        selectedDevice?.let { device ->
            Timber.d("Audio device selected, type: ${device.type}")
            tryOrNull(
                onError = { failure ->
                    Timber.e(failure, "Audio: exception when setting communication device")
                }
            ) {
                setCommunicationDevice(device).also {
                    if (!it) {
                        Timber.w("Audio: unable to set the communication device")
                    }
                }
            }
        }
    } else {
        // If we don't have access to the new APIs, use the deprecated ones
        @Suppress("DEPRECATION")
        isSpeakerphoneOn = true
    }
}

fun AudioManager.disableExternalAudioDevice() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        clearCommunicationDevice()
    } else {
        // If we don't have access to the new APIs, use the deprecated ones
        @Suppress("DEPRECATION")
        isSpeakerphoneOn = false
    }
}
