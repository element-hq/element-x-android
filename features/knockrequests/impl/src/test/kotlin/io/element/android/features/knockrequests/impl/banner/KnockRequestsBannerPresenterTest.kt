/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.banner

import com.google.common.truth.Truth.assertThat
import io.element.android.features.knockrequests.impl.data.KnockRequestPermissions
import io.element.android.features.knockrequests.impl.data.KnockRequestsService
import io.element.android.libraries.matrix.api.room.knock.KnockRequest
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID_3
import io.element.android.libraries.matrix.test.room.knock.FakeKnockRequest
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class) class KnockRequestsBannerPresenterTest {
    @Test
    fun `present - when feature is disabled then the banner should be hidden`() = runTest {
        val knockRequests = flowOf(listOf(FakeKnockRequest()))
        val presenter = createKnockRequestsBannerPresenter(isFeatureEnabled = false, knockRequestsFlow = knockRequests)
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.isVisible).isFalse()
            }
        }
    }

    @Test
    fun `present - when empty knock request list then the banner should be hidden`() = runTest {
        val knockRequests = flowOf(emptyList<KnockRequest>())
        val presenter = createKnockRequestsBannerPresenter(knockRequestsFlow = knockRequests)
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.isVisible).isFalse()
            }
        }
    }

    @Test
    fun `present - when no permission to manage knock requests then the banner should be hidden`() = runTest {
        val presenter = createKnockRequestsBannerPresenter(canAcceptKnockRequests = false)
        presenter.test {
            awaitItem().also { state ->
                assertThat(state.isVisible).isFalse()
            }
        }
    }

    @Test
    fun `present - when everything is setup to manage knocks with data, then the banner should be visible`() = runTest {
        val knockRequests = flowOf(
            listOf(
                FakeKnockRequest(
                    reason = "A reason",
                )
            )
        )
        val presenter = createKnockRequestsBannerPresenter(knockRequestsFlow = knockRequests)
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.isVisible).isTrue()
                assertThat(state.knockRequests).hasSize(1)
                assertThat(state.canAccept).isTrue()
                assertThat(state.reason).isEqualTo("A reason")
            }
        }
    }

    @Test
    fun `present - when multiple knock requests, the banner should not have reason nor subtitle`() = runTest {
        val knockRequests = flowOf(
            listOf(
                FakeKnockRequest(
                    displayName = "Alice",
                ),
                FakeKnockRequest(
                    displayName = "Bob",
                ),
                FakeKnockRequest(
                    displayName = "Charlie",
                ),
            )
        )
        val presenter = createKnockRequestsBannerPresenter(knockRequestsFlow = knockRequests)
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.isVisible).isTrue()
                assertThat(state.knockRequests).hasSize(3)
                assertThat(state.reason).isNull()
                assertThat(state.subtitle).isNull()
            }
        }
    }

    @Test
    fun `present - when there are some seen knock requests, then the banner should filtered them`() = runTest {
        val knockRequests = flowOf(
            listOf(
                FakeKnockRequest(
                    displayName = "Alice",
                    isSeen = true,
                    userId = A_USER_ID
                ),
                FakeKnockRequest(
                    displayName = "Bob",
                    isSeen = true,
                    userId = A_USER_ID_2
                ),
                FakeKnockRequest(
                    isSeen = false,
                    displayName = "Charlie",
                    reason = "A reason",
                    userId = A_USER_ID_3
                ),
            )
        )
        val presenter = createKnockRequestsBannerPresenter(knockRequestsFlow = knockRequests)
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.isVisible).isTrue()
                // Only Charlie should be displayed
                assertThat(state.knockRequests).hasSize(1)
                assertThat(state.reason).isEqualTo("A reason")
                assertThat(state.subtitle).isEqualTo(A_USER_ID_3.value)
            }
        }
    }

    @Test
    fun `present - given AcceptSingleRequest event with failure, then the banner should hide and reappear and error should appear and disappear`() = runTest {
        val acceptLambda = lambdaRecorder<Result<Unit>> { Result.failure(Exception()) }
        val knockRequest = FakeKnockRequest(
            displayName = "Alice",
            reason = "A reason",
            acceptLambda = acceptLambda
        )
        val knockRequests = flowOf(listOf(knockRequest))
        val presenter = createKnockRequestsBannerPresenter(knockRequestsFlow = knockRequests)
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                state.eventSink(KnockRequestsBannerEvents.AcceptSingleRequest)
            }
            awaitItem().also { state ->
                assertThat(state.isVisible).isFalse()
                assertThat(state.displayAcceptError).isFalse()
            }
            awaitItem().also { state ->
                assertThat(state.isVisible).isFalse()
                assertThat(state.displayAcceptError).isTrue()
            }
            awaitItem().also { state ->
                assertThat(state.isVisible).isTrue()
                assertThat(state.displayAcceptError).isTrue()
            }
            awaitItem().also { state ->
                assertThat(state.isVisible).isTrue()
                assertThat(state.displayAcceptError).isFalse()
            }
            assert(acceptLambda).isCalledOnce()
        }
    }

    @Test
    fun `present - given an AcceptSingleRequest event with success, then banner should be dismissed`() = runTest {
        val acceptLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val knockRequest = FakeKnockRequest(
            displayName = "Alice",
            reason = "A reason",
            acceptLambda = acceptLambda
        )
        val knockRequests = flowOf(listOf(knockRequest))
        val presenter = createKnockRequestsBannerPresenter(knockRequestsFlow = knockRequests)
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.knockRequests).hasSize(1)
                state.eventSink(KnockRequestsBannerEvents.AcceptSingleRequest)
            }
            awaitItem().also { state ->
                assertThat(state.isVisible).isFalse()
            }
            advanceUntilIdle()
            assert(acceptLambda).isCalledOnce()
        }
    }

    @Test
    fun `present - given a Dismiss event, then knock requests should be marked as seen`() = runTest {
        val markAsSeenLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val knockRequests = flowOf(
            listOf(
                FakeKnockRequest(markAsSeenLambda = markAsSeenLambda),
                FakeKnockRequest(markAsSeenLambda = markAsSeenLambda),
                FakeKnockRequest(markAsSeenLambda = markAsSeenLambda),
            )
        )
        val presenter = createKnockRequestsBannerPresenter(knockRequestsFlow = knockRequests)
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                state.eventSink(KnockRequestsBannerEvents.Dismiss)
            }
            advanceUntilIdle()
            assert(markAsSeenLambda).isCalledExactly(3)
        }
    }
}

private fun TestScope.createKnockRequestsBannerPresenter(
    knockRequestsFlow: Flow<List<KnockRequest>> = flowOf(emptyList()),
    canAcceptKnockRequests: Boolean = true,
    isFeatureEnabled: Boolean = true,
): KnockRequestsBannerPresenter {
    val knockRequestsService = KnockRequestsService(
        knockRequestsFlow = knockRequestsFlow,
        coroutineScope = backgroundScope,
        isKnockFeatureEnabledFlow = flowOf(isFeatureEnabled),
        permissionsFlow = flowOf(KnockRequestPermissions(canAcceptKnockRequests, canAcceptKnockRequests, canAcceptKnockRequests)),
    )
    return KnockRequestsBannerPresenter(
        knockRequestsService = knockRequestsService,
        sessionCoroutineScope = this,
    )
}
