/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.biometric

import io.element.android.tests.testutils.simulateLongTask

class FakeBiometricAuthenticator(
    override val isActive: Boolean = false,
    private val setupLambda: () -> Unit = { },
    private val authenticateLambda: () -> BiometricAuthenticator.AuthenticationResult = { BiometricAuthenticator.AuthenticationResult.Success },
) : BiometricAuthenticator {
    override suspend fun setup() = simulateLongTask {
        setupLambda()
    }

    override suspend fun authenticate() = simulateLongTask {
        authenticateLambda()
    }
}
