/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeRustRoom
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeRustRoomListService
import io.element.android.libraries.matrix.test.A_DEVICE_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RustBaseRoomTest {
    @Test
    fun `RustBaseRoom should cancel the room coroutine scope when it is destroyed`() = runTest {
        val rustBaseRoom = createRustBaseRoom(
            // Not using backgroundScope here, but the test scope
            sessionCoroutineScope = this
        )
        assertThat(rustBaseRoom.roomCoroutineScope.isActive).isTrue()
        rustBaseRoom.destroy()
        assertThat(rustBaseRoom.roomCoroutineScope.isActive).isFalse()
    }

    @Test
    fun `when currentUserMembership=JOINED and user leave room succeed then roomMembershipObserver emits change as LEFT`() = runTest {
        val roomMembershipObserver = RoomMembershipObserver()
        val rustBaseRoom = createRustBaseRoom(
            sessionCoroutineScope = this,
            initialRoomInfo = aRoomInfo(currentUserMembership = CurrentUserMembership.JOINED),
            innerRoom = FakeRustRoom(
                leaveLambda = {
                    // Simulate a successful leave
                }
            ),
            roomMembershipObserver = roomMembershipObserver,
        )
        val shared = roomMembershipObserver.updates.shareIn(scope = backgroundScope, started = SharingStarted.Eagerly, replay = 1)
        rustBaseRoom.leave()
        shared.test {
            val membershipUpdate = awaitItem()
            assertThat(membershipUpdate.roomId).isEqualTo(rustBaseRoom.roomId)
            assertThat(membershipUpdate.isUserInRoom).isFalse()
            assertThat(membershipUpdate.change).isEqualTo(MembershipChange.LEFT)
            ensureAllEventsConsumed()
        }
        rustBaseRoom.destroy()
    }

    @Test
    fun `when currentUserMembership=KNOCKED and user leave room succeed then roomMembershipObserver emits change as KNOCK_RETRACTED`() = runTest {
        val roomMembershipObserver = RoomMembershipObserver()
        val rustBaseRoom = createRustBaseRoom(
            sessionCoroutineScope = this,
            initialRoomInfo = aRoomInfo(currentUserMembership = CurrentUserMembership.KNOCKED),
            innerRoom = FakeRustRoom(
                leaveLambda = {
                    // Simulate a successful leave
                }
            ),
            roomMembershipObserver = roomMembershipObserver,
        )
        val shared = roomMembershipObserver.updates.shareIn(scope = backgroundScope, started = SharingStarted.Eagerly, replay = 1)
        rustBaseRoom.leave()
        shared.test {
            val membershipUpdate = awaitItem()
            assertThat(membershipUpdate.roomId).isEqualTo(rustBaseRoom.roomId)
            assertThat(membershipUpdate.isUserInRoom).isFalse()
            assertThat(membershipUpdate.change).isEqualTo(MembershipChange.KNOCK_RETRACTED)
            ensureAllEventsConsumed()
        }
        rustBaseRoom.destroy()
    }

    @Test
    fun `when currentUserMembership=INVITED and user leave room succeed then roomMembershipObserver emits change as INVITATION_REJECTED`() = runTest {
        val roomMembershipObserver = RoomMembershipObserver()
        val rustBaseRoom = createRustBaseRoom(
            sessionCoroutineScope = this,
            initialRoomInfo = aRoomInfo(currentUserMembership = CurrentUserMembership.INVITED),
            innerRoom = FakeRustRoom(
                leaveLambda = {
                    // Simulate a successful leave
                }
            ),
            roomMembershipObserver = roomMembershipObserver,
        )
        val shared = roomMembershipObserver.updates.shareIn(scope = backgroundScope, started = SharingStarted.Eagerly, replay = 1)
        rustBaseRoom.leave()
        shared.test {
            val membershipUpdate = awaitItem()
            assertThat(membershipUpdate.roomId).isEqualTo(rustBaseRoom.roomId)
            assertThat(membershipUpdate.isUserInRoom).isFalse()
            assertThat(membershipUpdate.change).isEqualTo(MembershipChange.INVITATION_REJECTED)
            ensureAllEventsConsumed()
        }
        rustBaseRoom.destroy()
    }

    @Test
    fun `when user leave room fails then roomMembershipObserver emits nothing`() = runTest {
        val roomMembershipObserver = RoomMembershipObserver()
        val rustBaseRoom = createRustBaseRoom(
            sessionCoroutineScope = this,
            initialRoomInfo = aRoomInfo(currentUserMembership = CurrentUserMembership.INVITED),
            innerRoom = FakeRustRoom(
                leaveLambda = { error("Leave failed") }
            ),
            roomMembershipObserver = roomMembershipObserver,
        )
        val shared = roomMembershipObserver.updates.shareIn(scope = backgroundScope, started = SharingStarted.Eagerly, replay = 1)
        rustBaseRoom.leave()
        shared.test {
            ensureAllEventsConsumed()
        }
        rustBaseRoom.destroy()
    }

    private fun TestScope.createRustBaseRoom(
        sessionCoroutineScope: CoroutineScope,
        initialRoomInfo: RoomInfo = aRoomInfo(),
        innerRoom: FakeRustRoom = FakeRustRoom(),
        roomMembershipObserver: RoomMembershipObserver = RoomMembershipObserver(),
    ): RustBaseRoom {
        val dispatchers = testCoroutineDispatchers()
        return RustBaseRoom(
            sessionId = A_SESSION_ID,
            deviceId = A_DEVICE_ID,
            innerRoom = innerRoom,
            coroutineDispatchers = dispatchers,
            roomSyncSubscriber = RoomSyncSubscriber(
                roomListService = FakeRustRoomListService(),
                dispatchers = dispatchers,
            ),
            roomMembershipObserver = roomMembershipObserver,
            sessionCoroutineScope = sessionCoroutineScope,
            roomInfoMapper = RoomInfoMapper(),
            initialRoomInfo = initialRoomInfo,
        )
    }
}
