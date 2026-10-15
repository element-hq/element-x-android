/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

data class PttSettingsState(
    val isCovert: Boolean,
    val overlayEnabled: Boolean,
    val lockScreenEnabled: Boolean,
    val isIgnoringBatteryOptimizations: Boolean,
    val bluetoothSupported: Boolean,
    val eventSink: (PttSettingsEvent) -> Unit,
)
