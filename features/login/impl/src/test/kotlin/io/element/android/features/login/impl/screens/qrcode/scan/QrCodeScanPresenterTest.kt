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

package io.element.android.features.login.impl.screens.qrcode.scan

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.test.auth.qrlogin.FakeMatrixQrCodeLoginDataFactory
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
        val presenter = createQrCodeScanPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(QrCodeScanEvents.QrCodeScanned(byteArrayOf()))
            assertThat(awaitItem().isScanning).isFalse()
            assertThat(awaitItem().authenticationAction.isLoading()).isTrue()
            assertThat(awaitItem().authenticationAction.isSuccess()).isTrue()
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

    private fun TestScope.createQrCodeScanPresenter(
        qrCodeLoginDataFactory: FakeMatrixQrCodeLoginDataFactory = FakeMatrixQrCodeLoginDataFactory(),
        coroutineDispatchers: CoroutineDispatchers = testCoroutineDispatchers(),
    ) = QrCodeScanPresenter(
        qrCodeLoginDataFactory = qrCodeLoginDataFactory,
        coroutineDispatchers = coroutineDispatchers,
    )
}
