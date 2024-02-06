/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.roomactions.impl

import io.element.android.features.roomactions.api.SetRoomIsFavoriteAction
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultSetRoomIsFavoriteActionTests {
    private val room = FakeMatrixRoom()

    @Test
    fun `given a room id and a client without rooms, when action is invoked, then it returns Result_RoomNotFound`() = runTest {
        val action = DefaultSetRoomIsFavoriteAction(FakeMatrixClient())
        val result = action(room.roomId, true)
        assert(result is SetRoomIsFavoriteAction.Result.RoomNotFound)
    }

    @Test
    fun `given a room, when action is invoked, then it returns Result_Success`() = runTest {
        val action = DefaultSetRoomIsFavoriteAction(FakeMatrixClient())
        val result = action(room, true)
        assert(result is SetRoomIsFavoriteAction.Result.Success)
    }

    @Test
    fun `given a room id and a client with a room, when action is invoked, then it returns Result_Success`() = runTest {
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(room.roomId, room)
        }
        val action = DefaultSetRoomIsFavoriteAction(client)
        val result = action(room.roomId, true)
        assert(result is SetRoomIsFavoriteAction.Result.Success)
    }

    @Test
    fun `given a room, when action is invoked and fail, then it returns Result_Exception`() = runTest {
        val action = DefaultSetRoomIsFavoriteAction(FakeMatrixClient())
        room.givenSetIsFavoriteResult(Result.failure(Exception()))
        val result = action(room, true)
        assert(result is SetRoomIsFavoriteAction.Result.Exception)
    }
}
