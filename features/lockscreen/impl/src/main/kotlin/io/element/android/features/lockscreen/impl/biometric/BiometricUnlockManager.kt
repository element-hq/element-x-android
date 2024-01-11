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

package io.element.android.features.lockscreen.impl.biometric

import androidx.compose.runtime.Composable

interface BiometricUnlockManager {
    /**
     * If the device is secured for example with a pin, pattern or password.
     */
    val isDeviceSecured: Boolean

    /**
     * If the device has biometric hardware and if the user has enrolled at least one biometric.
     */
    val hasAvailableAuthenticator: Boolean

    fun addCallback(callback: BiometricUnlock.Callback)
    fun removeCallback(callback: BiometricUnlock.Callback)

    @Composable
    fun rememberBiometricUnlock(): BiometricUnlock
}
