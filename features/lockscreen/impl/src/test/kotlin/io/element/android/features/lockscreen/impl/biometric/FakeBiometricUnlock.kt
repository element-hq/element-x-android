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

import io.element.android.tests.testutils.simulateLongTask

class FakeBiometricUnlock : BiometricUnlock {
    override val isActive = true

    override fun setup() = Unit

    private var authenticateResult: BiometricUnlock.AuthenticationResult = BiometricUnlock.AuthenticationResult.Success

    fun givenAuthenticateResult(result: BiometricUnlock.AuthenticationResult) {
        authenticateResult = result
    }

    override suspend fun authenticate(): BiometricUnlock.AuthenticationResult = simulateLongTask {
        return authenticateResult
    }
}
