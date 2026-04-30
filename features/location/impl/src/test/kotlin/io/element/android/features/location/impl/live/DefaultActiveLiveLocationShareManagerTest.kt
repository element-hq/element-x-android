/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.live

import com.google.common.truth.Truth.assertThat
import io.element.android.features.location.api.live.ActiveLiveLocationShare
import io.element.android.features.location.impl.live.service.LiveLocationSharingCoordinator
import io.element.android.libraries.matrix.api.room.location.BeaconInfoUpdate
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.services.toolbox.api.systemclock.SystemClock
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultActiveLiveLocationShareManagerTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `starting the first share starts the coordinator service after the beacon echo and adds an active share`() = runTest {
        val startServiceRecorder = lambdaRecorder<Unit> { }
        val stopServiceRecorder = lambdaRecorder<Unit> { }
        val coordinator = createCoordinator(
            startService = startServiceRecorder,
            stopService = stopServiceRecorder
        )
        val beaconInfoUpdates = MutableSharedFlow<BeaconInfoUpdate>(replay = 1)
        val room = FakeJoinedRoom(
            startLiveLocationShareResult = { Result.success(AN_EVENT_ID) },
            stopLiveLocationShareResult = { Result.success(Unit) },
        )
        val manager = createManager(
            client = FakeMatrixClient(
                sessionId = A_SESSION_ID,
                sessionCoroutineScope = backgroundScope,
                ownBeaconInfoUpdates = beaconInfoUpdates,
            ).also { it.givenGetRoomResult(A_ROOM_ID, room) },
            coordinator = coordinator,
            clock = FakeSystemClock(epochMillisResult = 123L),
        )
        advanceUntilIdle()

        val result = async { manager.startShare(A_ROOM_ID, 60.minutes) }
        beaconInfoUpdates.emit(BeaconInfoUpdate(roomId = A_ROOM_ID, beaconId = AN_EVENT_ID, isLive = true))

        assertThat(result.await().isSuccess).isTrue()
        assertThat(manager.activeShares.value).containsExactly(
            A_ROOM_ID,
            ActiveLiveLocationShare(
                beaconId = AN_EVENT_ID,
                roomId = A_ROOM_ID,
                expiresAt = Instant.fromEpochMilliseconds(3_600_123L),
            ),
        )
        assert(startServiceRecorder).isCalledOnce()
        assert(stopServiceRecorder).isNeverCalled()
    }

    @Test
    fun `stopping the last share stops the coordinator service`() = runTest {
        val startServiceRecorder = lambdaRecorder<Unit> { }
        val stopServiceRecorder = lambdaRecorder<Unit> { }
        val coordinator = createCoordinator(
            startService = startServiceRecorder,
            stopService = stopServiceRecorder
        )
        val beaconInfoUpdates = MutableSharedFlow<BeaconInfoUpdate>(replay = 1)
        val room = FakeJoinedRoom(
            startLiveLocationShareResult = { Result.success(AN_EVENT_ID) },
            stopLiveLocationShareResult = { Result.success(Unit) },
        )
        val manager = createManager(
            client = FakeMatrixClient(
                sessionId = A_SESSION_ID,
                sessionCoroutineScope = backgroundScope,
                ownBeaconInfoUpdates = beaconInfoUpdates,
            ).also { it.givenGetRoomResult(A_ROOM_ID, room) },
            coordinator = coordinator,
        )
        advanceUntilIdle()

        val startResult = async { manager.startShare(A_ROOM_ID, 15.minutes) }
        beaconInfoUpdates.emit(BeaconInfoUpdate(roomId = A_ROOM_ID, beaconId = AN_EVENT_ID, isLive = true))
        assertThat(startResult.await().isSuccess).isTrue()

        val result = manager.stopShare(A_ROOM_ID)

        assertThat(result.isSuccess).isTrue()
        assertThat(manager.activeShares.value).isEmpty()
        assert(startServiceRecorder).isCalledOnce()
        assert(stopServiceRecorder).isCalledOnce()
    }

    @Test
    fun `two managers with the same room id keep isolated state per session`() = runTest {
        val coordinator = createCoordinator()
        val beaconInfoUpdatesOne = MutableSharedFlow<BeaconInfoUpdate>(replay = 1)
        val beaconInfoUpdatesTwo = MutableSharedFlow<BeaconInfoUpdate>(replay = 1)
        val managerOne = createManager(
            client = FakeMatrixClient(
                sessionId = A_SESSION_ID,
                sessionCoroutineScope = backgroundScope,
                ownBeaconInfoUpdates = beaconInfoUpdatesOne,
            ).also {
                it.givenGetRoomResult(
                    A_ROOM_ID,
                    FakeJoinedRoom(
                        startLiveLocationShareResult = { Result.success(AN_EVENT_ID) },
                        stopLiveLocationShareResult = { Result.success(Unit) },
                    ),
                )
            },
            coordinator = coordinator,
        )
        val managerTwo = createManager(
            client = FakeMatrixClient(
                sessionId = A_SESSION_ID_2,
                sessionCoroutineScope = backgroundScope,
                ownBeaconInfoUpdates = beaconInfoUpdatesTwo,
            ).also {
                it.givenGetRoomResult(
                    A_ROOM_ID,
                    FakeJoinedRoom(
                        startLiveLocationShareResult = { Result.success(AN_EVENT_ID) },
                        stopLiveLocationShareResult = { Result.success(Unit) },
                    ),
                )
            },
            coordinator = coordinator,
        )
        advanceUntilIdle()

        val startResult = async { managerOne.startShare(A_ROOM_ID, 15.minutes) }
        beaconInfoUpdatesOne.emit(BeaconInfoUpdate(roomId = A_ROOM_ID, beaconId = AN_EVENT_ID, isLive = true))
        assertThat(startResult.await().isSuccess).isTrue()

        assertThat(managerOne.activeShares.value).containsKey(A_ROOM_ID)
        assertThat(managerTwo.activeShares.value).isEmpty()
    }

    private fun createManager(
        client: FakeMatrixClient = FakeMatrixClient(sessionId = A_SESSION_ID),
        coordinator: LiveLocationSharingCoordinator = createCoordinator(),
        clock: SystemClock = FakeSystemClock(),
    ): DefaultActiveLiveLocationShareManager {
        return DefaultActiveLiveLocationShareManager(
            matrixClient = client,
            coordinator = coordinator,
            clock = clock,
        )
    }

    private fun createCoordinator(
        startService: () -> Unit = {},
        stopService: () -> Unit = {},
        nowMillis: () -> Long = { 0L },
    ): LiveLocationSharingCoordinator {
        return LiveLocationSharingCoordinator(
            startService = startService,
            stopService = stopService,
            nowMillis = nowMillis,
        )
    }
}
