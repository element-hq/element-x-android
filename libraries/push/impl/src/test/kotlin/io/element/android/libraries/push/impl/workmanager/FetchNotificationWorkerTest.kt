/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.workmanager

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.taskexecutor.WorkManagerTaskExecutor
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.google.common.util.concurrent.ListenableFuture
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.features.networkmonitor.test.FakeNetworkMonitor
import io.element.android.libraries.androidutils.json.DefaultJsonProvider
import io.element.android.libraries.push.api.push.SyncOnNotifiableEvent
import io.element.android.libraries.push.impl.notifications.FakeNotifiableEventResolver
import io.element.android.libraries.push.impl.notifications.NotificationResolverQueue
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import io.element.android.libraries.push.test.notifications.FakeNotificationResolverQueue
import io.element.android.libraries.workmanager.api.WorkManagerRequest
import io.element.android.libraries.workmanager.api.di.MetroWorkerFactory
import io.element.android.libraries.workmanager.test.FakeWorkManagerScheduler
import io.element.android.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class FetchNotificationWorkerTest {
    @Test
    fun `test - success`() = runTest {
        var synced = false
        val syncOnNotifiableEventLambda = SyncOnNotifiableEvent { synced = true }

        val queue = FakeNotificationResolverQueue(
            processingLambda = { Result.success(ResolvedPushEvent.Event(aNotifiableMessageEvent())) }
        )
        val worker = createWorker(
            input = """
            [
                {
                    "session_id": "@alice:matrix.org",
                    "room_id": "!roomid:matrix.org",
                    "event_id": "$1436ebk:matrix.org",
                    "provider_info": "some_info"
                }
            ]
            """.trimIndent(),
            queue = queue,
            syncOnNotifiableEvent = syncOnNotifiableEventLambda,
        )

        val result = worker.doWork()

        // The process finished successfully
        assertThat(result).isEqualTo(ListenableWorker.Result.success())

        // A result was emitted
        assertThat(queue.results.replayCache).isNotEmpty()

        // An opportunistic sync was triggered
        assertThat(synced).isTrue()
    }

    @Test
    fun `test - invalid input fails the work`() = runTest {
        val worker = createWorker(
            input = """
            [
                {
                    "session_id": "!alice:matrix.org",
                    "room_id": "!roomid:matrix.org",
                    "event_id": "$1436ebk:matrix.org",
                    "provider_info": "some_info"
                }
            ]
            """.trimIndent(),
        )

        val result = worker.doWork()

        // The process failed
        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun `test - no network connectivity fails the work`() = runTest {
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Disconnected)
        val worker = createWorker(
            input = """
            [
                {
                    "session_id": "@alice:matrix.org",
                    "room_id": "!roomid:matrix.org",
                    "event_id": "$1436ebk:matrix.org",
                    "provider_info": "some_info"
                }
            ]
            """.trimIndent(),
            networkMonitor = networkMonitor,
        )

        val result = worker.doWork()

        advanceTimeBy(10.seconds)

        // The process failed due to a timeout in getting the network connectivity, a retry is scheduled
        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }

    @Test
    fun `test - failing to resolve events re-schedules the work`() = runTest {
        val submitWorkerLambda = lambdaRecorder<WorkManagerRequest, Unit> {}
        val scheduler = FakeWorkManagerScheduler(submitLambda = submitWorkerLambda)

        val resolver = FakeNotifiableEventResolver(
            resolveEventsResult = { _, _ -> Result.failure(Exception("Failed to resolve events")) }
        )

        val worker = createWorker(
            input = """
            [
                {
                    "session_id": "@alice:matrix.org",
                    "room_id": "!roomid:matrix.org",
                    "event_id": "$1436ebk:matrix.org",
                    "provider_info": "some_info"
                }
            ]
            """.trimIndent(),
            eventResolver = resolver,
            workManagerScheduler = scheduler,
        )

        val result = worker.doWork()

        // The process was considered successful, but a retry was scheduled due to the failure to resolve events
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        submitWorkerLambda.assertions().isCalledOnce()
    }

    private fun TestScope.createWorker(
        input: String,
        networkMonitor: FakeNetworkMonitor = FakeNetworkMonitor(),
        eventResolver: FakeNotifiableEventResolver = FakeNotifiableEventResolver(resolveEventsResult = { _, _ -> Result.success(emptyMap()) }),
        queue: NotificationResolverQueue = FakeNotificationResolverQueue(
            processingLambda = { Result.success(ResolvedPushEvent.Event(aNotifiableMessageEvent())) }
        ),
        workManagerScheduler: FakeWorkManagerScheduler = FakeWorkManagerScheduler(),
        syncOnNotifiableEvent: SyncOnNotifiableEvent = SyncOnNotifiableEvent {},
    ) = FetchNotificationsWorker(
        workerParams = createWorkerParams(workDataOf("requests" to input)),
        context = InstrumentationRegistry.getInstrumentation().context,
        networkMonitor = networkMonitor,
        eventResolver = eventResolver,
        queue = queue,
        workManagerScheduler = workManagerScheduler,
        syncOnNotifiableEvent = syncOnNotifiableEvent,
        coroutineDispatchers = testCoroutineDispatchers(),
        workerDataConverter = WorkerDataConverter(DefaultJsonProvider()),
        buildVersionSdkIntProvider = FakeBuildVersionSdkIntProvider(33),
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
        { context, id, data -> FakeListenableFuture() },
        { context, id, foregroundInfo -> FakeListenableFuture() },
    )
}

class FakeListenableFuture<T> : ListenableFuture<T> {
    override fun addListener(listener: Runnable, executor: Executor) = Unit
    override fun cancel(mayInterruptIfRunning: Boolean): Boolean = true
    override fun get(): T? = null
    override fun get(timeout: Long, unit: TimeUnit?): T? = null
    override fun isCancelled(): Boolean = false
    override fun isDone(): Boolean = false
}
