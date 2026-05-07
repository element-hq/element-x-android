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
import io.element.android.libraries.matrix.api.linknewdevice.LinkMobileStep
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiCheckCodeSender
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiGrantLoginWithQrCodeHandler
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiQrCodeData
import io.element.android.libraries.matrix.test.QR_CODE_DATA_RECIPROCATE
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.GrantGeneratedQrLoginProgress
import org.matrix.rustcomponents.sdk.HumanQrGrantLoginException

class RustLinkMobileHandlerTest {
    @Test
    fun `start function works as expected`() = runTest {
        val completable = CompletableDeferred<Unit>()
        val handler = FakeFfiGrantLoginWithQrCodeHandler(
            generateResult = {
                // Ensure that the coroutine is hold
                completable.await()
            }
        )
        val sut = createRustLinkMobileHandler(
            handler,
        )
        sut.linkMobileStep.test {
            val initialItem = awaitItem()
            assertThat(initialItem).isEqualTo(LinkMobileStep.Uninitialized)
            backgroundScope.launch {
                sut.start()
            }
            runCurrent()
            // progress from the handler is mapped and emitted
            listOf(
                GrantGeneratedQrLoginProgress.Starting to LinkMobileStep.Starting::class.java,
                GrantGeneratedQrLoginProgress.SyncingSecrets to LinkMobileStep.SyncingSecrets::class.java,
                GrantGeneratedQrLoginProgress.WaitingForAuth("aVerificationUri")
                    to LinkMobileStep.WaitingForAuth::class.java,
                GrantGeneratedQrLoginProgress.QrScanned(FakeFfiCheckCodeSender())
                    to LinkMobileStep.QrScanned::class.java,
                GrantGeneratedQrLoginProgress.QrReady(FakeFfiQrCodeData(toBytesResult = { QR_CODE_DATA_RECIPROCATE }))
                    to LinkMobileStep.QrReady::class.java,
                GrantGeneratedQrLoginProgress.Done to LinkMobileStep.Done::class.java,
            ).forEach { (progress, expectedStepClass) ->
                handler.emitGenerateProgress(progress)
                assertThat(awaitItem()).isInstanceOf(expectedStepClass)
            }
            // generate returns, no new event is emitted
            completable.complete(Unit)
            expectNoEvents()
        }
    }

    @Test
    fun `when generates does not emits the Done state, the code emits it`() = runTest {
        val completable = CompletableDeferred<Unit>()
        val handler = FakeFfiGrantLoginWithQrCodeHandler(
            generateResult = {
                // Ensure that the coroutine is hold
                completable.await()
            }
        )
        val sut = createRustLinkMobileHandler(
            handler,
        )
        sut.linkMobileStep.test {
            val initialItem = awaitItem()
            assertThat(initialItem).isEqualTo(LinkMobileStep.Uninitialized)
            backgroundScope.launch {
                sut.start()
            }
            runCurrent()
            handler.emitGenerateProgress(GrantGeneratedQrLoginProgress.Starting)
            assertThat(awaitItem()).isEqualTo(LinkMobileStep.Starting)
            // generate returns, Done event is emitted
            completable.complete(Unit)
            assertThat(awaitItem()).isEqualTo(LinkMobileStep.Done)
        }
    }

    @Test
    fun `when start throws HumanQrGrantLoginException, the handler emits error step`() = runTest {
        val handler = FakeFfiGrantLoginWithQrCodeHandler(
            generateResult = { throw HumanQrGrantLoginException.NotFound("Timeout") }
        )
        val sut = createRustLinkMobileHandler(
            handler,
        )
        sut.linkMobileStep.test {
            val initialItem = awaitItem()
            assertThat(initialItem).isEqualTo(LinkMobileStep.Uninitialized)
            backgroundScope.launch {
                sut.start()
            }
            runCurrent()
            val errorState = awaitItem()
            assertThat(errorState).isInstanceOf(LinkMobileStep.Error::class.java)
            val errorType = (errorState as LinkMobileStep.Error).errorType
            assertThat(errorType).isInstanceOf(ErrorType.NotFound::class.java)
        }
    }

    private fun TestScope.createRustLinkMobileHandler(
        handler: FakeFfiGrantLoginWithQrCodeHandler = FakeFfiGrantLoginWithQrCodeHandler(),
    ) = RustLinkMobileHandler(
        inner = handler,
        sessionCoroutineScope = backgroundScope,
        sessionDispatcher = StandardTestDispatcher(testScheduler),
    )
}
