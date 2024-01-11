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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
        DisposableEffect(Unit) {
            val biometricUnlockCallback = object : DefaultBiometricUnlockCallback() {
                override fun onBiometricUnlockSuccess() {
                    onUnlock()
                }
            }
            val pinCodeVerifiedCallback = object : DefaultPinCodeManagerCallback() {
                override fun onPinCodeVerified() {
                    onUnlock()
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
