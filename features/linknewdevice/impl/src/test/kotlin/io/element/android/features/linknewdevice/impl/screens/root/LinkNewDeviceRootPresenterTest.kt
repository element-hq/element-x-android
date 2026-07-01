/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.root

import com.google.common.truth.Truth.assertThat
import io.element.android.features.linknewdevice.impl.LinkNewMobileHandler
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.linknewdevice.FakeLinkMobileHandler
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class LinkNewDeviceRootPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val matrixClient = FakeMatrixClient(
            canLinkNewDeviceResult = { Result.success(true) },
        )
        createPresenter(
            matrixClient = matrixClient,
        ).test {
            val initialState = awaitItem()
            assertThat(initialState.isSupported.isUninitialized()).isTrue()
            assertThat(awaitItem().isSupported.dataOrNull()).isTrue()
        }
    }

    @Test
    fun `present - new login device not supported`() = runTest {
        val matrixClient = FakeMatrixClient(
            canLinkNewDeviceResult = { Result.success(false) },
        )
        createPresenter(
            matrixClient = matrixClient,
        ).test {
            val initialState = awaitItem()
            assertThat(initialState.isSupported.isUninitialized()).isTrue()
            assertThat(awaitItem().isSupported.dataOrNull()).isFalse()
        }
    }

    @Test
    fun `present - error`() = runTest {
        val matrixClient = FakeMatrixClient(
            canLinkNewDeviceResult = { Result.failure(AN_EXCEPTION) },
        )
        createPresenter(
            matrixClient = matrixClient,
        ).test {
            val initialState = awaitItem()
            assertThat(initialState.isSupported.isUninitialized()).isTrue()
            assertThat(awaitItem().isSupported.isFailure()).isTrue()
        }
    }

    @Test
    fun `present - link new mobile device`() = runTest {
        val linkMobileHandler = FakeLinkMobileHandler(
            startResult = {},
        )
        val matrixClient = FakeMatrixClient(
            canLinkNewDeviceResult = { Result.success(true) },
            sessionCoroutineScope = backgroundScope,
            createLinkMobileHandlerResult = { Result.success(linkMobileHandler) }
        )
        val linkNewMobileHandler = LinkNewMobileHandler(matrixClient)
        createPresenter(
            matrixClient = matrixClient,
            linkNewMobileHandler = linkNewMobileHandler,
        ).test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.isSupported.dataOrNull()).isTrue()
            linkNewMobileHandler.createAndStartNewHandler()
            skipItems(1)
            val loadingState = awaitItem()
            assertThat(loadingState.qrCodeData.isLoading()).isTrue()
            skipItems(1)
        }
    }

    @Test
    fun `present - close dialog resets qrCodeData`() = runTest {
        val fakeLinkMobileHandler = FakeLinkMobileHandler(startResult = {})
        val matrixClient = FakeMatrixClient(
            canLinkNewDeviceResult = { Result.success(true) },
            sessionCoroutineScope = backgroundScope,
            createLinkMobileHandlerResult = { Result.success(fakeLinkMobileHandler) }
        )
        val linkNewMobileHandler = LinkNewMobileHandler(matrixClient)
        createPresenter(
            matrixClient = matrixClient,
            linkNewMobileHandler = linkNewMobileHandler,
        ).test {
            skipItems(1)
            linkNewMobileHandler.onTooManyRotation()
            var errorState = awaitItem()
            while (!errorState.qrCodeData.isFailure()) {
                errorState = awaitItem()
            }
            assertThat(errorState.qrCodeData.isFailure()).isTrue()
            errorState.eventSink(LinkNewDeviceRootEvent.CloseDialog)
            var resetState = awaitItem()
            while (!resetState.qrCodeData.isUninitialized()) {
                resetState = awaitItem()
            }
            assertThat(resetState.qrCodeData.isUninitialized()).isTrue()
        }
    }

    private fun createPresenter(
        matrixClient: MatrixClient = FakeMatrixClient(),
        linkNewMobileHandler: LinkNewMobileHandler = LinkNewMobileHandler(matrixClient),
    ) = LinkNewDeviceRootPresenter(
        matrixClient = matrixClient,
        linkNewMobileHandler = linkNewMobileHandler,
    )
}
