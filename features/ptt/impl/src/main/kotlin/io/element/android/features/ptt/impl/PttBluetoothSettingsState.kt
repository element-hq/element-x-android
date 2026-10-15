/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import io.element.android.features.ptt.api.PttBluetoothState

data class PttBluetoothSettingsState(
    val device: PttBluetoothState,
    val eventSink: (PttBluetoothSettingsEvent) -> Unit,
)
