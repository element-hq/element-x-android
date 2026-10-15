/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.ptt.api.PttBluetoothState
import io.element.android.features.ptt.api.PttBluetoothStatus

class PttBluetoothSettingsStatePreviewParam : PreviewParameterProvider<PttBluetoothSettingsState> {
    override val values = sequenceOf(
        PttBluetoothSettingsState(
            device = PttBluetoothState(
                deviceName = null,
                status = PttBluetoothStatus.Disconnected,
                autoConnect = true,
                isPressed = false,
                eventCount = 0,
            ),
            eventSink = {},
        ),
    ) + PttBluetoothStatus.entries.asSequence().map {
        PttBluetoothSettingsState(
            device = PttBluetoothState(
                deviceName = "PTT-Z",
                status = it,
                autoConnect = true,
                isPressed = false,
                eventCount = 0,
            ),
            eventSink = {},
        )
    } + sequenceOf(
        PttBluetoothSettingsState(
            device = PttBluetoothState(
                deviceName = "PTT-Z",
                status = PttBluetoothStatus.Ready,
                isPressed = true,
                eventCount = 3,
                autoConnect = true,
            ),
            eventSink = {},
        ),
    )
}
