/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.unlock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import dev.zacsweers.metro.Inject
import io.element.android.features.lockscreen.impl.biometric.BiometricAuthenticatorManager
import io.element.android.features.lockscreen.impl.biometric.DefaultBiometricUnlockCallback
import io.element.android.features.lockscreen.impl.pin.DefaultPinCodeManagerCallback
import io.element.android.features.lockscreen.impl.pin.PinCodeManager

@Inject
class PinUnlockHelper(
    private val biometricAuthenticatorManager: BiometricAuthenticatorManager,
    private val pinCodeManager: PinCodeManager
) {
    @Composable
    fun OnUnlockEffect(onUnlock: () -> Unit) {
        val latestOnUnlock by rememberUpdatedState(onUnlock)
        DisposableEffect(Unit) {
            val biometricUnlockCallback = object : DefaultBiometricUnlockCallback() {
                override fun onBiometricAuthenticationSuccess() {
                    latestOnUnlock()
                }
            }
            val pinCodeVerifiedCallback = object : DefaultPinCodeManagerCallback() {
                override fun onPinCodeVerified() {
                    latestOnUnlock()
                }
            }
            biometricAuthenticatorManager.addCallback(biometricUnlockCallback)
            pinCodeManager.addCallback(pinCodeVerifiedCallback)
            onDispose {
                biometricAuthenticatorManager.removeCallback(biometricUnlockCallback)
                pinCodeManager.removeCallback(pinCodeVerifiedCallback)
            }
        }
    }
}
