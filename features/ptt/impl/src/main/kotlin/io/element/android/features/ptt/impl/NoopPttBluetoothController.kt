/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import android.content.IntentSender
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.ptt.api.PttBluetoothController
import io.element.android.features.ptt.api.PttBluetoothState
import io.element.android.features.ptt.api.PttBluetoothStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@ContributesBinding(AppScope::class)
class NoopPttBluetoothController : PttBluetoothController {
    override val state = MutableStateFlow(
        PttBluetoothState(
            status = PttBluetoothStatus.Unsupported,
            deviceName = null,
            autoConnect = true,
            isPressed = false,
            eventCount = 0,
        ),
    ).asStateFlow()
    override fun findDevice(showChooser: (IntentSender) -> Unit) = Unit
    override fun cancelSelection() = Unit
    override fun connect() = Unit
    override fun disconnect() = Unit
    override fun forget() = Unit
    override fun setAutoConnect(enabled: Boolean) = Unit
    override fun enterSetup() = Unit
    override fun leaveSetup() = Unit
}
