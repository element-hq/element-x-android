/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.linknewdevice.impl.screens.scan

import com.google.common.truth.Truth.assertThat
import io.element.android.features.linknewdevice.impl.LinkNewDesktopHandler
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeDecodeException
import io.element.android.libraries.matrix.api.linknewdevice.LinkDesktopStep
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.QR_CODE_DATA_RECIPROCATE
import io.element.android.libraries.matrix.test.linknewdevice.FakeLinkDesktopHandler
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ScanQrCodePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val matrixClient = FakeMatrixClient(
            createLinkDesktopHandlerResult = { Result.success(FakeLinkDesktopHandler()) }
        )
        createPresenter(
            matrixClient = matrixClient,
        ).test {
            val initialState = awaitItem()
            assertThat(initialState.scanAction.isLoading()).isTrue()
        }
    }

    @Test
    fun `present - handle scanned event - success`() = runTest {
        val handleScannedQrCodeResult = lambdaRecorder<ByteArray, Unit> { }
        val matrixClient = FakeMatrixClient(
            sessionCoroutineScope = backgroundScope,
            createLinkDesktopHandlerResult = {
                Result.success(
                    FakeLinkDesktopHandler(
                        handleScannedQrCodeResult = handleScannedQrCodeResult,
                    )
                )
            }
        )
        createPresenter(
            matrixClient = matrixClient,
        ).test {
            val initialState = awaitItem()
            assertThat(initialState.scanAction.isLoading()).isTrue()
            initialState.eventSink(ScanQrCodeEvent.QrCodeScanned(QR_CODE_DATA_RECIPROCATE))
            val scannedState = awaitItem()
            assertThat(scannedState.scanAction.isSuccess()).isTrue()
            runCurrent()
            handleScannedQrCodeResult.assertions().isCalledOnce().with(value(QR_CODE_DATA_RECIPROCATE))
        }
    }

    @Test
    fun `present - handle scanned event - failure`() = runTest {
        val handleScannedQrCodeResult = lambdaRecorder<ByteArray, Unit> { }
        val handler = FakeLinkDesktopHandler(
            handleScannedQrCodeResult = handleScannedQrCodeResult,
        )
        val matrixClient = FakeMatrixClient(
            sessionCoroutineScope = backgroundScope,
            createLinkDesktopHandlerResult = {
                Result.success(handler)
            }
        )
        createPresenter(
            matrixClient = matrixClient,
        ).test {
            val initialState = awaitItem()
            assertThat(initialState.scanAction.isLoading()).isTrue()
            initialState.eventSink(ScanQrCodeEvent.QrCodeScanned(QR_CODE_DATA_RECIPROCATE))
            val scannedState = awaitItem()
            assertThat(scannedState.scanAction.isSuccess()).isTrue()
            handler.emitStep(LinkDesktopStep.InvalidQrCode(QrCodeDecodeException.Crypto("Invalid QR Code")))
            skipItems(1)
            val errorState = awaitItem()
            assertThat(errorState.scanAction.isFailure()).isTrue()
            handleScannedQrCodeResult.assertions().isCalledOnce().with(value(QR_CODE_DATA_RECIPROCATE))
            // Reset by trying again
            errorState.eventSink(ScanQrCodeEvent.TryAgain)
            val resetState = awaitItem()
            assertThat(resetState.scanAction.isLoading()).isTrue()
        }
    }
}

private fun createPresenter(
    matrixClient: MatrixClient,
) = ScanQrCodePresenter(
    linkNewDesktopHandler = LinkNewDesktopHandler(matrixClient),
)
