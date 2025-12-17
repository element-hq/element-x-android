/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.unlock

import androidx.biometric.BiometricPrompt
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.lockscreen.impl.biometric.BiometricAuthenticator
import io.element.android.features.lockscreen.impl.biometric.BiometricUnlockError
import io.element.android.features.lockscreen.impl.pin.model.PinEntry
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData

open class PinUnlockStateProvider : PreviewParameterProvider<PinUnlockState> {
    override val values: Sequence<PinUnlockState>
        get() = sequenceOf(
            aPinUnlockState(),
            aPinUnlockState(pinEntry = PinEntry.createEmpty(4).fillWith("12")),
            aPinUnlockState(showWrongPinTitle = true),
            aPinUnlockState(showSignOutPrompt = true),
            aPinUnlockState(showBiometricUnlock = false),
            aPinUnlockState(showSignOutPrompt = true, remainingAttempts = AsyncData.Success(0)),
            aPinUnlockState(signOutAction = AsyncAction.Loading),
            aPinUnlockState(
                biometricUnlockResult = BiometricAuthenticator.AuthenticationResult.Failure(
                    BiometricUnlockError(BiometricPrompt.ERROR_LOCKOUT, "Biometric auth disabled")
                )
            ),
        )
}

fun aPinUnlockState(
    pinEntry: PinEntry = PinEntry.createEmpty(4),
    remainingAttempts: AsyncData<Int> = AsyncData.Success(3),
    showWrongPinTitle: Boolean = false,
    showSignOutPrompt: Boolean = false,
    showBiometricUnlock: Boolean = true,
    biometricUnlockResult: BiometricAuthenticator.AuthenticationResult? = null,
    isUnlocked: Boolean = false,
    signOutAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
) = PinUnlockState(
    pinEntry = AsyncData.Success(pinEntry),
    showWrongPinTitle = showWrongPinTitle,
    remainingAttempts = remainingAttempts,
    showSignOutPrompt = showSignOutPrompt,
    showBiometricUnlock = showBiometricUnlock,
    signOutAction = signOutAction,
    biometricUnlockResult = biometricUnlockResult,
    isUnlocked = isUnlocked,
    eventSink = {}
)
