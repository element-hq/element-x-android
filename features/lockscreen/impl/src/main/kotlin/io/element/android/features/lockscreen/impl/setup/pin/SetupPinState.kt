/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.setup.pin

import io.element.android.features.lockscreen.impl.pin.model.PinEntry
import io.element.android.features.lockscreen.impl.setup.pin.validation.SetupPinFailure

data class SetupPinState(
    val choosePinEntry: PinEntry,
    val confirmPinEntry: PinEntry,
    val isConfirmationStep: Boolean,
    val setupPinFailure: SetupPinFailure?,
    val appName: String,
    val eventSink: (SetupPinEvents) -> Unit
) {
    val activePinEntry = if (isConfirmationStep) {
        confirmPinEntry
    } else {
        choosePinEntry
    }
}
