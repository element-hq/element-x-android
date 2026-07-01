/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.linknewdevice.impl.screens.number

import com.google.common.truth.Truth.assertThat
import io.element.android.features.linknewdevice.impl.LinkNewMobileHandler
import io.element.android.libraries.matrix.api.linknewdevice.LinkMobileStep
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.linknewdevice.FakeCheckCodeSender
import io.element.android.libraries.matrix.test.linknewdevice.FakeLinkMobileHandler
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class EnterNumberPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        createPresenter().test {
            val initialState = awaitItem()
            assertThat(initialState.number).isEmpty()
            assertThat(initialState.sendingCode.isUninitialized()).isTrue()
        }
    }

    @Test
    fun `present - enter numbers`() = runTest {
        createPresenter().test {
            val initialState = awaitItem()
            assertThat(initialState.number).isEmpty()
            initialState.eventSink(EnterNumberEvent.UpdateNumber("12"))
            val state2 = awaitItem()
            assertThat(state2.number).isEqualTo("12")
            // Non numeric characters are ignored
            state2.eventSink(EnterNumberEvent.UpdateNumber("1a"))
            val state3 = awaitItem()
            assertThat(state3.number).isEqualTo("1")
        }
    }

    @Test
    fun `present - continue in wrong state generates an error`() = runTest {
        createPresenter().test {
            val initialState = awaitItem()
            initialState.eventSink(EnterNumberEvent.Continue)
            val state2 = awaitItem()
            assertThat(state2.sendingCode.isFailure()).isTrue()
        }
    }

    @Test
    fun `present - continue when number is not valid invokes the navigator`() = runTest {
        val linkMobileHandler = FakeLinkMobileHandler(
            startResult = {},
        )
        val validateResult = lambdaRecorder<UByte, Boolean> { false }
        val checkCodeSender = FakeCheckCodeSender(
            validateResult = validateResult,
        )
        val matrixClient = FakeMatrixClient(
            sessionCoroutineScope = backgroundScope,
            createLinkMobileHandlerResult = { Result.success(linkMobileHandler) }
        )
        val linkNewMobileHandler = LinkNewMobileHandler(matrixClient)
        linkNewMobileHandler.createAndStartNewHandler()
        val navigateToWrongNumberErrorLambda = lambdaRecorder<Unit> { }
        val navigator = FakeEnterNumberNavigator(
            navigateToWrongNumberErrorLambda = navigateToWrongNumberErrorLambda,
        )
        createPresenter(
            navigator = navigator,
            linkNewMobileHandler = linkNewMobileHandler,
        ).test {
            skipItems(1)
            val initialState = awaitItem()
            linkMobileHandler.emitStep(
                LinkMobileStep.QrScanned(checkCodeSender)
            )
            runCurrent()
            initialState.eventSink(EnterNumberEvent.UpdateNumber("88"))
            skipItems(1)
            initialState.eventSink(EnterNumberEvent.Continue)
            skipItems(2)
            val finalState = awaitItem()
            assertThat(finalState.sendingCode.isLoading()).isTrue()
            advanceUntilIdle()
            validateResult.assertions().isCalledOnce().with(value(88.toUByte()))
            navigateToWrongNumberErrorLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - continue when the number is valid but sending fails`() = runTest {
        val linkMobileHandler = FakeLinkMobileHandler(
            startResult = {},
        )
        val validateResult = lambdaRecorder<UByte, Boolean> { true }
        val sendResult = lambdaRecorder<UByte, Result<Unit>> { Result.failure(AN_EXCEPTION) }
        val checkCodeSender = FakeCheckCodeSender(
            validateResult = validateResult,
            sendResult = sendResult,
        )
        val matrixClient = FakeMatrixClient(
            sessionCoroutineScope = backgroundScope,
            createLinkMobileHandlerResult = { Result.success(linkMobileHandler) }
        )
        val linkNewMobileHandler = LinkNewMobileHandler(matrixClient)
        linkNewMobileHandler.createAndStartNewHandler()
        createPresenter(
            linkNewMobileHandler = linkNewMobileHandler,
        ).test {
            val initialState = awaitItem()
            linkMobileHandler.emitStep(
                LinkMobileStep.QrScanned(checkCodeSender)
            )
            runCurrent()
            skipItems(1)
            initialState.eventSink(EnterNumberEvent.UpdateNumber("88"))
            skipItems(1)
            initialState.eventSink(EnterNumberEvent.Continue)
            skipItems(1)
            val loadingState = awaitItem()
            assertThat(loadingState.sendingCode.isLoading()).isTrue()
            val finalState = awaitItem()
            assertThat(finalState.sendingCode.isFailure()).isTrue()
            validateResult.assertions().isCalledOnce().with(value(88.toUByte()))
            sendResult.assertions().isCalledOnce().with(value(88.toUByte()))
        }
    }

    @Test
    fun `present - continue when the number is valid and sending is successful`() = runTest {
        val linkMobileHandler = FakeLinkMobileHandler(
            startResult = {},
        )
        val validateResult = lambdaRecorder<UByte, Boolean> { true }
        val sendResult = lambdaRecorder<UByte, Result<Unit>> { Result.success(Unit) }
        val checkCodeSender = FakeCheckCodeSender(
            validateResult = validateResult,
            sendResult = sendResult,
        )
        val matrixClient = FakeMatrixClient(
            sessionCoroutineScope = backgroundScope,
            createLinkMobileHandlerResult = { Result.success(linkMobileHandler) }
        )
        val linkNewMobileHandler = LinkNewMobileHandler(matrixClient)
        linkNewMobileHandler.createAndStartNewHandler()
        createPresenter(
            linkNewMobileHandler = linkNewMobileHandler,
        ).test {
            skipItems(1)
            val initialState = awaitItem()
            linkMobileHandler.emitStep(
                LinkMobileStep.QrScanned(checkCodeSender)
            )
            runCurrent()
            initialState.eventSink(EnterNumberEvent.UpdateNumber("88"))
            skipItems(1)
            initialState.eventSink(EnterNumberEvent.Continue)
            skipItems(2)
            val loadingState = awaitItem()
            assertThat(loadingState.sendingCode.isLoading()).isTrue()
            expectNoEvents()
            advanceUntilIdle()
            validateResult.assertions().isCalledOnce().with(value(88.toUByte()))
            sendResult.assertions().isCalledOnce().with(value(88.toUByte()))
        }
    }

    private fun createPresenter(
        navigator: EnterNumberNavigator = FakeEnterNumberNavigator(),
        linkNewMobileHandler: LinkNewMobileHandler = LinkNewMobileHandler(FakeMatrixClient()),
    ) = EnterNumberPresenter(
        navigator = navigator,
        linkNewMobileHandler = linkNewMobileHandler,
    )
}
