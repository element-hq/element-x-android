/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.unlock

import io.element.android.features.lockscreen.impl.unlock.keypad.PinKeypadModel

sealed interface PinUnlockEvents {
    data class OnPinKeypadPressed(val pinKeypadModel: PinKeypadModel) : PinUnlockEvents
    data class OnPinEntryChanged(val entryAsText: String) : PinUnlockEvents
    data object OnForgetPin : PinUnlockEvents
    data object ClearSignOutPrompt : PinUnlockEvents
    data object SignOut : PinUnlockEvents
    data object OnUseBiometric : PinUnlockEvents
    data object ClearBiometricError : PinUnlockEvents
}
