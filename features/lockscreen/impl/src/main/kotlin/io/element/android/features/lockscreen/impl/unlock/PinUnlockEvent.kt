/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.unlock

import io.element.android.features.lockscreen.impl.unlock.keypad.PinKeypadModel

sealed interface PinUnlockEvent {
    data class OnPinKeypadPressed(val pinKeypadModel: PinKeypadModel) : PinUnlockEvent
    data class OnPinEntryChanged(val entryAsText: String) : PinUnlockEvent
    data object OnForgetPin : PinUnlockEvent
    data object ClearSignOutPrompt : PinUnlockEvent
    data object SignOut : PinUnlockEvent
    data object OnUseBiometric : PinUnlockEvent
    data object ClearBiometricError : PinUnlockEvent
}
