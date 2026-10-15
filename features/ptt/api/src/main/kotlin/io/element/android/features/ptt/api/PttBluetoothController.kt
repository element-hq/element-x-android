/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.api

import android.content.IntentSender
import kotlinx.coroutines.flow.StateFlow

/** Device-wide accessory setup. Implemented by the enterprise Bluetooth module. */
interface PttBluetoothController {
    val state: StateFlow<PttBluetoothState>
    fun findDevice(showChooser: (IntentSender) -> Unit)
    fun cancelSelection()
    fun connect()
    fun disconnect()
    fun forget()
    fun setAutoConnect(enabled: Boolean)

    /** While the setup screen is open, button events are displayed but never transmit audio. */
    fun enterSetup()
    fun leaveSetup()
}

data class PttBluetoothState(
    val deviceName: String?,
    val status: PttBluetoothStatus,
    val autoConnect: Boolean,
    val isPressed: Boolean,
    val eventCount: Int,
)

enum class PttBluetoothStatus {
    Disconnected,
    Selecting,
    Connecting,
    Ready,
    PermissionRequired,
    BluetoothOff,
    Unsupported,
    Failed,
}
