/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.login.impl.qrcode

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.auth.FakeAuthenticationService
import io.element.android.libraries.matrix.test.auth.qrlogin.FakeQrCodeLoginData
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultQrCodeLoginManagerTest {
    @Test
    fun `authenticate - returns success if the login succeeded`() = runTest {
        val authenticationService = FakeAuthenticationService(
            loginWithQrCodeResult = { _, _ -> Result.success(A_SESSION_ID) }
        )
        val manager = DefaultQrCodeLoginManager(authenticationService)
        val result = manager.authenticate(FakeQrCodeLoginData())

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(A_SESSION_ID)
    }

    @Test
    fun `authenticate - returns failure if the login failed`() = runTest {
        val authenticationService = FakeAuthenticationService(
            loginWithQrCodeResult = { _, _ -> Result.failure(IllegalStateException("Auth failed")) }
        )
        val manager = DefaultQrCodeLoginManager(authenticationService)
        val result = manager.authenticate(FakeQrCodeLoginData())

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `authenticate - emits the auth steps`() = runTest {
        val authenticationService = FakeAuthenticationService(
            loginWithQrCodeResult = { _, progressListener ->
                progressListener(QrCodeLoginStep.EstablishingSecureChannel("00"))
                progressListener(QrCodeLoginStep.Starting)
                progressListener(QrCodeLoginStep.WaitingForToken("000000"))
                progressListener(QrCodeLoginStep.Finished)
                Result.success(A_SESSION_ID)
            }
        )
        val manager = DefaultQrCodeLoginManager(authenticationService)
        manager.currentLoginStep.test {
            manager.authenticate(FakeQrCodeLoginData())

            assertThat(awaitItem()).isEqualTo(QrCodeLoginStep.Uninitialized)
            assertThat(awaitItem()).isEqualTo(QrCodeLoginStep.EstablishingSecureChannel("00"))
            assertThat(awaitItem()).isEqualTo(QrCodeLoginStep.Starting)
            assertThat(awaitItem()).isEqualTo(QrCodeLoginStep.WaitingForToken("000000"))
            assertThat(awaitItem()).isEqualTo(QrCodeLoginStep.Finished)
        }
    }
}
