/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav

import io.element.android.appnav.di.SyncOrchestrator
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.features.networkmonitor.test.FakeNetworkMonitor
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.matrix.test.sync.FakeSyncService
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.services.appnavstate.test.FakeAppForegroundStateService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class SyncOrchestratorTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `when the sync wasn't running before, an initial sync will take place, even with no network`() = runTest {
        val startSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(initialSyncState = SyncState.Idle).apply {
            startSyncLambda = startSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Disconnected)
        val syncOrchestrator = createSyncOrchestrator(
            syncService = syncService,
            networkMonitor = networkMonitor,
        )

        // We start observing with an initial sync
        syncOrchestrator.start()

        // Advance the time just enough to make sure the initial sync has run
        advanceTimeBy(1.milliseconds)
        startSyncRecorder.assertions().isCalledOnce()
    }

    @Test
    fun `when the sync wasn't running before, an initial sync will take place`() = runTest {
        val startSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(initialSyncState = SyncState.Idle).apply {
            startSyncLambda = startSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Connected)
        val syncOrchestrator = createSyncOrchestrator(
            syncService = syncService,
            networkMonitor = networkMonitor,
        )

        // We start observing with an initial sync
        syncOrchestrator.start()

        // Advance the time just enough to make sure the initial sync has run
        advanceTimeBy(1.milliseconds)
        startSyncRecorder.assertions().isCalledOnce()

        // If we wait for a while, the sync will not be started again by the observer since it's already running
        advanceTimeBy(10.seconds)
        startSyncRecorder.assertions().isCalledOnce()
    }

    @Test
    fun `when the app goes to background and the sync was running, it will be stopped after a delay`() = runTest {
        val stopSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(initialSyncState = SyncState.Running).apply {
            stopSyncLambda = stopSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Connected)
        val appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = true)
        val syncOrchestrator = createSyncOrchestrator(
            syncService = syncService,
            networkMonitor = networkMonitor,
            appForegroundStateService = appForegroundStateService,
        )

        // We start observing
        syncOrchestrator.observeStates()

        // Advance the time to make sure the orchestrator has had time to start processing the inputs
        advanceTimeBy(100.milliseconds)

        // Stop sync was never called
        stopSyncRecorder.assertions().isNeverCalled()

        // Now we send the app to background
        appForegroundStateService.isInForeground.value = false

        // Stop sync will be called after some delay
        stopSyncRecorder.assertions().isNeverCalled()
        advanceTimeBy(10.seconds)
        stopSyncRecorder.assertions().isCalledOnce()
    }

    @Test
    fun `when the app state changes several times in a short while, stop sync is only called once`() = runTest {
        val stopSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(initialSyncState = SyncState.Running).apply {
            stopSyncLambda = stopSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Connected)
        val appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = true)
        val syncOrchestrator = createSyncOrchestrator(
            syncService = syncService,
            networkMonitor = networkMonitor,
            appForegroundStateService = appForegroundStateService,
        )

        // We start observing
        syncOrchestrator.observeStates()

        // Advance the time to make sure the orchestrator has had time to start processing the inputs
        advanceTimeBy(100.milliseconds)

        // Stop sync was never called
        stopSyncRecorder.assertions().isNeverCalled()

        // Now we send the app to background
        appForegroundStateService.isInForeground.value = false

        // Ensure the stop action wasn't called yet
        stopSyncRecorder.assertions().isNeverCalled()
        advanceTimeBy(1.seconds)
        appForegroundStateService.isInForeground.value = true
        advanceTimeBy(1.seconds)

        // Ensure the stop action wasn't called yet either, since we didn't give it enough time to emit after the expected delay
        stopSyncRecorder.assertions().isNeverCalled()

        // Now change it again and wait for enough time
        appForegroundStateService.isInForeground.value = false
        advanceTimeBy(4.seconds)

        // And confirm it's now called
        stopSyncRecorder.assertions().isCalledOnce()
    }

    @Test
    fun `when the app was in background and we receive a notification, a sync will be started then stopped`() = runTest {
        val startSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val stopSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(initialSyncState = SyncState.Idle).apply {
            startSyncLambda = startSyncRecorder
            stopSyncLambda = stopSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Connected)
        val appForegroundStateService = FakeAppForegroundStateService(
            initialForegroundValue = false,
            initialIsSyncingNotificationEventValue = false,
        )
        val syncOrchestrator = createSyncOrchestrator(
            syncService = syncService,
            networkMonitor = networkMonitor,
            appForegroundStateService = appForegroundStateService,
        )

        // We start observing
        syncOrchestrator.observeStates()

        // Advance the time to make sure the orchestrator has had time to start processing the inputs
        advanceTimeBy(100.milliseconds)

        // Start sync was never called
        startSyncRecorder.assertions().isNeverCalled()

        // Now we receive a notification and need to sync
        appForegroundStateService.updateIsSyncingNotificationEvent(true)

        // Start sync will be called shortly after
        advanceTimeBy(1.milliseconds)
        startSyncRecorder.assertions().isCalledOnce()

        // If the sync is running and we mark the notification sync as no longer necessary, the sync stops after a delay
        syncService.emitSyncState(SyncState.Running)
        appForegroundStateService.updateIsSyncingNotificationEvent(false)

        advanceTimeBy(10.seconds)
        stopSyncRecorder.assertions().isCalledOnce()
    }

    @Test
    fun `when the app was in background and we join a call, a sync will be started`() = runTest {
        val startSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val stopSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(initialSyncState = SyncState.Idle).apply {
            startSyncLambda = startSyncRecorder
            stopSyncLambda = stopSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Connected)
        val appForegroundStateService = FakeAppForegroundStateService(
            initialForegroundValue = false,
            initialIsSyncingNotificationEventValue = false,
        )
        val syncOrchestrator = createSyncOrchestrator(
            syncService = syncService,
            networkMonitor = networkMonitor,
            appForegroundStateService = appForegroundStateService,
        )

        // We start observing
        syncOrchestrator.observeStates()

        // Advance the time to make sure the orchestrator has had time to start processing the inputs
        advanceTimeBy(100.milliseconds)

        // Start sync was never called
        startSyncRecorder.assertions().isNeverCalled()

        // Now we join a call
        appForegroundStateService.updateIsInCallState(true)

        // Start sync will be called shortly after
        advanceTimeBy(1.milliseconds)
        startSyncRecorder.assertions().isCalledOnce()

        // If the sync is running and we mark the in-call state as false, the sync stops after a delay
        syncService.emitSyncState(SyncState.Running)
        appForegroundStateService.updateIsInCallState(false)

        advanceTimeBy(10.seconds)
        stopSyncRecorder.assertions().isCalledOnce()
    }

    @Test
    fun `when the app was in background and we have an incoming ringing call, a sync will be started`() = runTest {
        val startSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val stopSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(initialSyncState = SyncState.Idle).apply {
            startSyncLambda = startSyncRecorder
            stopSyncLambda = stopSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Connected)
        val appForegroundStateService = FakeAppForegroundStateService(
            initialForegroundValue = false,
            initialIsSyncingNotificationEventValue = false,
            initialHasRingingCall = false,
        )
        val syncOrchestrator = createSyncOrchestrator(
            syncService = syncService,
            networkMonitor = networkMonitor,
            appForegroundStateService = appForegroundStateService,
        )

        // We start observing
        syncOrchestrator.observeStates()

        // Advance the time to make sure the orchestrator has had time to start processing the inputs
        advanceTimeBy(100.milliseconds)

        // Start sync was never called
        startSyncRecorder.assertions().isNeverCalled()

        // Now we receive a ringing call
        appForegroundStateService.updateHasRingingCall(true)

        // Start sync will be called shortly after
        advanceTimeBy(1.milliseconds)
        startSyncRecorder.assertions().isCalledOnce()

        // If the sync is running and the ringing call notification is now over, the sync stops after a delay
        syncService.emitSyncState(SyncState.Running)
        appForegroundStateService.updateHasRingingCall(false)

        advanceTimeBy(10.seconds)
        stopSyncRecorder.assertions().isCalledOnce()
    }

    @Test
    fun `when the app is in foreground, we sync for a notification and a call is ongoing, the sync will only stop when all conditions are false`() = runTest {
        val startSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val stopSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(initialSyncState = SyncState.Running).apply {
            startSyncLambda = startSyncRecorder
            stopSyncLambda = stopSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Connected)
        val appForegroundStateService = FakeAppForegroundStateService(
            initialForegroundValue = true,
            initialIsSyncingNotificationEventValue = true,
            initialIsInCallValue = true,
        )
        val syncOrchestrator = createSyncOrchestrator(
            syncService = syncService,
            networkMonitor = networkMonitor,
            appForegroundStateService = appForegroundStateService,
        )

        // We start observing
        syncOrchestrator.observeStates()

        // Advance the time to make sure the orchestrator has had time to start processing the inputs
        advanceTimeBy(100.milliseconds)

        // Start sync was never called
        startSyncRecorder.assertions().isNeverCalled()

        // We send the app to background, it's still syncing
        appForegroundStateService.givenIsInForeground(false)
        advanceTimeBy(10.seconds)
        stopSyncRecorder.assertions().isNeverCalled()

        // We stop the notification sync, it's still syncing
        appForegroundStateService.updateIsSyncingNotificationEvent(false)
        advanceTimeBy(10.seconds)
        stopSyncRecorder.assertions().isNeverCalled()

        // We set the in-call state to false, now it stops syncing after a delay
        appForegroundStateService.updateIsInCallState(false)
        advanceTimeBy(10.seconds)
        stopSyncRecorder.assertions().isCalledOnce()
    }

    @Test
    fun `if the sync was running, it's set to be stopped but something triggers a sync again, the sync is not stopped`() = runTest {
        val stopSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(initialSyncState = SyncState.Running).apply {
            stopSyncLambda = stopSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Connected)
        val appForegroundStateService = FakeAppForegroundStateService(
            initialForegroundValue = true,
            initialIsSyncingNotificationEventValue = false,
            initialIsInCallValue = false,
        )
        val syncOrchestrator = createSyncOrchestrator(
            syncService = syncService,
            networkMonitor = networkMonitor,
            appForegroundStateService = appForegroundStateService,
        )

        // We start observing
        syncOrchestrator.observeStates()

        // Advance the time to make sure the orchestrator has had time to start processing the inputs
        advanceTimeBy(100.milliseconds)

        // This will set the sync to stop
        appForegroundStateService.givenIsInForeground(false)

        // But if we reset it quickly before the stop sync takes place, the sync is not stopped
        advanceTimeBy(2.seconds)
        appForegroundStateService.givenIsInForeground(true)

        advanceTimeBy(10.seconds)
        stopSyncRecorder.assertions().isNeverCalled()
    }

    @Test
    fun `when network is offline, sync service should not start`() = runTest {
        val startSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(initialSyncState = SyncState.Idle).apply {
            startSyncLambda = startSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Disconnected)
        val syncOrchestrator = createSyncOrchestrator(
            syncService = syncService,
            networkMonitor = networkMonitor,
        )

        // We start observing
        syncOrchestrator.observeStates()

        // This should still not trigger a sync, since there is no network
        advanceTimeBy(10.seconds)
        startSyncRecorder.assertions().isNeverCalled()
    }

    private fun TestScope.createSyncOrchestrator(
        syncService: FakeSyncService = FakeSyncService(),
        networkMonitor: FakeNetworkMonitor = FakeNetworkMonitor(),
        appForegroundStateService: FakeAppForegroundStateService = FakeAppForegroundStateService(),
    ) = SyncOrchestrator(
        syncService = syncService,
        sessionCoroutineScope = backgroundScope,
        networkMonitor = networkMonitor,
        appForegroundStateService = appForegroundStateService,
        dispatchers = testCoroutineDispatchers(),
        analyticsService = FakeAnalyticsService(),
    )
}
