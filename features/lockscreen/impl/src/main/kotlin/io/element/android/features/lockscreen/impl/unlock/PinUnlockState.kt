/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.unlock

import io.element.android.features.lockscreen.impl.biometric.BiometricUnlock
import io.element.android.features.lockscreen.impl.biometric.BiometricUnlockError
import io.element.android.features.lockscreen.impl.pin.model.PinEntry
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData

data class PinUnlockState(
    val pinEntry: AsyncData<PinEntry>,
    val showWrongPinTitle: Boolean,
    val remainingAttempts: AsyncData<Int>,
    val showSignOutPrompt: Boolean,
    val signOutAction: AsyncAction<String?>,
    val showBiometricUnlock: Boolean,
    val isUnlocked: Boolean,
    val biometricUnlockResult: BiometricUnlock.AuthenticationResult?,
    val eventSink: (PinUnlockEvents) -> Unit
) {
    val isSignOutPromptCancellable = when (remainingAttempts) {
        is AsyncData.Success -> remainingAttempts.data > 0
        else -> true
    }

    val biometricUnlockErrorMessage = when {
        biometricUnlockResult is BiometricUnlock.AuthenticationResult.Failure &&
            biometricUnlockResult.error is BiometricUnlockError &&
            biometricUnlockResult.error.isAuthDisabledError -> {
            biometricUnlockResult.error.message
        }
        else -> null
    }
    val showBiometricUnlockError = biometricUnlockErrorMessage != null
}
