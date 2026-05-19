/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.linknewdevice.impl.screens.qrcode

import com.google.common.truth.Truth.assertThat
import io.element.android.features.linknewdevice.impl.LinkNewMobileHandler
import io.element.android.libraries.matrix.api.linknewdevice.LinkMobileHandler
import io.element.android.libraries.matrix.api.linknewdevice.LinkMobileStep
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.linknewdevice.FakeLinkMobileHandler
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ShowQrCodePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        createPresenter().test {
            val initialState = awaitItem()
            assertThat(initialState.data.dataOrNull()).isEqualTo("DATA")
        }
    }

    @Test
    fun `present - when handler emits QrRotating, the presenter requests to rotate the QrCode`() = runTest {
        val linkMobileHandler = FakeLinkMobileHandler(
            startResult = {},
        )
        val createLinkMobileHandlerResult = lambdaRecorder<Result<LinkMobileHandler>> {
            Result.success(linkMobileHandler)
        }
        val matrixClient = FakeMatrixClient(
            sessionCoroutineScope = backgroundScope,
            createLinkMobileHandlerResult = createLinkMobileHandlerResult,
        )
        val linkNewMobileHandler = LinkNewMobileHandler(matrixClient)
        linkNewMobileHandler.createAndStartNewHandler()
        createPresenter(
            linkNewMobileHandler = linkNewMobileHandler,
        ).test {
            awaitItem()
            linkMobileHandler.emitStep(
                LinkMobileStep.QrRotating
            )
            runCurrent()
            val finalState = awaitItem()
            assertThat(finalState.data.isLoading()).isTrue()
            createLinkMobileHandlerResult.assertions().isCalledExactly(2)
        }
    }

    @Test
    fun `present - when handler emits QrRotating, the presenter requests to rotate the QrCode and the code is rotated`() = runTest {
        val linkMobileHandler = FakeLinkMobileHandler(
            startResult = {},
        )
        val matrixClient = FakeMatrixClient(
            sessionCoroutineScope = backgroundScope,
            createLinkMobileHandlerResult = { Result.success(linkMobileHandler) },
        )
        val linkNewMobileHandler = LinkNewMobileHandler(matrixClient)
        linkNewMobileHandler.createAndStartNewHandler()
        createPresenter(
            linkNewMobileHandler = linkNewMobileHandler,
        ).test {
            awaitItem()
            linkMobileHandler.emitStep(
                LinkMobileStep.QrRotating
            )
            runCurrent()
            linkMobileHandler.emitStep(
                LinkMobileStep.QrReady("DATA2")
            )
            val finalState = awaitItem()
            assertThat(finalState.data.dataOrNull()).isEqualTo("DATA2")
        }
    }

    private fun createPresenter(
        linkNewMobileHandler: LinkNewMobileHandler = LinkNewMobileHandler(FakeMatrixClient()),
    ) = ShowQrCodePresenter(
        initialData = "DATA",
        linkNewMobileHandler = linkNewMobileHandler,
    )
}
