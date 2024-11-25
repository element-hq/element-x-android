/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.biometric

class FakeBiometricAuthenticator(
    override val isActive: Boolean = false,
    private val authenticateLambda: suspend () -> BiometricAuthenticator.AuthenticationResult = { BiometricAuthenticator.AuthenticationResult.Success },
) : BiometricAuthenticator {
    override fun setup() = Unit
    override suspend fun authenticate() = authenticateLambda()
}
