/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.unlock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import io.element.android.features.lockscreen.impl.biometric.BiometricUnlockManager
import io.element.android.features.lockscreen.impl.biometric.DefaultBiometricUnlockCallback
import io.element.android.features.lockscreen.impl.pin.DefaultPinCodeManagerCallback
import io.element.android.features.lockscreen.impl.pin.PinCodeManager
import javax.inject.Inject

class PinUnlockHelper @Inject constructor(
    private val biometricUnlockManager: BiometricUnlockManager,
    private val pinCodeManager: PinCodeManager
) {
    @Composable
    fun OnUnlockEffect(onUnlock: () -> Unit) {
        val latestOnUnlock by rememberUpdatedState(onUnlock)
        DisposableEffect(Unit) {
            val biometricUnlockCallback = object : DefaultBiometricUnlockCallback() {
                override fun onBiometricUnlockSuccess() {
                    latestOnUnlock()
                }
            }
            val pinCodeVerifiedCallback = object : DefaultPinCodeManagerCallback() {
                override fun onPinCodeVerified() {
                    latestOnUnlock()
                }
            }
            biometricUnlockManager.addCallback(biometricUnlockCallback)
            pinCodeManager.addCallback(pinCodeVerifiedCallback)
            onDispose {
                biometricUnlockManager.removeCallback(biometricUnlockCallback)
                pinCodeManager.removeCallback(pinCodeVerifiedCallback)
            }
        }
    }
}
