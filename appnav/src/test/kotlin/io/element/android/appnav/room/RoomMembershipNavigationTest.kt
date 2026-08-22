/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.room

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.room.aRoomInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Optional

@OptIn(ExperimentalCoroutinesApi::class)
class RoomMembershipNavigationTest {
    @Test
    fun `a joined room enters the room`() = runTest {
        val roomInfo = MutableStateFlow(anOptionalRoomInfo(CurrentUserMembership.JOINED))
        navigationFlow(roomInfo).test {
            assertThat(awaitItem()).isEqualTo(RoomMembershipNavigation.EnterRoom)
        }
    }

    @Test
    fun `an invited room shows the join room screen`() = runTest {
        val roomInfo = MutableStateFlow(anOptionalRoomInfo(CurrentUserMembership.INVITED))
        navigationFlow(roomInfo).test {
            assertThat(awaitItem()).isEqualTo(RoomMembershipNavigation.ShowJoinRoom)
        }
    }

    @Test
    fun `leaving from this device dismisses the flow when the local leave arrives first`() = runTest {
        val roomInfo = MutableStateFlow(anOptionalRoomInfo(CurrentUserMembership.JOINED))
        val membershipUpdates = MutableSharedFlow<RoomMembershipObserver.RoomMembershipUpdate>(extraBufferCapacity = 10)
        navigationFlow(roomInfo, membershipUpdates).test {
            assertThat(awaitItem()).isEqualTo(RoomMembershipNavigation.EnterRoom)
            runCurrent()
            membershipUpdates.emit(aLeftUpdate())
            runCurrent()
            roomInfo.value = anOptionalRoomInfo(CurrentUserMembership.LEFT)
            runCurrent()
            assertThat(expectMostRecentItem()).isEqualTo(RoomMembershipNavigation.Dismiss)
        }
    }

    @Test
    fun `leaving from this device dismisses the flow when the room info arrives first`() = runTest {
        val roomInfo = MutableStateFlow(anOptionalRoomInfo(CurrentUserMembership.JOINED))
        val membershipUpdates = MutableSharedFlow<RoomMembershipObserver.RoomMembershipUpdate>(extraBufferCapacity = 10)
        navigationFlow(roomInfo, membershipUpdates).test {
            assertThat(awaitItem()).isEqualTo(RoomMembershipNavigation.EnterRoom)
            runCurrent()
            roomInfo.value = anOptionalRoomInfo(CurrentUserMembership.LEFT)
            assertThat(awaitItem()).isEqualTo(RoomMembershipNavigation.ShowJoinRoom)
            membershipUpdates.emit(aLeftUpdate())
            runCurrent()
            assertThat(expectMostRecentItem()).isEqualTo(RoomMembershipNavigation.Dismiss)
        }
    }

    @Test
    fun `leaving from another device shows the join room screen`() = runTest {
        val roomInfo = MutableStateFlow(anOptionalRoomInfo(CurrentUserMembership.JOINED))
        navigationFlow(roomInfo).test {
            assertThat(awaitItem()).isEqualTo(RoomMembershipNavigation.EnterRoom)
            roomInfo.value = anOptionalRoomInfo(CurrentUserMembership.LEFT)
            runCurrent()
            assertThat(expectMostRecentItem()).isEqualTo(RoomMembershipNavigation.ShowJoinRoom)
        }
    }

    @Test
    fun `a local leave for another room is ignored`() = runTest {
        val roomInfo = MutableStateFlow(anOptionalRoomInfo(CurrentUserMembership.JOINED))
        val membershipUpdates = MutableSharedFlow<RoomMembershipObserver.RoomMembershipUpdate>(extraBufferCapacity = 10)
        navigationFlow(roomInfo, membershipUpdates).test {
            assertThat(awaitItem()).isEqualTo(RoomMembershipNavigation.EnterRoom)
            runCurrent()
            membershipUpdates.emit(aLeftUpdate(roomId = A_ROOM_ID_2))
            runCurrent()
            roomInfo.value = anOptionalRoomInfo(CurrentUserMembership.LEFT)
            runCurrent()
            assertThat(expectMostRecentItem()).isEqualTo(RoomMembershipNavigation.ShowJoinRoom)
        }
    }

    private fun TestScope.navigationFlow(
        roomInfo: MutableStateFlow<Optional<RoomInfo>>,
        membershipUpdates: MutableSharedFlow<RoomMembershipObserver.RoomMembershipUpdate> = MutableSharedFlow(extraBufferCapacity = 10),
    ) = roomMembershipNavigation(
        roomId = A_ROOM_ID,
        roomInfoFlow = roomInfo,
        membershipUpdates = membershipUpdates,
        scope = backgroundScope,
    )

    private fun anOptionalRoomInfo(membership: CurrentUserMembership) = Optional.of(aRoomInfo(currentUserMembership = membership))

    private fun aLeftUpdate(roomId: RoomId = A_ROOM_ID) = RoomMembershipObserver.RoomMembershipUpdate(
        roomId = roomId,
        isSpace = false,
        isUserInRoom = false,
        change = MembershipChange.LEFT,
    )
}
