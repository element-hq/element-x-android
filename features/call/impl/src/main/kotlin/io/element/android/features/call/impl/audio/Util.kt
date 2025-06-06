/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.audio

import android.media.AudioDeviceInfo

fun deviceName(type: Int, name: CharSequence): String {
    // TODO maybe translate these?
    return when (type) {
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth - $name"
        AudioDeviceInfo.TYPE_USB_ACCESSORY -> "USB accessory - $name"
        AudioDeviceInfo.TYPE_USB_DEVICE -> "USB device - $name"
        AudioDeviceInfo.TYPE_USB_HEADSET -> "USB headset - $name"
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired headset - $name"
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired headphones"
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Built-in speaker"
        AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> "Built-in earpiece"
        else -> "Unknown"
    }
}
