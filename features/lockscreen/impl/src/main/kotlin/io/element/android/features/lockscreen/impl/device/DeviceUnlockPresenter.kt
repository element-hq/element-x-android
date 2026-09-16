/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.device

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.lockscreen.impl.biometric.BiometricAuthenticatorManager
import io.element.android.features.lockscreen.impl.pin.PinCodeManager
import io.element.android.features.lockscreen.impl.unlock.PinUnlockHelper
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.flow.first

@Inject
class DeviceUnlockPresenter(
    private val pinUnlockHelper: PinUnlockHelper,
    private val biometricAuthenticatorManager: BiometricAuthenticatorManager,
    private val deviceUnlockCallbackHolder: DeviceUnlockCallbackHolder,
    private val pinCodeManager: PinCodeManager,
) : Presenter<DeviceUnlockState> {
    @Composable
    override fun present(): DeviceUnlockState {
        var showApplicationPinCode by remember {
            mutableStateOf(false)
        }

        val biometricUnlock = biometricAuthenticatorManager.rememberUnlockDeviceBiometricAuthenticator()
        val deviceUnlockCallback by deviceUnlockCallbackHolder.deviceUnlockCallback.collectAsState()
        val canUseDeviceUnlock = biometricAuthenticatorManager.canUseDeviceUnlock

        fun setUnlock(isUnlock: Boolean) {
            deviceUnlockCallback?.let {
                if (isUnlock) {
                    it.onUnlock()
                } else {
                    it.onCancel()
                }
            }
            showApplicationPinCode = false
            deviceUnlockCallbackHolder.onDone()
        }

        LaunchedEffect(biometricUnlock, canUseDeviceUnlock, deviceUnlockCallback) {
            if (deviceUnlockCallback != null) {
                if (canUseDeviceUnlock) {
                    biometricUnlock.setup()
                    biometricUnlock.authenticate()
                } else if (pinCodeManager.hasPinCode().first()) {
                    showApplicationPinCode = true
                } else {
                    // No security, unlock immediately
                    setUnlock(true)
                }
            }
        }

        pinUnlockHelper.OnUnlockEffect { isUnlock ->
            setUnlock(isUnlock)
        }

        fun handleEvent(event: DeviceUnlockEvent) {
            when (event) {
                DeviceUnlockEvent.CancelPinCode -> {
                    showApplicationPinCode = false
                    setUnlock(false)
                }
            }
        }

        return DeviceUnlockState(
            showApplicationPinCode = showApplicationPinCode,
            eventSink = ::handleEvent,
        )
    }
}
