/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

sealed interface PttBluetoothSettingsEvent {
    data object Find : PttBluetoothSettingsEvent
    data object Connect : PttBluetoothSettingsEvent
    data object Disconnect : PttBluetoothSettingsEvent
    data object Forget : PttBluetoothSettingsEvent
    data object OpenBluetoothSettings : PttBluetoothSettingsEvent
    data object OpenAppSettings : PttBluetoothSettingsEvent
    data class SetAutoConnect(val enabled: Boolean) : PttBluetoothSettingsEvent
}
