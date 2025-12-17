/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.biometric

import androidx.compose.runtime.Composable

interface BiometricAuthenticatorManager {
    /**
     * If the device is secured for example with a pin, pattern or password.
     */
    val isDeviceSecured: Boolean

    /**
     * If the device has biometric hardware and if the user has enrolled at least one biometric.
     */
    val hasAvailableAuthenticator: Boolean

    fun addCallback(callback: BiometricAuthenticator.Callback)
    fun removeCallback(callback: BiometricAuthenticator.Callback)

    /**
     * Remember a biometric authenticator ready for unlocking the app.
     */
    @Composable
    fun rememberUnlockBiometricAuthenticator(): BiometricAuthenticator

    /**
     * Remember a biometric authenticator ready for confirmation.
     */
    @Composable
    fun rememberConfirmBiometricAuthenticator(): BiometricAuthenticator
}
