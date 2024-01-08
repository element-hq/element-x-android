/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.lockscreen.impl.unlock

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.lockscreen.impl.biometric.BiometricUnlock
import io.element.android.features.lockscreen.impl.pin.model.PinEntry
import io.element.android.libraries.architecture.AsyncData

open class PinUnlockStateProvider : PreviewParameterProvider<PinUnlockState> {
    override val values: Sequence<PinUnlockState>
        get() = sequenceOf(
            aPinUnlockState(),
            aPinUnlockState(pinEntry = PinEntry.createEmpty(4).fillWith("12")),
            aPinUnlockState(showWrongPinTitle = true),
            aPinUnlockState(showSignOutPrompt = true),
            aPinUnlockState(showBiometricUnlock = false),
            aPinUnlockState(showSignOutPrompt = true, remainingAttempts = 0),
            aPinUnlockState(signOutAction = AsyncData.Loading()),
        )
}

fun aPinUnlockState(
    pinEntry: PinEntry = PinEntry.createEmpty(4),
    remainingAttempts: Int = 3,
    showWrongPinTitle: Boolean = false,
    showSignOutPrompt: Boolean = false,
    showBiometricUnlock: Boolean = true,
    biometricUnlockResult: BiometricUnlock.AuthenticationResult? = null,
    isUnlocked: Boolean = false,
    signOutAction: AsyncData<String?> = AsyncData.Uninitialized,
) = PinUnlockState(
    pinEntry = AsyncData.Success(pinEntry),
    showWrongPinTitle = showWrongPinTitle,
    remainingAttempts = AsyncData.Success(remainingAttempts),
    showSignOutPrompt = showSignOutPrompt,
    showBiometricUnlock = showBiometricUnlock,
    signOutAction = signOutAction,
    biometricUnlockResult = biometricUnlockResult,
    isUnlocked = isUnlocked,
    eventSink = {}
)
