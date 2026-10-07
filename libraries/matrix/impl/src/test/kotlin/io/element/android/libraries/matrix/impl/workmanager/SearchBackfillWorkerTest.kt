/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.workmanager

import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.taskexecutor.WorkManagerTaskExecutor
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.google.common.util.concurrent.ListenableFuture
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.SdkPendingTask
import io.element.android.libraries.matrix.api.search.SearchBackfillStrategy
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.FakeSdkPendingTask
import io.element.android.libraries.matrix.test.search.FakeSearchBackfillService
import io.element.android.libraries.workmanager.api.di.MetroWorkerFactory
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.UUID
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class SearchBackfillWorkerTest : RobolectricTest() {
    @Test
    fun `missing session id - fails`() = runTest {
        val worker = createWorker(input = null, client = FakeMatrixClient())

        assertThat(worker.doWork()).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun `client cannot be restored - fails`() = runTest {
        val worker = createWorker(client = null)

        assertThat(worker.doWork()).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun `backfill already running - does not start another one`() = runTest {
        val startLambda = lambdaRecorder<SearchBackfillStrategy, Result<SdkPendingTask>> { error("Should not be called") }
        val client = FakeMatrixClient(
            searchBackfillService = FakeSearchBackfillService(
                isSearchBackfillRunningLambda = { true },
                startSearchBackfillLambda = startLambda,
            ),
        )

        val result = createWorker(client = client).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        startLambda.assertions().isNeverCalled()
    }

    @Test
    fun `start fails - fails`() = runTest {
        val client = FakeMatrixClient(
            searchBackfillService = FakeSearchBackfillService(
                startSearchBackfillLambda = { Result.failure(IllegalStateException("boom")) },
            ),
        )

        assertThat(createWorker(client = client).doWork()).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun `backfill finishes - succeeds and closes the task`() = runTest {
        var running = true
        val closeLambda = lambdaRecorder<Unit> { }
        val startLambda = lambdaRecorder<SearchBackfillStrategy, Result<SdkPendingTask>> {
            Result.success(FakeSdkPendingTask(isRunningLambda = { running }, closeLambda = closeLambda))
        }
        val client = FakeMatrixClient(searchBackfillService = FakeSearchBackfillService(startSearchBackfillLambda = startLambda))

        val deferred = async { createWorker(client = client).doWork() }
        advanceTimeBy(10.seconds)
        running = false
        advanceTimeBy(10.seconds)

        assertThat(deferred.await()).isEqualTo(ListenableWorker.Result.success())
        startLambda.assertions().isCalledOnce().with(value(SearchBackfillStrategy.BACKGROUND))
        closeLambda.assertions().isCalledOnce()
    }

    @Test
    fun `backfill times out after 10 minutes - closes the task and retries`() = runTest {
        val closeLambda = lambdaRecorder<Unit> { }
        val client = FakeMatrixClient(
            searchBackfillService = FakeSearchBackfillService(
                startSearchBackfillLambda = { Result.success(FakeSdkPendingTask(isRunningLambda = { true }, closeLambda = closeLambda)) },
            ),
        )

        val deferred = async { createWorker(client = client).doWork() }
        advanceTimeBy(9.minutes)
        assertThat(deferred.isCompleted).isFalse()
        advanceTimeBy(2.minutes)

        assertThat(deferred.await()).isEqualTo(ListenableWorker.Result.retry())
        closeLambda.assertions().isCalledOnce()
    }

    private fun TestScope.createWorker(
        input: String? = A_SESSION_ID.value,
        client: MatrixClient?,
    ) = SearchBackfillWorker(
        params = createWorkerParams(
            inputData = input?.let { workDataOf(SearchBackfillWorker.SESSION_ID_PARAM to it) } ?: Data.EMPTY,
        ),
        context = InstrumentationRegistry.getInstrumentation().context,
        matrixClientProvider = FakeMatrixClientProvider(
            getClient = { client?.let { Result.success(it) } ?: Result.failure(IllegalStateException("No client")) }
        ),
    )

    private fun TestScope.createWorkerParams(
        inputData: Data = Data.EMPTY,
    ): WorkerParameters = WorkerParameters(
        UUID.randomUUID(),
        inputData,
        emptySet(),
        WorkerParameters.RuntimeExtras(),
        0,
        0,
        Executors.newSingleThreadExecutor(),
        backgroundScope.coroutineContext,
        WorkManagerTaskExecutor(Executors.newSingleThreadExecutor()),
        MetroWorkerFactory(emptyMap()),
        { _, _, _ -> FakeListenableFuture() },
        { _, _, _ -> FakeListenableFuture() },
    )
}

private class FakeListenableFuture<T> : ListenableFuture<T> {
    override fun addListener(listener: Runnable, executor: Executor) = Unit
    override fun cancel(mayInterruptIfRunning: Boolean): Boolean = true
    override fun get(): T? = null
    override fun get(timeout: Long, unit: TimeUnit?): T? = null
    override fun isCancelled(): Boolean = false
    override fun isDone(): Boolean = false
}
