/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.appnav.room

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LoadingRoomStateFlowFactoryTest {
    @Test
    fun `flow should emit Loading and then Loaded when there is a room in cache`() = runTest {
        val room = FakeMatrixRoom(sessionId = A_SESSION_ID, roomId = A_ROOM_ID)
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
        val room = FakeMatrixRoom(sessionId = A_SESSION_ID, roomId = A_ROOM_ID)
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
