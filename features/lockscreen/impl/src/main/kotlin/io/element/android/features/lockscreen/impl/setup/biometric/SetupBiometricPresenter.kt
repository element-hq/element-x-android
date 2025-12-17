/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.setup.biometric

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.lockscreen.impl.biometric.BiometricAuthenticator
import io.element.android.features.lockscreen.impl.biometric.BiometricAuthenticatorManager
import io.element.android.features.lockscreen.impl.storage.LockScreenStore
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.launch

@Inject
class SetupBiometricPresenter(
    private val lockScreenStore: LockScreenStore,
    private val biometricAuthenticatorManager: BiometricAuthenticatorManager,
) : Presenter<SetupBiometricState> {
    @Composable
    override fun present(): SetupBiometricState {
        var isBiometricSetupDone by remember {
            mutableStateOf(false)
        }

        val coroutineScope = rememberCoroutineScope()
        val biometricUnlock = biometricAuthenticatorManager.rememberConfirmBiometricAuthenticator()

        fun handleEvent(event: SetupBiometricEvents) {
            when (event) {
                SetupBiometricEvents.AllowBiometric -> coroutineScope.launch {
                    biometricUnlock.setup()
                    if (biometricUnlock.authenticate() == BiometricAuthenticator.AuthenticationResult.Success) {
                        lockScreenStore.setIsBiometricUnlockAllowed(true)
                        isBiometricSetupDone = true
                    }
                }
                SetupBiometricEvents.UsePin -> coroutineScope.launch {
                    lockScreenStore.setIsBiometricUnlockAllowed(false)
                    isBiometricSetupDone = true
                }
            }
        }

        return SetupBiometricState(
            isBiometricSetupDone = isBiometricSetupDone,
            eventSink = ::handleEvent,
        )
    }
}
