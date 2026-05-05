/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.libraries.matrix.impl.linknewdevice

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.linknewdevice.ErrorType
import io.element.android.libraries.matrix.api.linknewdevice.LinkDesktopStep
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiGrantLoginWithQrCodeHandler
import io.element.android.libraries.matrix.test.QR_CODE_DATA
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.GrantQrLoginProgress
import org.matrix.rustcomponents.sdk.HumanQrGrantLoginException
import org.matrix.rustcomponents.sdk.QrCodeDecodeException

class RustLinkDesktopHandlerTest {
    @Test
    fun `handleScannedQrCode function works as expected`() = runTest {
        val completable = CompletableDeferred<Unit>()
        val handler = FakeFfiGrantLoginWithQrCodeHandler(
            scanResult = {
                // Ensure that the coroutine is hold
                completable.await()
            }
        )
        val sut = createRustLinkDesktopHandler(
            handler,
        )
        sut.linkDesktopStep.test {
            val initialItem = awaitItem()
            assertThat(initialItem).isEqualTo(LinkDesktopStep.Uninitialized)
            backgroundScope.launch {
                sut.handleScannedQrCode(QR_CODE_DATA)
            }
            runCurrent()
            // progress from the handler is mapped and emitted
            listOf(
                GrantQrLoginProgress.Starting to LinkDesktopStep.Starting,
                GrantQrLoginProgress.SyncingSecrets to LinkDesktopStep.SyncingSecrets,
                GrantQrLoginProgress.WaitingForAuth("aVerificationUri")
                    to LinkDesktopStep.WaitingForAuth("aVerificationUri"),
                GrantQrLoginProgress.EstablishingSecureChannel(1.toUByte(), "1")
                    to LinkDesktopStep.EstablishingSecureChannel(1.toUByte(), "1"),
                GrantQrLoginProgress.Done to LinkDesktopStep.Done,
            ).forEach { (progress, expectedStep) ->
                handler.emitScanProgress(progress)
                assertThat(awaitItem()).isEqualTo(expectedStep)
            }
            // scan returns, no new event is emitted
            completable.complete(Unit)
            expectNoEvents()
        }
    }

    @Test
    fun `when scan does not emits the Done state, the code emits it`() = runTest {
        val completable = CompletableDeferred<Unit>()
        val handler = FakeFfiGrantLoginWithQrCodeHandler(
            scanResult = {
                // Ensure that the coroutine is hold
                completable.await()
            }
        )
        val sut = createRustLinkDesktopHandler(
            handler,
        )
        sut.linkDesktopStep.test {
            val initialItem = awaitItem()
            assertThat(initialItem).isEqualTo(LinkDesktopStep.Uninitialized)
            backgroundScope.launch {
                sut.handleScannedQrCode(QR_CODE_DATA)
            }
            runCurrent()
            handler.emitScanProgress(GrantQrLoginProgress.Starting)
            assertThat(awaitItem()).isEqualTo(LinkDesktopStep.Starting)
            // scan returns, Done event is emitted
            completable.complete(Unit)
            assertThat(awaitItem()).isEqualTo(LinkDesktopStep.Done)
        }
    }

    @Test
    fun `when handleScannedQrCode throws QrCodeDecodeException, the handler emits error step`() = runTest {
        val handler = FakeFfiGrantLoginWithQrCodeHandler(
            scanResult = { throw QrCodeDecodeException.Crypto("Scan failed") }
        )
        val sut = createRustLinkDesktopHandler(
            handler,
        )
        sut.linkDesktopStep.test {
            val initialItem = awaitItem()
            assertThat(initialItem).isEqualTo(LinkDesktopStep.Uninitialized)
            backgroundScope.launch {
                sut.handleScannedQrCode(QR_CODE_DATA)
            }
            runCurrent()
            val errorState = awaitItem()
            assertThat(errorState).isInstanceOf(LinkDesktopStep.InvalidQrCode::class.java)
        }
    }

    @Test
    fun `when handleScannedQrCode throws HumanQrGrantLoginException, the handler emits error step`() = runTest {
        val handler = FakeFfiGrantLoginWithQrCodeHandler(
            scanResult = { throw HumanQrGrantLoginException.InvalidCheckCode("Invalid check code") }
        )
        val sut = createRustLinkDesktopHandler(
            handler,
        )
        sut.linkDesktopStep.test {
            val initialItem = awaitItem()
            assertThat(initialItem).isEqualTo(LinkDesktopStep.Uninitialized)
            backgroundScope.launch {
                sut.handleScannedQrCode(QR_CODE_DATA)
            }
            runCurrent()
            val errorState = awaitItem()
            assertThat(errorState).isInstanceOf(LinkDesktopStep.Error::class.java)
            val errorType = (errorState as LinkDesktopStep.Error).errorType
            assertThat(errorType).isInstanceOf(ErrorType.InvalidCheckCode::class.java)
        }
    }

    private fun TestScope.createRustLinkDesktopHandler(
        handler: FakeFfiGrantLoginWithQrCodeHandler = FakeFfiGrantLoginWithQrCodeHandler(),
    ) = RustLinkDesktopHandler(
        inner = handler,
        sessionCoroutineScope = backgroundScope,
        sessionDispatcher = StandardTestDispatcher(testScheduler),
        qrCodeDataParser = FakeQrCodeDataParser(),
    )
}
