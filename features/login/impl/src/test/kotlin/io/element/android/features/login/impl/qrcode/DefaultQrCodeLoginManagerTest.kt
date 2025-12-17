/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.qrcode

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import io.element.android.libraries.matrix.test.auth.qrlogin.FakeMatrixQrCodeLoginData
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultQrCodeLoginManagerTest {
    @Test
    fun `authenticate - returns success if the login succeeded`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            loginWithQrCodeResult = { _, _ -> Result.success(A_SESSION_ID) }
        )
        val manager = DefaultQrCodeLoginManager(authenticationService)
        val result = manager.authenticate(FakeMatrixQrCodeLoginData())

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(A_SESSION_ID)
    }

    @Test
    fun `authenticate - returns failure if the login failed`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            loginWithQrCodeResult = { _, _ -> Result.failure(IllegalStateException("Auth failed")) }
        )
        val manager = DefaultQrCodeLoginManager(authenticationService)
        val result = manager.authenticate(FakeMatrixQrCodeLoginData())

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `authenticate - emits the auth steps`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
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
            manager.authenticate(FakeMatrixQrCodeLoginData())

            assertThat(awaitItem()).isEqualTo(QrCodeLoginStep.Uninitialized)
            assertThat(awaitItem()).isEqualTo(QrCodeLoginStep.EstablishingSecureChannel("00"))
            assertThat(awaitItem()).isEqualTo(QrCodeLoginStep.Starting)
            assertThat(awaitItem()).isEqualTo(QrCodeLoginStep.WaitingForToken("000000"))
            assertThat(awaitItem()).isEqualTo(QrCodeLoginStep.Finished)
        }
    }
}
