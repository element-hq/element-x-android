/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevels
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiRoom
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiRoomListService
import io.element.android.libraries.matrix.test.A_DEVICE_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.test.room.defaultRoomPowerLevelValues
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Ignore
import org.junit.Test
import uniffi.matrix_sdk.RoomMemberRole

@Ignore("JNA direct mapping has broken unit tests with FFI fakes")
class RustBaseRoomTest {
    @Test
    fun `RustBaseRoom should cancel the room coroutine scope when it is destroyed`() = runTest {
        val rustBaseRoom = createRustBaseRoom()
        assertThat(rustBaseRoom.roomCoroutineScope.isActive).isTrue()
        rustBaseRoom.destroy()
        assertThat(rustBaseRoom.roomCoroutineScope.isActive).isFalse()
    }

    @Test
    fun `when currentUserMembership=JOINED and user leave room succeed then roomMembershipObserver emits change as LEFT`() = runTest {
        val roomMembershipObserver = RoomMembershipObserver()
        val rustBaseRoom = createRustBaseRoom(
            initialRoomInfo = aRoomInfo(currentUserMembership = CurrentUserMembership.JOINED),
            innerRoom = FakeFfiRoom(
                leaveLambda = {
                    // Simulate a successful leave
                }
            ),
            roomMembershipObserver = roomMembershipObserver,
        )
        leaveRoomAndObserveMembershipChange(roomMembershipObserver, rustBaseRoom) {
            val membershipUpdate = awaitItem()
            assertThat(membershipUpdate.roomId).isEqualTo(rustBaseRoom.roomId)
            assertThat(membershipUpdate.isSpace).isFalse()
            assertThat(membershipUpdate.isUserInRoom).isFalse()
            assertThat(membershipUpdate.change).isEqualTo(MembershipChange.LEFT)
        }
    }

    @Test
    fun `when currentUserMembership=KNOCKED and user leave room succeed then roomMembershipObserver emits change as KNOCK_RETRACTED`() = runTest {
        val roomMembershipObserver = RoomMembershipObserver()
        val rustBaseRoom = createRustBaseRoom(
            initialRoomInfo = aRoomInfo(currentUserMembership = CurrentUserMembership.KNOCKED),
            innerRoom = FakeFfiRoom(
                leaveLambda = {
                    // Simulate a successful leave
                }
            ),
            roomMembershipObserver = roomMembershipObserver,
        )
        leaveRoomAndObserveMembershipChange(roomMembershipObserver, rustBaseRoom) {
            val membershipUpdate = awaitItem()
            assertThat(membershipUpdate.roomId).isEqualTo(rustBaseRoom.roomId)
            assertThat(membershipUpdate.isSpace).isFalse()
            assertThat(membershipUpdate.isUserInRoom).isFalse()
            assertThat(membershipUpdate.change).isEqualTo(MembershipChange.KNOCK_RETRACTED)
        }
    }

    @Test
    fun `when currentUserMembership=INVITED and user leave room succeed then roomMembershipObserver emits change as INVITATION_REJECTED`() = runTest {
        val roomMembershipObserver = RoomMembershipObserver()
        val rustBaseRoom = createRustBaseRoom(
            initialRoomInfo = aRoomInfo(currentUserMembership = CurrentUserMembership.INVITED),
            innerRoom = FakeFfiRoom(
                leaveLambda = {
                    // Simulate a successful leave
                }
            ),
            roomMembershipObserver = roomMembershipObserver,
        )
        leaveRoomAndObserveMembershipChange(roomMembershipObserver, rustBaseRoom) {
            val membershipUpdate = awaitItem()
            assertThat(membershipUpdate.roomId).isEqualTo(rustBaseRoom.roomId)
            assertThat(membershipUpdate.isSpace).isFalse()
            assertThat(membershipUpdate.isUserInRoom).isFalse()
            assertThat(membershipUpdate.change).isEqualTo(MembershipChange.INVITATION_REJECTED)
        }
    }

    @Test
    fun `when user leave room fails then roomMembershipObserver emits nothing`() = runTest {
        val roomMembershipObserver = RoomMembershipObserver()
        val rustBaseRoom = createRustBaseRoom(
            initialRoomInfo = aRoomInfo(currentUserMembership = CurrentUserMembership.INVITED),
            innerRoom = FakeFfiRoom(
                leaveLambda = { error("Leave failed") }
            ),
            roomMembershipObserver = roomMembershipObserver,
        )
        leaveRoomAndObserveMembershipChange(roomMembershipObserver, rustBaseRoom) {
            // No emit
        }
    }

    @Test
    fun `userRole loads and maps the role`() = runTest {
        val rustBaseRoom = createRustBaseRoom(
            initialRoomInfo = aRoomInfo(
                roomPowerLevels = RoomPowerLevels(
                    values = defaultRoomPowerLevelValues(),
                    users = persistentMapOf(A_USER_ID to 100L)
                )
            ),
            innerRoom = FakeFfiRoom(
                suggestedRoleForUserLambda = { userId ->
                    // Simulate the role suggestion based on power level
                    if (userId == A_USER_ID.value) RoomMemberRole.ADMINISTRATOR else RoomMemberRole.USER
                }
            ),
        )
        val result = rustBaseRoom.userRole(A_USER_ID).getOrNull()
        assertThat(result).isNotNull()
        assertThat(result).isEqualTo(RoomMember.Role.Admin)

        rustBaseRoom.destroy()
    }

    private suspend fun TestScope.leaveRoomAndObserveMembershipChange(
        roomMembershipObserver: RoomMembershipObserver,
        rustBaseRoom: RustBaseRoom,
        validate: suspend TurbineTestContext<RoomMembershipObserver.RoomMembershipUpdate>.() -> Unit
    ) {
        val shared = roomMembershipObserver.updates.shareIn(scope = backgroundScope, started = SharingStarted.Eagerly, replay = 1)
        rustBaseRoom.leave()
        shared.test {
            validate()
            ensureAllEventsConsumed()
        }
        rustBaseRoom.destroy()
    }

    private fun TestScope.createRustBaseRoom(
        initialRoomInfo: RoomInfo = aRoomInfo(),
        innerRoom: FakeFfiRoom = FakeFfiRoom(),
        roomMembershipObserver: RoomMembershipObserver = RoomMembershipObserver(),
    ): RustBaseRoom {
        val dispatchers = testCoroutineDispatchers()
        return RustBaseRoom(
            sessionId = A_SESSION_ID,
            deviceId = A_DEVICE_ID,
            innerRoom = innerRoom,
            coroutineDispatchers = dispatchers,
            roomSyncSubscriber = RoomSyncSubscriber(
                roomListService = FakeFfiRoomListService(),
                dispatchers = dispatchers,
            ),
            roomMembershipObserver = roomMembershipObserver,
            // Not using backgroundScope here, but the test scope
            sessionCoroutineScope = this,
            roomInfoMapper = RoomInfoMapper(),
            initialRoomInfo = initialRoomInfo,
        )
    }
}
