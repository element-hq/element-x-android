/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.biometric

open class DefaultBiometricUnlockCallback : BiometricAuthenticator.Callback {
    override fun onBiometricSetupError() = Unit
    override fun onBiometricAuthenticationSuccess() = Unit
    override fun onBiometricAuthenticationFailed(error: Exception?) = Unit
}
