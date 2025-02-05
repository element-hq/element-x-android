/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav

import io.element.android.appnav.di.SyncOrchestrator
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.features.networkmonitor.test.FakeNetworkMonitor
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.sync.FakeSyncService
import io.element.android.services.appnavstate.test.FakeAppForegroundStateService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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
    fun `when the app goes to background and the sync was running, it will be stopped after a delay`() = runTest {
        val stopSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(initialSyncState = SyncState.Running).apply {
            stopSyncLambda = stopSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Connected)
        val appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = true)
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())
        val syncOrchestrator = createSyncOrchestrator(
            syncService = syncService,
            networkMonitor = networkMonitor,
            appForegroundStateService = appForegroundStateService,
            baseCoroutineScope = coroutineScope
        )

        // We start observing, we skip the initial sync attempt since the state is running
        syncOrchestrator.start()

        // Advance the time to make sure we left the initial sync behind
        advanceTimeBy(1.seconds)

        // Stop sync was never called
        stopSyncRecorder.assertions().isNeverCalled()

        // Now we send the app to background
        appForegroundStateService.isInForeground.value = false

        // Stop sync will be called after some delay
        stopSyncRecorder.assertions().isNeverCalled()
        advanceTimeBy(10.seconds)
        stopSyncRecorder.assertions().isCalledOnce()

        // Stop observing
        coroutineScope.cancel()
    }

    @Test
    fun `when the app was in background and we receive a notification, a sync will be started then stopped`() = runTest {
        val startSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val stopSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(initialSyncState = SyncState.Running).apply {
            startSyncLambda = startSyncRecorder
            stopSyncLambda = stopSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Connected)
        val appForegroundStateService = FakeAppForegroundStateService(
            initialForegroundValue = false,
            initialIsSyncingNotificationEventValue = false,
        )
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())
        val syncOrchestrator = createSyncOrchestrator(
            syncService = syncService,
            networkMonitor = networkMonitor,
            appForegroundStateService = appForegroundStateService,
            baseCoroutineScope = coroutineScope
        )

        // We start observing, we skip the initial sync attempt since the state is running
        syncOrchestrator.start()

        // Advance the time to make sure we left the initial sync behind
        advanceTimeBy(1.seconds)

        // Start sync was never called
        startSyncRecorder.assertions().isNeverCalled()

        // We stop the ongoing sync, give the sync service some time to stop
        syncService.emitSyncState(SyncState.Idle)
        advanceTimeBy(10.seconds)
        stopSyncRecorder.assertions().isCalledOnce()

        // Now we receive a notification and need to sync
        appForegroundStateService.updateIsSyncingNotificationEvent(true)

        // Start sync will be called shortly after
        advanceTimeBy(1.milliseconds)
        startSyncRecorder.assertions().isCalledOnce()

        // If the sync is running and we mark the notification sync as no longer necessary, the sync stops after a delay
        syncService.emitSyncState(SyncState.Running)
        appForegroundStateService.updateIsSyncingNotificationEvent(false)

        advanceTimeBy(10.seconds)
        stopSyncRecorder.assertions().isCalledExactly(2)

        // Stop observing
        coroutineScope.cancel()
    }

    @Test
    fun `when the app was in background and we join a call, a sync will be started`() = runTest {
        val startSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val stopSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(initialSyncState = SyncState.Running).apply {
            startSyncLambda = startSyncRecorder
            stopSyncLambda = stopSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Connected)
        val appForegroundStateService = FakeAppForegroundStateService(
            initialForegroundValue = false,
            initialIsSyncingNotificationEventValue = false,
        )
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())
        val syncOrchestrator = createSyncOrchestrator(
            syncService = syncService,
            networkMonitor = networkMonitor,
            appForegroundStateService = appForegroundStateService,
            baseCoroutineScope = coroutineScope
        )

        // We start observing, we skip the initial sync attempt since the state is running
        syncOrchestrator.start()

        // Advance the time to make sure we left the initial sync behind
        advanceTimeBy(1.seconds)

        // Start sync was never called
        startSyncRecorder.assertions().isNeverCalled()

        // We stop the ongoing sync, give the sync service some time to stop
        syncService.emitSyncState(SyncState.Idle)
        advanceTimeBy(10.seconds)
        stopSyncRecorder.assertions().isCalledOnce()

        // Now we join a call
        appForegroundStateService.updateIsInCallState(true)

        // Start sync will be called shortly after
        advanceTimeBy(1.milliseconds)
        startSyncRecorder.assertions().isCalledOnce()

        // If the sync is running and we mark the in-call state as false, the sync stops after a delay
        syncService.emitSyncState(SyncState.Running)
        appForegroundStateService.updateIsInCallState(false)

        advanceTimeBy(10.seconds)
        stopSyncRecorder.assertions().isCalledExactly(2)

        // Stop observing
        coroutineScope.cancel()
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
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())
        val syncOrchestrator = createSyncOrchestrator(
            syncService = syncService,
            networkMonitor = networkMonitor,
            appForegroundStateService = appForegroundStateService,
            baseCoroutineScope = coroutineScope
        )

        // We start observing, we skip the initial sync attempt since the state is running
        syncOrchestrator.start()

        // Advance the time to make sure we left the initial sync behind
        advanceTimeBy(1.seconds)

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

        // Stop observing
        coroutineScope.cancel()
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
            initialIsSyncingNotificationEventValue = true,
            initialIsInCallValue = true,
        )
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())
        val syncOrchestrator = createSyncOrchestrator(
            syncService = syncService,
            networkMonitor = networkMonitor,
            appForegroundStateService = appForegroundStateService,
            baseCoroutineScope = coroutineScope
        )

        // We start observing, we skip the initial sync attempt since the state is running
        syncOrchestrator.start()

        // Advance the time to make sure we left the initial sync behind
        advanceTimeBy(1.seconds)

        // This will set the sync to stop
        appForegroundStateService.givenIsInForeground(false)

        // But if we reset it quickly before the stop sync takes place, the sync is not stopped
        appForegroundStateService.givenIsInForeground(true)

        advanceTimeBy(10.seconds)
        stopSyncRecorder.assertions().isNeverCalled()

        // Stop observing
        coroutineScope.cancel()
    }

    @Test
    fun `when network is offline, sync service should not start`() = runTest {
        val startSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(initialSyncState = SyncState.Running).apply {
            startSyncLambda = startSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Disconnected)
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())
        val syncOrchestrator = createSyncOrchestrator(
            syncService = syncService,
            networkMonitor = networkMonitor,
            baseCoroutineScope = coroutineScope
        )

        // We start observing, we skip the initial sync attempt since the state is running
        syncOrchestrator.start()

        // Advance the time to make sure we left the initial sync behind
        advanceTimeBy(1.seconds)

        // Set the sync state to idle
        syncService.emitSyncState(SyncState.Idle)

        // This should still not trigger a sync, since there is no network
        advanceTimeBy(10.seconds)
        startSyncRecorder.assertions().isNeverCalled()

        // Stop observing
        coroutineScope.cancel()
    }

    private fun TestScope.createSyncOrchestrator(
        syncService: FakeSyncService = FakeSyncService(),
        networkMonitor: FakeNetworkMonitor = FakeNetworkMonitor(),
        appForegroundStateService: FakeAppForegroundStateService = FakeAppForegroundStateService(),
        baseCoroutineScope: CoroutineScope = CoroutineScope(coroutineContext + SupervisorJob()),
    ) = SyncOrchestrator(
        matrixClient = FakeMatrixClient(syncService = syncService),
        networkMonitor = networkMonitor,
        appForegroundStateService = appForegroundStateService,
        sessionCoroutineScope = baseCoroutineScope,
        dispatchers = testCoroutineDispatchers(),
    )
}
