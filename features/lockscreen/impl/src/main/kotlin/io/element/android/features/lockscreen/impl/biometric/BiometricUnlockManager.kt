/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
