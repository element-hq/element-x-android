/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav

import io.element.android.appnav.di.DefaultSyncOrchestrator
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSyncOrchestratorTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `when the sync wasn't running before, an initial sync will always take place, even with no network`() = runTest {
        val stateFlow = MutableStateFlow<SyncState>(SyncState.Idle)
        val startSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(syncStateFlow = stateFlow).apply {
            startSyncLambda = startSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Offline)
        val syncOrchestrator = createSyncOrchestrator(syncService, networkMonitor)

        // We start observing
        syncOrchestrator.start()

        // Advance the time to make sure we left the initial sync behind
        advanceTimeBy(1.seconds)

        // Start sync will be called shortly after
        startSyncRecorder.assertions().isCalledOnce()

        // Stop observing
        syncOrchestrator.stop()
    }

    @Test
    fun `when the app goes to background and the sync was running, it will be stopped after a delay`() = runTest {
        val stateFlow = MutableStateFlow<SyncState>(SyncState.Running)
        val stopSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(syncStateFlow = stateFlow).apply {
            stopSyncLambda = stopSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Online)
        val appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = true)
        val syncOrchestrator = createSyncOrchestrator(syncService, networkMonitor, appForegroundStateService)

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
        syncOrchestrator.stop()
    }

    @Test
    fun `when the app was in background and we receive a notification, a sync will be started then stopped`() = runTest {
        val stateFlow = MutableStateFlow<SyncState>(SyncState.Running)
        val startSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val stopSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(syncStateFlow = stateFlow).apply {
            startSyncLambda = startSyncRecorder
            stopSyncLambda = stopSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Online)
        val appForegroundStateService = FakeAppForegroundStateService(
            initialForegroundValue = false,
            initialIsSyncingNotificationEventValue = false,
        )
        val syncOrchestrator = createSyncOrchestrator(syncService, networkMonitor, appForegroundStateService)

        // We start observing, we skip the initial sync attempt since the state is running
        syncOrchestrator.start()

        // Advance the time to make sure we left the initial sync behind
        advanceTimeBy(1.seconds)

        // Start sync was never called
        startSyncRecorder.assertions().isNeverCalled()

        // We stop the ongoing sync, give the sync service some time to stop
        stateFlow.value = SyncState.Idle
        advanceTimeBy(10.seconds)
        stopSyncRecorder.assertions().isCalledOnce()

        // Now we receive a notification and need to sync
        appForegroundStateService.updateIsSyncingNotificationEvent(true)

        // Start sync will be called shortly after
        advanceTimeBy(1.milliseconds)
        startSyncRecorder.assertions().isCalledOnce()

        // If the sync is running and we mark the notification sync as no longer necessary, the sync stops after a delay
        stateFlow.value = SyncState.Running
        appForegroundStateService.updateIsSyncingNotificationEvent(false)

        advanceTimeBy(10.seconds)
        stopSyncRecorder.assertions().isCalledExactly(2)

        // Stop observing
        syncOrchestrator.stop()
    }

    @Test
    fun `when the app was in background and we join a call, a sync will be started`() = runTest {
        val stateFlow = MutableStateFlow<SyncState>(SyncState.Running)
        val startSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val stopSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(syncStateFlow = stateFlow).apply {
            startSyncLambda = startSyncRecorder
            stopSyncLambda = stopSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Online)
        val appForegroundStateService = FakeAppForegroundStateService(
            initialForegroundValue = false,
            initialIsSyncingNotificationEventValue = false,
        )
        val syncOrchestrator = createSyncOrchestrator(syncService, networkMonitor, appForegroundStateService)

        // We start observing, we skip the initial sync attempt since the state is running
        syncOrchestrator.start()

        // Advance the time to make sure we left the initial sync behind
        advanceTimeBy(1.seconds)

        // Start sync was never called
        startSyncRecorder.assertions().isNeverCalled()

        // We stop the ongoing sync, give the sync service some time to stop
        stateFlow.value = SyncState.Idle
        advanceTimeBy(10.seconds)
        stopSyncRecorder.assertions().isCalledOnce()

        // Now we join a call
        appForegroundStateService.updateIsInCallState(true)

        // Start sync will be called shortly after
        advanceTimeBy(1.milliseconds)
        startSyncRecorder.assertions().isCalledOnce()

        // If the sync is running and we mark the in-call state as false, the sync stops after a delay
        stateFlow.value = SyncState.Running
        appForegroundStateService.updateIsInCallState(false)

        advanceTimeBy(10.seconds)
        stopSyncRecorder.assertions().isCalledExactly(2)

        // Stop observing
        syncOrchestrator.stop()
    }

    @Test
    fun `when the app is in foreground, we sync for a notification and a call is ongoing, the sync will only stop when all conditions are false`() = runTest {
        val stateFlow = MutableStateFlow<SyncState>(SyncState.Running)
        val startSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val stopSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(syncStateFlow = stateFlow).apply {
            startSyncLambda = startSyncRecorder
            stopSyncLambda = stopSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Online)
        val appForegroundStateService = FakeAppForegroundStateService(
            initialForegroundValue = true,
            initialIsSyncingNotificationEventValue = true,
            initialIsInCallValue = true,
        )
        val syncOrchestrator = createSyncOrchestrator(syncService, networkMonitor, appForegroundStateService)

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
        syncOrchestrator.stop()
    }

    @Test
    fun `if the sync was running, it's set to be stopped but something triggers a sync again, the sync is not stopped`() = runTest {
        val stateFlow = MutableStateFlow<SyncState>(SyncState.Running)
        val stopSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(syncStateFlow = stateFlow).apply {
            stopSyncLambda = stopSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Online)
        val appForegroundStateService = FakeAppForegroundStateService(
            initialForegroundValue = true,
            initialIsSyncingNotificationEventValue = true,
            initialIsInCallValue = true,
        )
        val syncOrchestrator = createSyncOrchestrator(syncService, networkMonitor, appForegroundStateService)

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
        syncOrchestrator.stop()
    }

    @Test
    fun `when network is offline, sync service should not start`() = runTest {
        val stateFlow = MutableStateFlow<SyncState>(SyncState.Running)
        val startSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(syncStateFlow = stateFlow).apply {
            startSyncLambda = startSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Offline)
        val syncOrchestrator = createSyncOrchestrator(syncService, networkMonitor)

        // We start observing, we skip the initial sync attempt since the state is running
        syncOrchestrator.start()

        // Advance the time to make sure we left the initial sync behind
        advanceTimeBy(1.seconds)

        // Set the sync state to idle
        stateFlow.value = SyncState.Idle

        // This should still not trigger a sync, since there is no network
        advanceTimeBy(10.seconds)
        startSyncRecorder.assertions().isNeverCalled()

        // Stop observing
        syncOrchestrator.stop()
    }

    @Test
    fun `when sync was running and network is now offline, sync service should be stopped`() = runTest {
        val stateFlow = MutableStateFlow<SyncState>(SyncState.Running)
        val stopSyncRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val syncService = FakeSyncService(syncStateFlow = stateFlow).apply {
            stopSyncLambda = stopSyncRecorder
        }
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Online)
        val syncOrchestrator = createSyncOrchestrator(syncService, networkMonitor)

        // We start observing, we skip the initial sync attempt since the state is running
        syncOrchestrator.start()

        // Advance the time to make sure we left the initial sync behind
        advanceTimeBy(1.seconds)

        // Network is now offline
        networkMonitor.connectivity.value = NetworkStatus.Offline

        // This will stop the sync after some delay
        stopSyncRecorder.assertions().isNeverCalled()
        advanceTimeBy(10.seconds)
        stopSyncRecorder.assertions().isCalledOnce()

        // Stop observing
        syncOrchestrator.stop()
    }

    private fun TestScope.createSyncOrchestrator(
        syncService: FakeSyncService = FakeSyncService(),
        networkMonitor: FakeNetworkMonitor = FakeNetworkMonitor(),
        appForegroundStateService: FakeAppForegroundStateService = FakeAppForegroundStateService(),
    ) = DefaultSyncOrchestrator(
        matrixClient = FakeMatrixClient(syncService = syncService),
        networkMonitor = networkMonitor,
        appForegroundStateService = appForegroundStateService,
        baseCoroutineScope = CoroutineScope(coroutineContext + SupervisorJob()),
        dispatchers = testCoroutineDispatchers(),
    )
}
