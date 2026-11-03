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
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.libraries.matrix.test.roomlist.FakeDynamicRoomList
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.libraries.matrix.test.sync.FakeSyncService
import io.element.android.libraries.push.impl.db.PushRequest
import io.element.android.libraries.push.impl.history.FakePushHistoryService
import io.element.android.libraries.push.impl.notifications.fixtures.aPushRequest
import io.element.android.services.appnavstate.test.FakeAppForegroundStateService
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

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
        roomListService = FakeRoomListService(
            allRooms = FakeDynamicRoomList(summaries = MutableStateFlow(listOf(aRoomSummary(roomId = A_ROOM_ID)))),
        )
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
    fun `when app is in background, the sync waits 10 seconds if no newer push is pending`() = runTest {
        val sut = createSyncOnNotifiableEvent(
            client = client,
            appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = false),
        )

        sut(listOf(notificationRequest))

        assertThat(currentTime).isEqualTo(10_000)
    }

    @Test
    fun `when app is in background, the sync ends as soon as a newer push is pending`() = runTest {
        val pendingRequests = MutableStateFlow(emptyList<PushRequest>())
        val sut = createSyncOnNotifiableEvent(
            client = client,
            appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = false),
            pushHistoryService = FakePushHistoryService(getPendingPushRequestsFlow = { _, _ -> pendingRequests }),
        )
        launch {
            delay(2_000)
            // An incoming call is pushed while the sync is running
            pendingRequests.value = listOf(aPushRequest(eventId = AN_EVENT_ID_2))
        }

        sut(listOf(notificationRequest))

        assertThat(currentTime).isEqualTo(2_000)
    }

    @Test
    fun `when app is in background, the sync ends right away if a newer push was already pending`() = runTest {
        val sut = createSyncOnNotifiableEvent(
            client = client,
            appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = false),
            pushHistoryService = FakePushHistoryService(
                getPendingPushRequestsFlow = { _, _ -> MutableStateFlow(listOf(aPushRequest(eventId = AN_EVENT_ID_2))) },
            ),
        )

        sut(listOf(notificationRequest))

        assertThat(currentTime).isEqualTo(0)
    }

    @Test
    fun `when app is in background, only the pushes newer than the handled ones are observed`() = runTest {
        val getPendingPushRequestsFlow = lambdaRecorder<SessionId, Instant, MutableStateFlow<List<PushRequest>>> { _, _ ->
            MutableStateFlow(emptyList())
        }
        val sut = createSyncOnNotifiableEvent(
            client = client,
            appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = false),
            pushHistoryService = FakePushHistoryService(getPendingPushRequestsFlow = getPendingPushRequestsFlow),
        )

        sut(
            listOf(
                notificationRequest.copy(pushDate = 1_000),
                aPushRequest(eventId = AN_EVENT_ID_2).copy(pushDate = 2_000),
            )
        )

        assert(getPendingPushRequestsFlow).isCalledOnce().with(value(A_SESSION_ID), value(Instant.fromEpochMilliseconds(2_000)))
    }

    private fun TestScope.createSyncOnNotifiableEvent(
        client: MatrixClient = FakeMatrixClient(),
        isSyncOnPushEnabled: Boolean = true,
        appForegroundStateService: FakeAppForegroundStateService = FakeAppForegroundStateService(
            initialForegroundValue = true,
        ),
        pushHistoryService: FakePushHistoryService = FakePushHistoryService(
            getPendingPushRequestsFlow = { _, _ -> MutableStateFlow(emptyList()) },
        ),
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
            systemClock = FakeSystemClock(),
        )
    }
}
