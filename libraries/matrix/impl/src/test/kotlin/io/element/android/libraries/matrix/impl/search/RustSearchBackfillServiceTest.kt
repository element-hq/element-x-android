/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.search.SearchBackfillStrategy
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiClient
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiTaskHandle
import io.element.android.libraries.matrix.impl.search.workmanager.SearchBackfillRequestBuilder
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.workmanager.api.WorkManagerRequestBuilder
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.WorkManagerRequestWrapper
import io.element.android.libraries.workmanager.test.FakeWorkManagerScheduler
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.TaskHandle
import uniffi.matrix_sdk.SearchBackfillStrategy as RustSearchBackfillStrategy

class RustSearchBackfillServiceTest {
    @Test
    fun `schedulePeriodicSearchBackfill submits a request when nothing is pending`() = runTest {
        val builder = aRequestBuilder()
        val createBuilderLambda = lambdaRecorder<SessionId, SearchBackfillRequestBuilder> { builder }
        val submitLambda = lambdaRecorder<WorkManagerRequestBuilder, Unit> { }
        val service = createService(
            requestBuilderFactory = SearchBackfillRequestBuilder.Factory(createBuilderLambda),
            workManagerScheduler = FakeWorkManagerScheduler(
                submitLambda = submitLambda,
                hasPendingWorkLambda = { _, _ -> false },
            ),
        )

        val result = service.schedulePeriodicSearchBackfill(SearchBackfillStrategy.BACKGROUND)

        assertThat(result.isSuccess).isTrue()
        createBuilderLambda.assertions().isCalledOnce().with(value(A_SESSION_ID))
        submitLambda.assertions().isCalledOnce().with(value(builder))
    }

    @Test
    fun `schedulePeriodicSearchBackfill does nothing when work is already pending`() = runTest {
        val hasPendingWorkLambda = lambdaRecorder<SessionId, WorkManagerRequestType, Boolean> { _, _ -> true }
        val createBuilderLambda = lambdaRecorder<SessionId, SearchBackfillRequestBuilder> { error("Should not be called") }
        val submitLambda = lambdaRecorder<WorkManagerRequestBuilder, Unit> { error("Should not be called") }
        val service = createService(
            requestBuilderFactory = SearchBackfillRequestBuilder.Factory(createBuilderLambda),
            workManagerScheduler = FakeWorkManagerScheduler(
                submitLambda = submitLambda,
                hasPendingWorkLambda = hasPendingWorkLambda,
            ),
        )

        val result = service.schedulePeriodicSearchBackfill(SearchBackfillStrategy.BACKGROUND)

        assertThat(result.isSuccess).isTrue()
        hasPendingWorkLambda.assertions().isCalledOnce().with(value(A_SESSION_ID), value(WorkManagerRequestType.SEARCH_BACKFILL))
        createBuilderLambda.assertions().isNeverCalled()
        submitLambda.assertions().isNeverCalled()
    }

