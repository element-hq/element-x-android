/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.live

import com.google.common.truth.Truth.assertThat
import io.element.android.features.location.api.Location
import io.element.android.features.location.api.live.ActiveLiveLocationShare
import io.element.android.features.location.impl.live.service.FakeLiveLocationSharingCoordinator
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.services.appnavstate.api.ActiveRoomsHolder
import io.element.android.services.toolbox.api.systemclock.SystemClock
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class DefaultActiveLiveLocationShareManagerTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `starting the first share registers the manager and adds an active share`() = runTest {
        val coordinator = FakeLiveLocationSharingCoordinator()
        val room = FakeJoinedRoom(
            startLiveLocationShareResult = { Result.success(Unit) },
        )
        val manager = aManager(
            client = FakeMatrixClient(sessionId = A_SESSION_ID).also { it.givenGetRoomResult(A_ROOM_ID, room) },
            coordinator = coordinator,
            clock = FakeSystemClock(epochMillisResult = 123L),
        )

        val result = manager.startShare(A_ROOM_ID, 60.minutes)

        assertThat(result.isSuccess).isTrue()
        assertThat(manager.activeShares.value).containsExactly(
            A_ROOM_ID,
            ActiveLiveLocationShare(
                roomId = A_ROOM_ID,
                expiresAt = kotlin.time.Instant.fromEpochMilliseconds(3_600_123L),
            ),
        )
        assertThat(coordinator.registeredSessionIds).containsExactly(A_SESSION_ID)
    }

    @Test
    fun `stopping the last share unregisters the manager`() = runTest {
        val coordinator = FakeLiveLocationSharingCoordinator()
        val room = FakeJoinedRoom(
            startLiveLocationShareResult = { Result.success(Unit) },
            stopLiveLocationShareResult = { Result.success(Unit) },
        )
        val manager = aManager(
            client = FakeMatrixClient(sessionId = A_SESSION_ID).also { it.givenGetRoomResult(A_ROOM_ID, room) },
            coordinator = coordinator,
        )

        manager.startShare(A_ROOM_ID, 15.minutes)

        val result = manager.stopShare(A_ROOM_ID)

        assertThat(result.isSuccess).isTrue()
        assertThat(manager.activeShares.value).isEmpty()
        assertThat(coordinator.registeredSessionIds).isEmpty()
    }

    @Test
    fun `onLocationUpdate prefers ActiveRoomsHolder before sdk lookup`() = runTest {
        val sentByHeldRoom = mutableListOf<String>()
        val heldRoom = FakeJoinedRoom(
            startLiveLocationShareResult = { Result.success(Unit) },
            sendLiveLocationResult = { geoUri ->
                sentByHeldRoom += geoUri
                Result.success(Unit)
            },
        )
        val clientRoom = FakeJoinedRoom(
            startLiveLocationShareResult = { Result.success(Unit) },
            sendLiveLocationResult = { error("client room should not send live location") },
        )
        val manager = aManager(
            activeRoomsHolder = activeRoomsHolder(heldRoom),
            client = FakeMatrixClient(sessionId = A_SESSION_ID).also { it.givenGetRoomResult(A_ROOM_ID, clientRoom) },
        )

        manager.startShare(A_ROOM_ID, 15.minutes)
        manager.onLocationUpdate(Location(lat = 51.5074, lon = -0.1278, accuracy = 10f))

        assertThat(sentByHeldRoom).containsExactly("geo:51.5074,-0.1278;u=10.0")
    }

    @Test
    fun `two managers with the same room id keep isolated state per session`() = runTest {
        val coordinator = FakeLiveLocationSharingCoordinator()
        val managerOne = aManager(
            client = FakeMatrixClient(sessionId = A_SESSION_ID).also {
                it.givenGetRoomResult(
                    A_ROOM_ID,
                    FakeJoinedRoom(startLiveLocationShareResult = { Result.success(Unit) }),
                )
            },
            coordinator = coordinator,
        )
        val managerTwo = aManager(
            client = FakeMatrixClient(sessionId = A_SESSION_ID_2).also {
                it.givenGetRoomResult(
                    A_ROOM_ID,
                    FakeJoinedRoom(startLiveLocationShareResult = { Result.success(Unit) }),
                )
            },
            coordinator = coordinator,
        )

        managerOne.startShare(A_ROOM_ID, 15.minutes)

        assertThat(managerOne.activeShares.value).containsKey(A_ROOM_ID)
        assertThat(managerTwo.activeShares.value).isEmpty()
    }

    private fun aManager(
        client: FakeMatrixClient = FakeMatrixClient(sessionId = A_SESSION_ID),
        activeRoomsHolder: ActiveRoomsHolder = activeRoomsHolder(),
        coordinator: FakeLiveLocationSharingCoordinator = FakeLiveLocationSharingCoordinator(),
        clock: SystemClock = FakeSystemClock(),
    ): DefaultActiveLiveLocationShareManager {
        return DefaultActiveLiveLocationShareManager(
            matrixClient = client,
            activeRoomsHolder = activeRoomsHolder,
            coordinator = coordinator,
            clock = clock,
        )
    }

    private fun activeRoomsHolder(vararg rooms: JoinedRoom): ActiveRoomsHolder {
        val roomsBySession = rooms.groupBy { it.sessionId }
        return object : ActiveRoomsHolder {
            override fun addRoom(room: JoinedRoom) = Unit

            override fun getActiveRoom(sessionId: SessionId): JoinedRoom? {
                return roomsBySession[sessionId]?.lastOrNull()
            }

            override fun getActiveRoomMatching(sessionId: SessionId, roomId: RoomId): JoinedRoom? {
                return roomsBySession[sessionId]?.firstOrNull { it.roomId == roomId }
            }

            override fun removeRoom(sessionId: SessionId, roomId: RoomId) = Unit

            override fun clear(sessionId: SessionId) = Unit
        }
    }
}
