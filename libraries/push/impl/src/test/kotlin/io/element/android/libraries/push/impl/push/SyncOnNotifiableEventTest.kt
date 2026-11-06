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
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
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
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

// Far enough from 0 so the first sync is never considered too recent
private const val INITIAL_TIMESTAMP = 1_700_000_000_000L

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

        assertThat(currentTime).isEqualTo(10.seconds.inWholeMilliseconds)
    }

    @Test
    fun `when app is in background, the sync ends as soon as a newer push is pending`() = runTest {
        val pendingRequests = MutableStateFlow(emptyList<PushRequest>())
        val sut = createSyncOnNotifiableEvent(
            client = client,
            appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = false),
            pushHistoryService = FakePushHistoryService(getPendingPushRequestsFlow = { _, _ -> pendingRequests }),
        )
        val delayAmount = 2.seconds
        launch {
            delay(delayAmount)
            // An incoming call is pushed while the sync is running
            pendingRequests.value = listOf(aPushRequest(eventId = AN_EVENT_ID_2))
        }

        sut(listOf(notificationRequest))

        assertThat(currentTime).isEqualTo(delayAmount.inWholeMilliseconds)
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

    @Test
    fun `when there are no push requests, nothing happens`() = runTest {
        val appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = false)
        val sut = createSyncOnNotifiableEvent(client = client, appForegroundStateService = appForegroundStateService)

        appForegroundStateService.isSyncingNotificationEvent.test {
            sut(emptyList())

            assertThat(awaitItem()).isFalse()
            ensureAllEventsConsumed()
        }
        assert(subscribeToSyncLambda).isNeverCalled()
    }

    @Test
    fun `when app is in foreground, no opportunistic sync is done`() = runTest {
        val getPendingPushRequestsFlow = lambdaRecorder<SessionId, Instant, MutableStateFlow<List<PushRequest>>> { _, _ ->
            MutableStateFlow(emptyList())
        }
        val appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = true)
        val sut = createSyncOnNotifiableEvent(
            client = client,
            appForegroundStateService = appForegroundStateService,
            pushHistoryService = FakePushHistoryService(getPendingPushRequestsFlow = getPendingPushRequestsFlow),
        )

        appForegroundStateService.isSyncingNotificationEvent.test {
            sut(listOf(notificationRequest))

            assertThat(awaitItem()).isFalse()
            ensureAllEventsConsumed()
        }
        assertThat(currentTime).isEqualTo(0)
        assert(getPendingPushRequestsFlow).isNeverCalled()
    }

    @Test
    fun `when app is in background, the sync ends as soon as the room list is updated`() = runTest {
        val summaries = MutableStateFlow(listOf(aRoomSummary(roomId = A_ROOM_ID)))
        val client = FakeMatrixClient(
            syncService = syncService,
            roomListService = FakeRoomListService(allRooms = FakeDynamicRoomList(summaries = summaries)),
        )
        val sut = createSyncOnNotifiableEvent(
            client = client,
            appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = false),
        )
        val delayAmount = 3.seconds
        launch {
            delay(delayAmount)
            // The sync updates the room list
            summaries.value = listOf(aRoomSummary(roomId = A_ROOM_ID_2), aRoomSummary(roomId = A_ROOM_ID))
        }

        sut(listOf(notificationRequest))

        assertThat(currentTime).isEqualTo(delayAmount.inWholeMilliseconds)
    }

    @Test
    fun `when app is in background, a new sync is skipped if the previous one completed less than a minute ago`() = runTest {
        val summaries = MutableStateFlow(listOf(aRoomSummary(roomId = A_ROOM_ID)))
        val getPendingPushRequestsFlow = lambdaRecorder<SessionId, Instant, MutableStateFlow<List<PushRequest>>> { _, _ ->
            MutableStateFlow(emptyList())
        }
        val systemClock = FakeSystemClock(epochMillisResult = INITIAL_TIMESTAMP)
        val sut = createSyncOnNotifiableEvent(
            client = aClientWithRoomList(summaries),
            appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = false),
            pushHistoryService = FakePushHistoryService(getPendingPushRequestsFlow = getPendingPushRequestsFlow),
            systemClock = systemClock,
        )

        syncWithRoomListUpdate(sut, summaries, listOf(notificationRequest))
        assert(getPendingPushRequestsFlow).isCalledOnce()

        // Less than a minute later, the sync is skipped
        systemClock.epochMillisResult = INITIAL_TIMESTAMP + 59_999
        sut(listOf(notificationRequest))
        assert(getPendingPushRequestsFlow).isCalledOnce()

        // A minute after the first sync, it runs again
        systemClock.epochMillisResult = INITIAL_TIMESTAMP + 60_000
        syncWithRoomListUpdate(sut, summaries, listOf(notificationRequest))
        assert(getPendingPushRequestsFlow).isCalledExactly(2)
    }

    @Test
    fun `when app is in background, a recent sync of a session doesn't prevent another session from syncing`() = runTest {
        val summaries = MutableStateFlow(listOf(aRoomSummary(roomId = A_ROOM_ID)))
        val getPendingPushRequestsFlow = lambdaRecorder<SessionId, Instant, MutableStateFlow<List<PushRequest>>> { _, _ ->
            MutableStateFlow(emptyList())
        }
        val sut = createSyncOnNotifiableEvent(
            client = aClientWithRoomList(summaries),
            appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = false),
            pushHistoryService = FakePushHistoryService(getPendingPushRequestsFlow = getPendingPushRequestsFlow),
        )

        syncWithRoomListUpdate(sut, summaries, listOf(aPushRequest(sessionId = A_SESSION_ID)))
        syncWithRoomListUpdate(sut, summaries, listOf(aPushRequest(sessionId = A_SESSION_ID_2)))
        // A_SESSION_ID was synced less than a minute ago, so it's skipped
        sut(listOf(aPushRequest(sessionId = A_SESSION_ID)))

        assert(getPendingPushRequestsFlow).isCalledExactly(2).withSequence(
            listOf(value(A_SESSION_ID), any()),
            listOf(value(A_SESSION_ID_2), any()),
        )
    }

    @Test
    fun `when app is in background, a sync ended early by a newer push doesn't prevent the next one`() = runTest {
        val getPendingPushRequestsFlow = lambdaRecorder<SessionId, Instant, MutableStateFlow<List<PushRequest>>> { _, _ ->
            MutableStateFlow(listOf(aPushRequest(eventId = AN_EVENT_ID_2)))
        }
        val sut = createSyncOnNotifiableEvent(
            client = client,
            appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = false),
            pushHistoryService = FakePushHistoryService(getPendingPushRequestsFlow = getPendingPushRequestsFlow),
        )

        sut(listOf(notificationRequest))
        sut(listOf(notificationRequest))

        assert(getPendingPushRequestsFlow).isCalledExactly(2)
    }

    @Test
    fun `when app is in background, a sync that timed out doesn't prevent the next one`() = runTest {
        val getPendingPushRequestsFlow = lambdaRecorder<SessionId, Instant, MutableStateFlow<List<PushRequest>>> { _, _ ->
            MutableStateFlow(emptyList())
        }
        val sut = createSyncOnNotifiableEvent(
            client = client,
            appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = false),
            pushHistoryService = FakePushHistoryService(getPendingPushRequestsFlow = getPendingPushRequestsFlow),
        )

        sut(listOf(notificationRequest))
        assertThat(currentTime).isEqualTo(10.seconds.inWholeMilliseconds)
        sut(listOf(notificationRequest))

        assert(getPendingPushRequestsFlow).isCalledExactly(2)
    }

    @Test
    fun `when app is in background and the clock goes backwards, the sync is not skipped`() = runTest {
        val summaries = MutableStateFlow(listOf(aRoomSummary(roomId = A_ROOM_ID)))
        val getPendingPushRequestsFlow = lambdaRecorder<SessionId, Instant, MutableStateFlow<List<PushRequest>>> { _, _ ->
            MutableStateFlow(emptyList())
        }
        val systemClock = FakeSystemClock(epochMillisResult = INITIAL_TIMESTAMP)
        val sut = createSyncOnNotifiableEvent(
            client = aClientWithRoomList(summaries),
            appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = false),
            pushHistoryService = FakePushHistoryService(getPendingPushRequestsFlow = getPendingPushRequestsFlow),
            systemClock = systemClock,
        )

        syncWithRoomListUpdate(sut, summaries, listOf(notificationRequest))
        systemClock.epochMillisResult = INITIAL_TIMESTAMP - 1_000
        syncWithRoomListUpdate(sut, summaries, listOf(notificationRequest))

        assert(getPendingPushRequestsFlow).isCalledExactly(2)
    }

    @Test
    fun `when app is in background, concurrent syncs of several sessions keep the syncing flag until they all end`() = runTest {
        val appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = false)
        val tracker = OpportunisticSyncTracker(appForegroundStateService, FakeSystemClock(epochMillisResult = INITIAL_TIMESTAMP))
        // 2 instances sharing the same tracker, like 2 workers would do
        val sut1 = createSyncOnNotifiableEvent(client = client, appForegroundStateService = appForegroundStateService, tracker = tracker)
        val sut2 = createSyncOnNotifiableEvent(client = client, appForegroundStateService = appForegroundStateService, tracker = tracker)

        appForegroundStateService.isSyncingNotificationEvent.test {
            assertThat(awaitItem()).isFalse()
            launch { sut1(listOf(aPushRequest(sessionId = A_SESSION_ID))) }
            launch {
                delay(5.seconds)
                sut2(listOf(aPushRequest(sessionId = A_SESSION_ID_2)))
            }
            assertThat(awaitItem()).isTrue()
            // The first sync times out after 10s, the second one is still running
            advanceUntilIdle()
            assertThat(awaitItem()).isFalse()
            assertThat(currentTime).isEqualTo(15.seconds.inWholeMilliseconds)
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `when app is in background, a skipped sync doesn't reset the syncing flag of a running one`() = runTest {
        val appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = false)
        val tracker = OpportunisticSyncTracker(appForegroundStateService, FakeSystemClock(epochMillisResult = INITIAL_TIMESTAMP))
        val sut1 = createSyncOnNotifiableEvent(client = client, appForegroundStateService = appForegroundStateService, tracker = tracker)
        val sut2 = createSyncOnNotifiableEvent(client = client, appForegroundStateService = appForegroundStateService, tracker = tracker)

        launch { sut1(listOf(notificationRequest)) }
        delay(1.seconds)
        // Same session, already syncing: skipped
        sut2(listOf(notificationRequest))
        assertThat(appForegroundStateService.isSyncingNotificationEvent.value).isTrue()

        advanceUntilIdle()
        assertThat(appForegroundStateService.isSyncingNotificationEvent.value).isFalse()
    }

    private fun aClientWithRoomList(summaries: MutableStateFlow<List<RoomSummary>>) = FakeMatrixClient(
        syncService = syncService,
        roomListService = FakeRoomListService(allRooms = FakeDynamicRoomList(summaries = summaries)),
    )

    /**
     * Runs [sut] while the room list gets updated, so the sync completes.
     */
    private suspend fun TestScope.syncWithRoomListUpdate(
        sut: SyncOnNotifiableEvent,
        summaries: MutableStateFlow<List<RoomSummary>>,
        requests: List<PushRequest>,
    ) {
        launch {
            delay(1.seconds)
            summaries.value = summaries.value.reversed() + aRoomSummary(roomId = A_ROOM_ID_2)
        }
        sut(requests)
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
        systemClock: FakeSystemClock = FakeSystemClock(epochMillisResult = INITIAL_TIMESTAMP),
        tracker: OpportunisticSyncTracker = OpportunisticSyncTracker(appForegroundStateService, systemClock),
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
            opportunisticSyncTracker = tracker,
        )
    }
}
