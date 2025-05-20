/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.scan

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.login.impl.changeserver.UnauthorizedAccountProviderException
import io.element.android.features.login.impl.qrcode.FakeQrCodeLoginManager
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import io.element.android.libraries.matrix.api.auth.qrlogin.QrLoginException
import io.element.android.libraries.matrix.test.auth.qrlogin.FakeMatrixQrCodeLoginData
import io.element.android.libraries.matrix.test.auth.qrlogin.FakeMatrixQrCodeLoginDataFactory
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class QrCodeScanPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createQrCodeScanPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().run {
                assertThat(isScanning).isTrue()
                assertThat(authenticationAction.isUninitialized()).isTrue()
            }
        }
    }

    @Test
    fun `present - scanned QR code successfully`() = runTest {
        val qrCodeLoginDataFactory = FakeMatrixQrCodeLoginDataFactory(
            parseQrCodeLoginDataResult = {
                Result.success(
                    FakeMatrixQrCodeLoginData(
                        serverNameResult = { "example.com" }
                    )
                )
            }
        )
        val presenter = createQrCodeScanPresenter(
            qrCodeLoginDataFactory = qrCodeLoginDataFactory,
            enterpriseService = FakeEnterpriseService(
                isAllowedToConnectToHomeserverResult = { true },
            )
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(QrCodeScanEvents.QrCodeScanned(byteArrayOf()))
            assertThat(awaitItem().isScanning).isFalse()
            assertThat(awaitItem().authenticationAction.isLoading()).isTrue()
            assertThat(awaitItem().authenticationAction.isSuccess()).isTrue()
        }
    }

    @Test
    fun `present - scanned QR code successfully, but homeserver not allowed`() = runTest {
        val qrCodeLoginDataFactory = FakeMatrixQrCodeLoginDataFactory(
            parseQrCodeLoginDataResult = {
                Result.success(
                    FakeMatrixQrCodeLoginData(
                        serverNameResult = { "example.com" }
                    )
                )
            }
        )
        val presenter = createQrCodeScanPresenter(
            qrCodeLoginDataFactory = qrCodeLoginDataFactory,
            enterpriseService = FakeEnterpriseService(
                isAllowedToConnectToHomeserverResult = { false },
                defaultHomeserverListResult = { listOf("element.io") },
            )
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(QrCodeScanEvents.QrCodeScanned(byteArrayOf()))
            assertThat(awaitItem().isScanning).isFalse()
            assertThat(awaitItem().authenticationAction.isLoading()).isTrue()
            awaitItem().also { state ->
                assertThat((state.authenticationAction.errorOrNull() as UnauthorizedAccountProviderException).unauthorisedAccountProviderTitle)
                    .isEqualTo("example.com")
                assertThat((state.authenticationAction.errorOrNull() as UnauthorizedAccountProviderException).authorisedAccountProviderTitles)
                    .containsExactly("element.io")
            }
        }
    }

    @Test
    fun `present - scanned QR code failed and can be retried`() = runTest {
        val qrCodeLoginDataFactory = FakeMatrixQrCodeLoginDataFactory(
            parseQrCodeLoginDataResult = { Result.failure(Exception("Failed to parse QR code")) }
        )
        val presenter = createQrCodeScanPresenter(qrCodeLoginDataFactory = qrCodeLoginDataFactory)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(QrCodeScanEvents.QrCodeScanned(byteArrayOf()))
            assertThat(awaitItem().isScanning).isFalse()
            assertThat(awaitItem().authenticationAction.isLoading()).isTrue()

            val errorState = awaitItem()
            assertThat(errorState.authenticationAction.isFailure()).isTrue()

            errorState.eventSink(QrCodeScanEvents.TryAgain)
            assertThat(awaitItem().isScanning).isTrue()
            assertThat(awaitItem().authenticationAction.isUninitialized()).isTrue()
        }
    }

    @Test
    fun `present - login failed with so we display the error and recover from it`() = runTest {
        val qrCodeLoginDataFactory = FakeMatrixQrCodeLoginDataFactory()
        val qrCodeLoginManager = FakeQrCodeLoginManager()
        val resetAction = lambdaRecorder<Unit> {
            qrCodeLoginManager.currentLoginStep.value = QrCodeLoginStep.Uninitialized
        }
        qrCodeLoginManager.resetAction = resetAction
        val presenter = createQrCodeScanPresenter(qrCodeLoginDataFactory = qrCodeLoginDataFactory, qrCodeLoginManager = qrCodeLoginManager)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Skip initial item
            skipItems(1)

            qrCodeLoginManager.currentLoginStep.value = QrCodeLoginStep.Failed(QrLoginException.OtherDeviceNotSignedIn)

            val errorState = awaitItem()
            // The state for this screen is failure
            assertThat(errorState.authenticationAction.isFailure()).isTrue()
            // However, the QrCodeLoginManager is reset
            resetAction.assertions().isCalledOnce()
            assertThat(qrCodeLoginManager.currentLoginStep.value).isEqualTo(QrCodeLoginStep.Uninitialized)
        }
    }

    private fun TestScope.createQrCodeScanPresenter(
        qrCodeLoginDataFactory: FakeMatrixQrCodeLoginDataFactory = FakeMatrixQrCodeLoginDataFactory(),
        coroutineDispatchers: CoroutineDispatchers = testCoroutineDispatchers(),
        qrCodeLoginManager: FakeQrCodeLoginManager = FakeQrCodeLoginManager(),
        enterpriseService: EnterpriseService = FakeEnterpriseService(),
    ) = QrCodeScanPresenter(
        qrCodeLoginDataFactory = qrCodeLoginDataFactory,
        qrCodeLoginManager = qrCodeLoginManager,
        coroutineDispatchers = coroutineDispatchers,
        enterpriseService = enterpriseService,
    )
}
