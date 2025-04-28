/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.room

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.libraries.matrix.ui.room.LoadingRoomState
import io.element.android.libraries.matrix.ui.room.LoadingRoomStateFlowFactory
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LoadingBaseRoomStateFlowFactoryTest {
    @Test
    fun `flow should emit Loading and then Loaded when there is a room in cache`() = runTest {
        val room = FakeJoinedRoom(baseRoom = FakeBaseRoom(sessionId = A_SESSION_ID, roomId = A_ROOM_ID))
        val matrixClient = FakeMatrixClient(A_SESSION_ID).apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        val flowFactory = LoadingRoomStateFlowFactory(matrixClient)
        flowFactory
            .create(this, A_ROOM_ID)
            .test {
                assertThat(awaitItem()).isEqualTo(LoadingRoomState.Loading)
                assertThat(awaitItem()).isEqualTo(LoadingRoomState.Loaded(room))
            }
    }

    @Test
    fun `flow should emit Loading and then Loaded when there is a room in cache after SS is loaded`() = runTest {
        val room = FakeJoinedRoom(baseRoom = FakeBaseRoom(sessionId = A_SESSION_ID, roomId = A_ROOM_ID))
        val roomListService = FakeRoomListService()
        val matrixClient = FakeMatrixClient(A_SESSION_ID, roomListService = roomListService)
        val flowFactory = LoadingRoomStateFlowFactory(matrixClient)
        flowFactory
            .create(this, A_ROOM_ID)
            .test {
                assertThat(awaitItem()).isEqualTo(LoadingRoomState.Loading)
                matrixClient.givenGetRoomResult(A_ROOM_ID, room)
                roomListService.postAllRoomsLoadingState(RoomList.LoadingState.Loaded(1))
                assertThat(awaitItem()).isEqualTo(LoadingRoomState.Loaded(room))
            }
    }

    @Test
    fun `flow should emit Loading and then Error when there is no room in cache after SS is loaded`() = runTest {
        val roomListService = FakeRoomListService()
        val matrixClient = FakeMatrixClient(A_SESSION_ID, roomListService = roomListService)
        val flowFactory = LoadingRoomStateFlowFactory(matrixClient)
        flowFactory
            .create(this, A_ROOM_ID)
            .test {
                assertThat(awaitItem()).isEqualTo(LoadingRoomState.Loading)
                roomListService.postAllRoomsLoadingState(RoomList.LoadingState.Loaded(1))
                assertThat(awaitItem()).isEqualTo(LoadingRoomState.Error)
            }
    }
}