    @Test
    fun `schedulePeriodicSearchBackfill returns a failure when submitting fails`() = runTest {
        val service = createService(
            requestBuilderFactory = SearchBackfillRequestBuilder.Factory { aRequestBuilder() },
            workManagerScheduler = FakeWorkManagerScheduler(submitLambda = { throw IllegalStateException("boom") }),
        )

        val result = service.schedulePeriodicSearchBackfill(SearchBackfillStrategy.BACKGROUND)

        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `cancelPeriodicSearchBackfill cancels the search backfill work of the session`() {
        val cancelLambda = lambdaRecorder<SessionId, WorkManagerRequestType?, Unit> { _, _ -> }
        val service = createService(workManagerScheduler = FakeWorkManagerScheduler(cancelLambda = cancelLambda))

        service.cancelPeriodicSearchBackfill()

        cancelLambda.assertions().isCalledOnce().with(value(A_SESSION_ID), value(WorkManagerRequestType.SEARCH_BACKFILL))
    }

    @Test
    fun `startSearchBackfill maps the strategy and returns a running task`() {
        val strategyLambda = lambdaRecorder<RustSearchBackfillStrategy, TaskHandle> { FakeFfiTaskHandle(isFinishedResult = { false }) }
        val service = createService(client = FakeFfiClient(runSearchBackfillResult = strategyLambda))

        val task = service.startSearchBackfill(SearchBackfillStrategy.FOREGROUND).getOrThrow()

        strategyLambda.assertions().isCalledOnce().with(value(RustSearchBackfillStrategy.FOREGROUND))
        assertThat(task.isRunning()).isTrue()
        assertThat(service.isSearchBackfillRunning()).isTrue()
    }

    @Test
    fun `startSearchBackfill maps the background strategy`() {
        val strategyLambda = lambdaRecorder<RustSearchBackfillStrategy, TaskHandle> { FakeFfiTaskHandle() }
        val service = createService(client = FakeFfiClient(runSearchBackfillResult = strategyLambda))

        service.startSearchBackfill(SearchBackfillStrategy.BACKGROUND)

        strategyLambda.assertions().isCalledOnce().with(value(RustSearchBackfillStrategy.BACKGROUND))
    }

    @Test
    fun `startSearchBackfill fails when a backfill is already running`() {
        val strategyLambda = lambdaRecorder<RustSearchBackfillStrategy, TaskHandle> { FakeFfiTaskHandle(isFinishedResult = { false }) }
        val service = createService(client = FakeFfiClient(runSearchBackfillResult = strategyLambda))
        service.startSearchBackfill(SearchBackfillStrategy.FOREGROUND)

        val result = service.startSearchBackfill(SearchBackfillStrategy.FOREGROUND)

        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        strategyLambda.assertions().isCalledOnce()
    }

    @Test
    fun `startSearchBackfill can start again once the previous task has finished`() {
        val strategyLambda = lambdaRecorder<RustSearchBackfillStrategy, TaskHandle> { FakeFfiTaskHandle(isFinishedResult = { true }) }
        val service = createService(client = FakeFfiClient(runSearchBackfillResult = strategyLambda))

        assertThat(service.startSearchBackfill(SearchBackfillStrategy.FOREGROUND).isSuccess).isTrue()
        assertThat(service.isSearchBackfillRunning()).isFalse()
        assertThat(service.startSearchBackfill(SearchBackfillStrategy.FOREGROUND).isSuccess).isTrue()

        strategyLambda.assertions().isCalledExactly(2)
    }

    @Test
    fun `startSearchBackfill returns a failure when the SDK fails`() {
        val service = createService(client = FakeFfiClient(runSearchBackfillResult = { error("boom") }))

        val result = service.startSearchBackfill(SearchBackfillStrategy.FOREGROUND)

        assertThat(result.isFailure).isTrue()
        assertThat(service.isSearchBackfillRunning()).isFalse()
    }

    @Test
    fun `isSearchBackfillRunning is false when nothing was started`() {
        assertThat(createService().isSearchBackfillRunning()).isFalse()
    }

    @Test
    fun `cancelSearchBackfill cancels the running task`() {
        val cancelLambda = lambdaRecorder<Unit> { }
        val service = createService(
            client = FakeFfiClient(runSearchBackfillResult = { FakeFfiTaskHandle(cancelResult = cancelLambda) }),
        )
        service.startSearchBackfill(SearchBackfillStrategy.FOREGROUND)

        val result = service.cancelSearchBackfill()

        assertThat(result.isSuccess).isTrue()
        cancelLambda.assertions().isCalledOnce()
        assertThat(service.isSearchBackfillRunning()).isFalse()
    }

    @Test
    fun `cancelSearchBackfill without a running task is a no-op`() {
        assertThat(createService().cancelSearchBackfill().isSuccess).isTrue()
    }

    @Test
    fun `startSearchBackfill works again after cancelSearchBackfill`() {
        val strategyLambda = lambdaRecorder<RustSearchBackfillStrategy, TaskHandle> { FakeFfiTaskHandle() }
        val service = createService(client = FakeFfiClient(runSearchBackfillResult = strategyLambda))
        service.startSearchBackfill(SearchBackfillStrategy.FOREGROUND)
        service.cancelSearchBackfill()

        assertThat(service.startSearchBackfill(SearchBackfillStrategy.FOREGROUND).isSuccess).isTrue()

        strategyLambda.assertions().isCalledExactly(2)
    }

    private fun aRequestBuilder() = object : SearchBackfillRequestBuilder {
        override suspend fun build(): Result<List<WorkManagerRequestWrapper>> = Result.success(emptyList())
    }

    private fun createService(
        client: FakeFfiClient = FakeFfiClient(),
        requestBuilderFactory: SearchBackfillRequestBuilder.Factory = SearchBackfillRequestBuilder.Factory { aRequestBuilder() },
        workManagerScheduler: FakeWorkManagerScheduler = FakeWorkManagerScheduler(),
    ) = RustSearchBackfillService(
        innerClient = client,
        searchBackfillRequestBuilderFactory = requestBuilderFactory,
        workManagerScheduler = workManagerScheduler,
    )
}
