/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.knockrequests.impl.list

import com.google.common.truth.Truth.assertThat
import io.element.android.features.knockrequests.impl.data.KnockRequestPermissions
import io.element.android.features.knockrequests.impl.data.KnockRequestsService
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.room.knock.KnockRequest
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.room.knock.FakeKnockRequest
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class KnockRequestsListPresenterTest {
    @Test
    fun `present - initial states should be emitted`() = runTest {
        val presenter = createKnockRequestsListPresenter()
        presenter.test {
            awaitItem().also { state ->
                assertThat(state.knockRequests).isInstanceOf(AsyncData.Loading::class.java)
                assertThat(state.permissions.canAccept).isFalse()
                assertThat(state.permissions.canDecline).isFalse()
                assertThat(state.permissions.canBan).isFalse()
            }
            awaitItem().also { state ->
                assertThat(state.knockRequests).isInstanceOf(AsyncData.Loading::class.java)
                assertThat(state.permissions.canAccept).isTrue()
                assertThat(state.permissions.canDecline).isTrue()
                assertThat(state.permissions.canBan).isTrue()
            }
            awaitItem().also { state ->
                assertThat(state.knockRequests).isInstanceOf(AsyncData.Success::class.java)
                assertThat(state.knockRequests.dataOrNull()).isEmpty()
            }
        }
    }

    @Test
    fun `present - accept success scenario`() = runTest {
        val acceptLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val knockRequest = FakeKnockRequest(acceptLambda = acceptLambda)
        val knockRequests = flowOf(listOf(knockRequest))
        val presenter = createKnockRequestsListPresenter(
            knockRequestsFlow = knockRequests
        )
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                val knockRequestPresentable = state.knockRequests.dataOrNull()?.first()!!
                state.eventSink(KnockRequestsListEvents.Accept(knockRequestPresentable))
            }
            skipItems(1)
            awaitItem().also { state ->
                val knockRequestPresentable = state.knockRequests.dataOrNull()?.first()!!
                assertThat(state.currentAction).isEqualTo(KnockRequestsAction.Accept(knockRequestPresentable))
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.Loading::class.java)
            }
            awaitItem().also { state ->
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.Success::class.java)
                state.eventSink(KnockRequestsListEvents.ResetCurrentAction)
            }
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
                assertThat(state.currentAction).isEqualTo(KnockRequestsAction.None)
                assertThat(state.knockRequests.dataOrNull().orEmpty()).isEmpty()
            }
            assert(acceptLambda).isCalledOnce()
        }
    }

    @Test
    fun `present - accept failure scenario`() = runTest {
        val acceptLambda = lambdaRecorder<Result<Unit>> { Result.failure(Exception()) }
        val knockRequest = FakeKnockRequest(acceptLambda = acceptLambda)
        val knockRequests = flowOf(listOf(knockRequest))
        val presenter = createKnockRequestsListPresenter(
            knockRequestsFlow = knockRequests
        )
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                val knockRequestPresentable = state.knockRequests.dataOrNull()?.first()!!
                state.eventSink(KnockRequestsListEvents.Accept(knockRequestPresentable))
            }
            skipItems(1)
            awaitItem().also { state ->
                val knockRequestPresentable = state.knockRequests.dataOrNull()?.first()!!
                assertThat(state.currentAction).isEqualTo(KnockRequestsAction.Accept(knockRequestPresentable))
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.Loading::class.java)
            }
            awaitItem().also { state ->
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.Failure::class.java)
                state.eventSink(KnockRequestsListEvents.RetryCurrentAction)
            }
            awaitItem().also { state ->
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.Loading::class.java)
            }
            awaitItem().also { state ->
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.Failure::class.java)
                state.eventSink(KnockRequestsListEvents.ResetCurrentAction)
            }
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
                assertThat(state.currentAction).isEqualTo(KnockRequestsAction.None)
                assertThat(state.knockRequests.dataOrNull()).hasSize(1)
            }
            assert(acceptLambda).isCalledExactly(2)
        }
    }

    @Test
    fun `present - decline success scenario`() = runTest {
        val declineLambda = lambdaRecorder<String?, Result<Unit>> { Result.success(Unit) }
        val knockRequest = FakeKnockRequest(declineLambda = declineLambda)
        val knockRequests = flowOf(listOf(knockRequest))
        val presenter = createKnockRequestsListPresenter(
            knockRequestsFlow = knockRequests
        )
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                val knockRequestPresentable = state.knockRequests.dataOrNull()?.first()!!
                state.eventSink(KnockRequestsListEvents.Decline(knockRequestPresentable))
            }
            skipItems(1)
            awaitItem().also { state ->
                val knockRequestPresentable = state.knockRequests.dataOrNull()?.first()!!
                assertThat(state.currentAction).isEqualTo(KnockRequestsAction.Decline(knockRequestPresentable))
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.ConfirmingNoParams::class.java)
                state.eventSink(KnockRequestsListEvents.ConfirmCurrentAction)
            }
            awaitItem().also { state ->
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.Loading::class.java)
            }
            awaitItem().also { state ->
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.Success::class.java)
                state.eventSink(KnockRequestsListEvents.ResetCurrentAction)
            }
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
                assertThat(state.currentAction).isEqualTo(KnockRequestsAction.None)
                assertThat(state.knockRequests.dataOrNull().orEmpty()).isEmpty()
            }
        }
        assert(declineLambda).isCalledOnce()
    }

    @Test
    fun `present - decline and ban success scenario`() = runTest {
        val declineAndBanLambda = lambdaRecorder<String?, Result<Unit>> { Result.success(Unit) }
        val knockRequest = FakeKnockRequest(declineAndBanLambda = declineAndBanLambda)
        val knockRequests = flowOf(listOf(knockRequest))
        val presenter = createKnockRequestsListPresenter(
            knockRequestsFlow = knockRequests
        )
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                val knockRequestPresentable = state.knockRequests.dataOrNull()?.first()!!
                state.eventSink(KnockRequestsListEvents.DeclineAndBan(knockRequestPresentable))
            }
            skipItems(1)
            awaitItem().also { state ->
                val knockRequestPresentable = state.knockRequests.dataOrNull()?.first()!!
                assertThat(state.currentAction).isEqualTo(KnockRequestsAction.DeclineAndBan(knockRequestPresentable))
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.ConfirmingNoParams::class.java)
                state.eventSink(KnockRequestsListEvents.ConfirmCurrentAction)
            }
            awaitItem().also { state ->
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.Loading::class.java)
            }
            awaitItem().also { state ->
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.Success::class.java)
                state.eventSink(KnockRequestsListEvents.ResetCurrentAction)
            }
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
                assertThat(state.currentAction).isEqualTo(KnockRequestsAction.None)
                assertThat(state.knockRequests.dataOrNull().orEmpty()).isEmpty()
            }
        }
        assert(declineAndBanLambda).isCalledOnce()
    }

    @Test
    fun `present - accept all success scenario`() = runTest {
        val acceptLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val knockRequests = flowOf(
            listOf(
                FakeKnockRequest(eventId = AN_EVENT_ID, acceptLambda = acceptLambda),
                FakeKnockRequest(eventId = AN_EVENT_ID_2, acceptLambda = acceptLambda),
            )
        )
        val presenter = createKnockRequestsListPresenter(
            knockRequestsFlow = knockRequests
        )
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.canAcceptAll).isTrue()
                state.eventSink(KnockRequestsListEvents.AcceptAll)
            }
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.currentAction).isEqualTo(KnockRequestsAction.AcceptAll)
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.ConfirmingNoParams::class.java)
                state.eventSink(KnockRequestsListEvents.ConfirmCurrentAction)
            }
            awaitItem().also { state ->
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.Loading::class.java)
            }
            awaitItem().also { state ->
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.Success::class.java)
                state.eventSink(KnockRequestsListEvents.ResetCurrentAction)
            }
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
                assertThat(state.currentAction).isEqualTo(KnockRequestsAction.None)
                assertThat(state.knockRequests.dataOrNull().orEmpty()).isEmpty()
            }
        }
        assert(acceptLambda).isCalledExactly(2)
    }

    @Test
    fun `present - accept all partial success scenario`() = runTest {
        val acceptSuccessLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val acceptFailureLambda = lambdaRecorder<Result<Unit>> { Result.failure(Exception()) }
        val knockRequests = flowOf(
            listOf(
                FakeKnockRequest(eventId = AN_EVENT_ID, acceptLambda = acceptSuccessLambda),
                FakeKnockRequest(eventId = AN_EVENT_ID_2, acceptLambda = acceptFailureLambda),
            )
        )
        val presenter = createKnockRequestsListPresenter(
            knockRequestsFlow = knockRequests
        )
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.canAcceptAll).isTrue()
                state.eventSink(KnockRequestsListEvents.AcceptAll)
            }
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.currentAction).isEqualTo(KnockRequestsAction.AcceptAll)
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.ConfirmingNoParams::class.java)
                state.eventSink(KnockRequestsListEvents.ConfirmCurrentAction)
            }
            awaitItem().also { state ->
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.Loading::class.java)
            }
            awaitItem().also { state ->
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.Failure::class.java)
                state.eventSink(KnockRequestsListEvents.ResetCurrentAction)
            }
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.asyncAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
                assertThat(state.currentAction).isEqualTo(KnockRequestsAction.None)
                assertThat(state.knockRequests.dataOrNull()).hasSize(1)
            }
        }
        assert(acceptFailureLambda).isCalledOnce()
        assert(acceptSuccessLambda).isCalledOnce()
    }

    private fun TestScope.createKnockRequestsListPresenter(
        canAccept: Boolean = true,
        canDecline: Boolean = true,
        canBan: Boolean = true,
        knockRequestsFlow: Flow<List<KnockRequest>> = flowOf(emptyList())
    ): KnockRequestsListPresenter {
        val knockRequestsService = KnockRequestsService(
            knockRequestsFlow = knockRequestsFlow,
            coroutineScope = backgroundScope,
            isKnockFeatureEnabledFlow = flowOf(true),
            permissionsFlow = flowOf(KnockRequestPermissions(canAccept, canDecline, canBan)),
        )
        return KnockRequestsListPresenter(knockRequestsService = knockRequestsService)
    }
}
