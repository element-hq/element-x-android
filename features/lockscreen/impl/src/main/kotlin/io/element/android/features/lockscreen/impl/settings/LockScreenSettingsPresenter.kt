/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.features.lockscreen.impl.LockScreenConfig
import io.element.android.features.lockscreen.impl.biometric.BiometricAuthenticator
import io.element.android.features.lockscreen.impl.biometric.BiometricAuthenticatorManager
import io.element.android.features.lockscreen.impl.pin.PinCodeManager
import io.element.android.features.lockscreen.impl.storage.LockScreenStore
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class LockScreenSettingsPresenter @Inject constructor(
    private val lockScreenConfig: LockScreenConfig,
    private val pinCodeManager: PinCodeManager,
    private val lockScreenStore: LockScreenStore,
    private val biometricAuthenticatorManager: BiometricAuthenticatorManager,
    private val coroutineScope: CoroutineScope,
) : Presenter<LockScreenSettingsState> {
    @Composable
    override fun present(): LockScreenSettingsState {
        val showRemovePinOption by produceState(initialValue = false) {
            pinCodeManager.hasPinCode().collect { hasPinCode ->
                value = !lockScreenConfig.isPinMandatory && hasPinCode
            }
        }
        val isBiometricEnabled by lockScreenStore.isBiometricUnlockAllowed().collectAsState(initial = false)
        var showRemovePinConfirmation by remember {
            mutableStateOf(false)
        }

        val biometricUnlock = biometricAuthenticatorManager.rememberConfirmBiometricAuthenticator()

        fun handleEvents(event: LockScreenSettingsEvents) {
            when (event) {
                LockScreenSettingsEvents.CancelRemovePin -> showRemovePinConfirmation = false
                LockScreenSettingsEvents.ConfirmRemovePin -> {
                    coroutineScope.launch {
                        if (showRemovePinConfirmation) {
                            showRemovePinConfirmation = false
                            pinCodeManager.deletePinCode()
                        }
                    }
                }
                LockScreenSettingsEvents.OnRemovePin -> showRemovePinConfirmation = true
                LockScreenSettingsEvents.ToggleBiometricAllowed -> {
                    coroutineScope.launch {
                        if (!isBiometricEnabled) {
                            biometricUnlock.setup()
                            if (biometricUnlock.authenticate() == BiometricAuthenticator.AuthenticationResult.Success) {
                                lockScreenStore.setIsBiometricUnlockAllowed(true)
                            }
                        } else {
                            lockScreenStore.setIsBiometricUnlockAllowed(false)
                        }
                    }
                }
            }
        }

        return LockScreenSettingsState(
            showRemovePinOption = showRemovePinOption,
            isBiometricEnabled = isBiometricEnabled,
            showRemovePinConfirmation = showRemovePinConfirmation,
            showToggleBiometric = biometricAuthenticatorManager.isDeviceSecured,
            eventSink = ::handleEvents
        )
    }
}
