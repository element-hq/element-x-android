/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.libraries.troubleshoot.impl.history

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.push.test.FakePushService
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PushHistoryPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPushHistoryPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.pushCounter).isEqualTo(0)
            assertThat(initialState.pushHistoryItems).isEmpty()
            assertThat(initialState.showOnlyErrors).isFalse()
            assertThat(initialState.resetAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - updating state`() = runTest {
        val pushService = FakePushService()
        val presenter = createPushHistoryPresenter(
            pushService = pushService,
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.pushCounter).isEqualTo(0)
            assertThat(initialState.pushHistoryItems).isEmpty()
            pushService.emitPushCounter(1)
            assertThat(awaitItem().pushCounter).isEqualTo(1)
            val item = aPushHistoryItem()
            pushService.emitPushHistoryItems(listOf(item))
            assertThat(awaitItem().pushHistoryItems).containsExactly(item)
        }
    }

    @Test
    fun `present - reset and cancel`() = runTest {
        val resetPushHistoryResult = lambdaRecorder<Unit> { }
        val pushService = FakePushService(
            resetPushHistoryResult = resetPushHistoryResult,
        )
        val presenter = createPushHistoryPresenter(
            pushService = pushService,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(PushHistoryEvents.Reset(requiresConfirmation = true))
            assertThat(awaitItem().resetAction).isEqualTo(AsyncAction.ConfirmingNoParams)
            initialState.eventSink(PushHistoryEvents.ClearDialog)
            assertThat(awaitItem().resetAction).isEqualTo(AsyncAction.Uninitialized)
            resetPushHistoryResult.assertions().isNeverCalled()
        }
    }

    @Test
    fun `present - reset and confirm`() = runTest {
        val resetPushHistoryResult = lambdaRecorder<Unit> { }
        val pushService = FakePushService(
            resetPushHistoryResult = resetPushHistoryResult,
        )
        val presenter = createPushHistoryPresenter(
            pushService = pushService,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(PushHistoryEvents.Reset(requiresConfirmation = true))
            assertThat(awaitItem().resetAction).isEqualTo(AsyncAction.ConfirmingNoParams)
            initialState.eventSink(PushHistoryEvents.Reset(requiresConfirmation = false))
            assertThat(awaitItem().resetAction).isEqualTo(AsyncAction.Loading)
            assertThat(awaitItem().resetAction).isEqualTo(AsyncAction.Uninitialized)
            resetPushHistoryResult.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - set show only errors`() = runTest {
        val pushService = FakePushService()
        val presenter = createPushHistoryPresenter(
            pushService = pushService,
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.showOnlyErrors).isFalse()
            val item = aPushHistoryItem(hasBeenResolved = true)
            val itemError = aPushHistoryItem(hasBeenResolved = false)
            pushService.emitPushHistoryItems(listOf(item, itemError))
            awaitItem().let { state ->
                assertThat(state.pushHistoryItems).containsExactly(item, itemError)
                state.eventSink(PushHistoryEvents.SetShowOnlyErrors(showOnlyErrors = true))
            }
            skipItems(1)
            awaitItem().let { state ->
                assertThat(state.showOnlyErrors).isTrue()
                assertThat(state.pushHistoryItems).containsExactly(itemError)
                state.eventSink(PushHistoryEvents.SetShowOnlyErrors(showOnlyErrors = false))
            }
            skipItems(1)
            awaitItem().let { state ->
                assertThat(state.showOnlyErrors).isFalse()
                assertThat(state.pushHistoryItems).containsExactly(item, itemError)
            }
        }
    }

    private fun createPushHistoryPresenter(
        pushService: PushService = FakePushService(),
    ): PushHistoryPresenter {
        return PushHistoryPresenter(
            pushService = pushService,
        )
    }
}
