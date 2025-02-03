/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_UNIQUE_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.sync.FakeSyncService
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableCallEvent
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableMessageEvent
import io.element.android.services.appnavstate.test.FakeAppForegroundStateService
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SyncOnNotifiableEventTest {
    private val timelineItems = MutableStateFlow<List<MatrixTimelineItem>>(emptyList())
    private val startSyncLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
    private val stopSyncLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
    private val subscribeToSyncLambda = lambdaRecorder<Unit> { }

    private val liveTimeline = FakeTimeline(
        timelineItems = timelineItems,
    )
    private val room = FakeMatrixRoom(
        roomId = A_ROOM_ID,
        liveTimeline = liveTimeline,
        subscribeToSyncLambda = subscribeToSyncLambda
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

    private val notifiableEvent = aNotifiableMessageEvent()
    private val incomingCallNotifiableEvent = aNotifiableCallEvent()

    @Test
    fun `when feature flag is disabled, nothing happens`() = runTest {
        val sut = createSyncOnNotifiableEvent(client = client, isSyncOnPushEnabled = false)

        sut(notifiableEvent)

        assert(startSyncLambda).isNeverCalled()
        assert(stopSyncLambda).isNeverCalled()
        assert(subscribeToSyncLambda).isNeverCalled()
    }

    @Test
    fun `when feature flag is enabled, a ringing call starts and stops the sync`() = runTest {
        val sut = createSyncOnNotifiableEvent(client = client, isAppInForeground = false, isSyncOnPushEnabled = true)

        sut(incomingCallNotifiableEvent)

        assert(startSyncLambda).isCalledOnce()
        assert(stopSyncLambda).isCalledOnce()
        assert(subscribeToSyncLambda).isCalledOnce()
    }

    @Test
    fun `when feature flag is disabled, a ringing call starts and stops the sync`() = runTest {
        val sut = createSyncOnNotifiableEvent(client = client, isAppInForeground = false, isSyncOnPushEnabled = false)

        sut(incomingCallNotifiableEvent)

        assert(startSyncLambda).isCalledOnce()
        assert(stopSyncLambda).isCalledOnce()
        assert(subscribeToSyncLambda).isCalledOnce()
    }

    @Test
    fun `when feature flag is enabled and app is in foreground, sync is not started`() = runTest {
        val sut = createSyncOnNotifiableEvent(client = client, isAppInForeground = true, isSyncOnPushEnabled = true)

        sut(notifiableEvent)
        sut(incomingCallNotifiableEvent)

        assert(startSyncLambda).isNeverCalled()
        assert(stopSyncLambda).isNeverCalled()
        assert(subscribeToSyncLambda).isCalledExactly(2)
    }

    @Test
    fun `when feature flag is enabled and app is in background, sync is started and stopped`() = runTest {
        val sut = createSyncOnNotifiableEvent(client = client, isAppInForeground = false, isSyncOnPushEnabled = true)

        timelineItems.emit(
            listOf(MatrixTimelineItem.Event(A_UNIQUE_ID, anEventTimelineItem()))
        )
        syncService.emitSyncState(SyncState.Running)
        sut(notifiableEvent)

        assert(startSyncLambda).isCalledOnce()
        assert(stopSyncLambda).isCalledOnce()
        assert(subscribeToSyncLambda).isCalledOnce()
    }

    @Test
    fun `when feature flag is enabled and app is in background, running multiple time only call once`() = runTest {
        val sut = createSyncOnNotifiableEvent(client = client, isAppInForeground = false, isSyncOnPushEnabled = true)

        coroutineScope {
            launch { sut(notifiableEvent) }
            launch { sut(notifiableEvent) }
            launch {
                delay(1)
                timelineItems.emit(
                    listOf(MatrixTimelineItem.Event(A_UNIQUE_ID, anEventTimelineItem()))
                )
            }
        }

        assert(startSyncLambda).isCalledOnce()
        assert(stopSyncLambda).isCalledOnce()
        assert(subscribeToSyncLambda).isCalledExactly(2)
    }

    private fun TestScope.createSyncOnNotifiableEvent(
        client: MatrixClient = FakeMatrixClient(),
        isSyncOnPushEnabled: Boolean = true,
        isAppInForeground: Boolean = true,
    ): SyncOnNotifiableEvent {
        val featureFlagService = FakeFeatureFlagService(
            initialState = mapOf(
                FeatureFlags.SyncOnPush.key to isSyncOnPushEnabled
            )
        )
        val appForegroundStateService = FakeAppForegroundStateService(
            initialValue = isAppInForeground
        )
        val matrixClientProvider = FakeMatrixClientProvider { Result.success(client) }
        return SyncOnNotifiableEvent(
            matrixClientProvider = matrixClientProvider,
            featureFlagService = featureFlagService,
            appForegroundStateService = appForegroundStateService,
            dispatchers = testCoroutineDispatchers(),
        )
    }
}
