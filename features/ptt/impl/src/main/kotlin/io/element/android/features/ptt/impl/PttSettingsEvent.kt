/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

sealed interface PttSettingsEvent {
    data class SetCovert(val enabled: Boolean) : PttSettingsEvent
    data class SetOverlay(val enabled: Boolean) : PttSettingsEvent
    data class SetLockScreen(val enabled: Boolean) : PttSettingsEvent
    data object ToggleBatteryExemption : PttSettingsEvent
    data object RefreshPermissions : PttSettingsEvent
    data object OpenAppSettings : PttSettingsEvent
}
