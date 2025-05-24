/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeRustRoom
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeRustRoomListService
import io.element.android.libraries.matrix.test.A_DEVICE_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RustBaseRoomTest {
    @Test
    fun `RustBaseRoom should cancel the room coroutine scope when it is destroyed`() = runTest {
        val rustBaseRoom = createRustBaseRoom()
        assertThat(rustBaseRoom.roomCoroutineScope.isActive).isTrue()
        rustBaseRoom.destroy()
        assertThat(rustBaseRoom.roomCoroutineScope.isActive).isFalse()
    }

    private fun TestScope.createRustBaseRoom(): RustBaseRoom {
        val dispatchers = testCoroutineDispatchers()
        return RustBaseRoom(
            sessionId = A_SESSION_ID,
            deviceId = A_DEVICE_ID,
            innerRoom = FakeRustRoom(),
            coroutineDispatchers = dispatchers,
            roomSyncSubscriber = RoomSyncSubscriber(
                roomListService = FakeRustRoomListService(),
                dispatchers = dispatchers,
            ),
            roomMembershipObserver = RoomMembershipObserver(),
            // Not using backgroundScope here, but the test scope
            sessionCoroutineScope = this,
            roomInfoMapper = RoomInfoMapper(),
            initialRoomInfo = aRoomInfo(),
        )
    }
}
