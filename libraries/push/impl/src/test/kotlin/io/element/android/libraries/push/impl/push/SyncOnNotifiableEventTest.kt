/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.sync.FakeSyncService
import io.element.android.libraries.push.impl.db.PushRequest
import io.element.android.libraries.push.impl.history.FakePushHistoryService
import io.element.android.libraries.push.impl.notifications.fixtures.aPushRequest
import io.element.android.services.appnavstate.test.FakeAppForegroundStateService
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncOnNotifiableEventTest {
    private val startSyncLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
    private val stopSyncLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
    private val subscribeToSyncLambda = lambdaRecorder<Unit> { }

    private val room = FakeJoinedRoom(
        baseRoom = FakeBaseRoom(
            roomId = A_ROOM_ID,
            subscribeToSyncLambda = subscribeToSyncLambda,
        ),
    )
    private val syncService = FakeSyncService(SyncState.Idle).also {
        it.startSyncLambda = startSyncLambda
        it.stopSyncLambda = stopSyncLambda
    }

    private val client = FakeMatrixClient(
        syncService = syncService,
    ).apply {
        givenGetRoomResult(A_ROOM_ID, room)
    }

    private val notificationRequest = aPushRequest()

    @Test
    fun `when feature flag is disabled, nothing happens`() = runTest {
        val sut = createSyncOnNotifiableEvent(client = client, isSyncOnPushEnabled = false)

        sut(listOf(notificationRequest))

        assert(startSyncLambda).isNeverCalled()
        assert(stopSyncLambda).isNeverCalled()
        assert(subscribeToSyncLambda).isNeverCalled()
    }

    @Test
    fun `when feature flag is enabled and app is in background, sync is started and stopped`() = runTest {
        val appForegroundStateService = FakeAppForegroundStateService(
            initialForegroundValue = false,
        )
        val sut = createSyncOnNotifiableEvent(client = client, appForegroundStateService = appForegroundStateService, isSyncOnPushEnabled = true)

        appForegroundStateService.isSyncingNotificationEvent.test {
            syncService.emitSyncState(SyncState.Running)
            sut(listOf(notificationRequest))

            // It's initially false
            assertThat(awaitItem()).isFalse()
            // Then it becomes true when we receive the push
            assertThat(awaitItem()).isTrue()
            // It becomes false once when the push is processed
            assertThat(awaitItem()).isFalse()

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `when feature flag is enabled and app is in background, running multiple times only call once`() = runTest {
        val appForegroundStateService = FakeAppForegroundStateService(
            initialForegroundValue = false,
        )
        val sut = createSyncOnNotifiableEvent(client = client, appForegroundStateService = appForegroundStateService, isSyncOnPushEnabled = true)

        appForegroundStateService.isSyncingNotificationEvent.test {
            launch { sut(listOf(notificationRequest)) }
            launch { sut(listOf(notificationRequest)) }

            // It's initially false
            assertThat(awaitItem()).isFalse()
            // Then it becomes true once, for the first received push
            assertThat(awaitItem()).isTrue()
            // It becomes false once all pushes are processed
            assertThat(awaitItem()).isFalse()

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `when app is in background, the sync waits 10 seconds if no other push is queued`() = runTest {
        val sut = createSyncOnNotifiableEvent(
            client = client,
            appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = false),
        )

        sut(listOf(notificationRequest))

        assertThat(currentTime).isEqualTo(10_000)
    }

    @Test
    fun `when app is in background, the sync ends as soon as a new push is queued`() = runTest {
        var pendingRequests = emptyList<PushRequest>()
        val queuedPushRequestNotifier = QueuedPushRequestNotifier()
        val sut = createSyncOnNotifiableEvent(
            client = client,
            appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = false),
            pushHistoryService = FakePushHistoryService(getPendingPushRequests = { _, _ -> Result.success(pendingRequests) }),
            queuedPushRequestNotifier = queuedPushRequestNotifier,
        )
        launch {
            delay(2_000)
            // An incoming call is pushed while the sync is running
            pendingRequests = listOf(aPushRequest(eventId = AN_EVENT_ID_2))
            queuedPushRequestNotifier.onPushRequestQueued()
        }

        sut(listOf(notificationRequest))

        assertThat(currentTime).isEqualTo(2_000)
    }

    @Test
    fun `when app is in background, the sync ends right away if a push was queued before it started`() = runTest {
        val sut = createSyncOnNotifiableEvent(
            client = client,
            appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = false),
            pushHistoryService = FakePushHistoryService(getPendingPushRequests = { _, _ -> Result.success(listOf(aPushRequest(eventId = AN_EVENT_ID_2))) }),
        )

        sut(listOf(notificationRequest))

        assertThat(currentTime).isEqualTo(0)
    }

    @Test
    fun `when app is in background, a push handled by the current work and pending again doesn't end the sync early`() = runTest {
        val sut = createSyncOnNotifiableEvent(
            client = client,
            appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = false),
            // The push this work just handled is pending again, for instance after a network error
            pushHistoryService = FakePushHistoryService(getPendingPushRequests = { _, _ -> Result.success(listOf(notificationRequest)) }),
        )

        sut(listOf(notificationRequest))

        assertThat(currentTime).isEqualTo(10_000)
    }

    private fun TestScope.createSyncOnNotifiableEvent(
        client: MatrixClient = FakeMatrixClient(),
        isSyncOnPushEnabled: Boolean = true,
        appForegroundStateService: FakeAppForegroundStateService = FakeAppForegroundStateService(
            initialForegroundValue = true,
        ),
        pushHistoryService: FakePushHistoryService = FakePushHistoryService(getPendingPushRequests = { _, _ -> Result.success(emptyList()) }),
        queuedPushRequestNotifier: QueuedPushRequestNotifier = QueuedPushRequestNotifier(),
    ): SyncOnNotifiableEvent {
        val featureFlagService = FakeFeatureFlagService(
            initialState = mapOf(
                FeatureFlags.SyncOnPush.key to isSyncOnPushEnabled
            )
        )
        val matrixClientProvider = FakeMatrixClientProvider { Result.success(client) }
        return DefaultSyncOnNotifiableEvent(
            matrixClientProvider = matrixClientProvider,
            featureFlagService = featureFlagService,
            appForegroundStateService = appForegroundStateService,
            dispatchers = testCoroutineDispatchers(),
            pushHistoryService = pushHistoryService,
            queuedPushRequestNotifier = queuedPushRequestNotifier,
            systemClock = FakeSystemClock(),
        )
    }
}
