/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.biometric

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class FakeBiometricAuthenticatorManager(
    override var isDeviceSecured: Boolean = true,
    override var hasAvailableAuthenticator: Boolean = false,
    private val createBiometricAuthenticator: () -> BiometricAuthenticator = { FakeBiometricAuthenticator() },
) : BiometricAuthenticatorManager {
    override fun addCallback(callback: BiometricAuthenticator.Callback) {
        // no-op
    }

    override fun removeCallback(callback: BiometricAuthenticator.Callback) {
        // no-op
    }

    @Composable
    override fun rememberUnlockBiometricAuthenticator(): BiometricAuthenticator {
        return remember {
           createBiometricAuthenticator()
        }
    }

    @Composable
    override fun rememberConfirmBiometricAuthenticator(): BiometricAuthenticator {
        return remember {
            createBiometricAuthenticator()
        }
    }
}
