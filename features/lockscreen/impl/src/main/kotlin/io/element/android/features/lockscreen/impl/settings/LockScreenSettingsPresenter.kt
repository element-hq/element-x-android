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

package io.element.android.features.lockscreen.impl.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.features.lockscreen.impl.LockScreenConfig
import io.element.android.features.lockscreen.impl.biometric.BiometricUnlockManager
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
    private val biometricUnlockManager: BiometricUnlockManager,
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
                        lockScreenStore.setIsBiometricUnlockAllowed(!isBiometricEnabled)
                    }
                }
            }
        }

        return LockScreenSettingsState(
            showRemovePinOption = showRemovePinOption,
            isBiometricEnabled = isBiometricEnabled,
            showRemovePinConfirmation = showRemovePinConfirmation,
            showToggleBiometric = biometricUnlockManager.isDeviceSecured,
            eventSink = ::handleEvents
        )
    }
}
