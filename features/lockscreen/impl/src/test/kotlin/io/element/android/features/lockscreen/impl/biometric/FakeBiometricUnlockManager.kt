/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.biometric

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class FakeBiometricUnlockManager : BiometricUnlockManager {
    override var isDeviceSecured: Boolean = true
    override var hasAvailableAuthenticator: Boolean = false

    override fun addCallback(callback: BiometricUnlock.Callback) {
        // no-op
    }

    override fun removeCallback(callback: BiometricUnlock.Callback) {
        // no-op
    }

    @Composable
    override fun rememberBiometricUnlock(): BiometricUnlock {
        return remember {
            NoopBiometricUnlock()
        }
    }
}
