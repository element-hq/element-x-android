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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.roomlist

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.roomlist.model.RoomListEvents
import io.element.android.features.roomlist.model.RoomListRoomSummary
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.core.SessionId
import io.element.android.libraries.matrixtest.FakeMatrixClient
import io.element.android.libraries.matrixtest.core.A_ROOM_ID
import io.element.android.libraries.matrixtest.core.A_ROOM_ID_VALUE
import io.element.android.libraries.matrixtest.room.A_LAST_MESSAGE
import io.element.android.libraries.matrixtest.room.A_ROOM_NAME
import io.element.android.libraries.matrixtest.room.InMemoryRoomSummaryDataSource
import io.element.android.libraries.matrixtest.room.aRoomSummaryFilled
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomListPresenterTests {

    @Test
    fun `present - should start with no user and then load user with success`() = runTest {
        val presenter = RoomListPresenter(
            FakeMatrixClient(
                SessionId("sessionId")
            ),
            LastMessageFormatter()
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.matrixUser).isNull()
            val withUserState = awaitItem()
            assertThat(withUserState.matrixUser).isNotNull()
        }
    }

    @Test
    fun `present - should filter room with success`() = runTest {
        val presenter = RoomListPresenter(
            FakeMatrixClient(
                SessionId("sessionId")
            ),
            LastMessageFormatter()
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val withUserState = awaitItem()
            assertThat(withUserState.filter).isEqualTo("")
            withUserState.eventSink.invoke(RoomListEvents.UpdateFilter("t"))
            val withFilterState = awaitItem()
            assertThat(withFilterState.filter).isEqualTo("t")
        }
    }

    @Test
    fun `present - load 1 room with success`() = runTest {
        val roomSummaryDataSource = InMemoryRoomSummaryDataSource()
        val presenter = RoomListPresenter(
            FakeMatrixClient(
                sessionId = SessionId("sessionId"),
                roomSummaryDataSource = roomSummaryDataSource
            ),
            LastMessageFormatter()
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val withUserState = awaitItem()
            // Room list is loaded with 16 placeholders
            assertThat(withUserState.roomList.size).isEqualTo(16)
            assertThat(withUserState.roomList.all { it.isPlaceholder }).isTrue()
            roomSummaryDataSource.postRoomSummary(listOf(aRoomSummaryFilled()))
            skipItems(1)
            val withRoomState = awaitItem()
            assertThat(withRoomState.roomList.size).isEqualTo(1)
            assertThat(withRoomState.roomList.first()).isEqualTo(aRoomListRoomSummary)
        }
    }

    @Test
    fun `present - load 1 room with success and filter rooms`() = runTest {
        val roomSummaryDataSource = InMemoryRoomSummaryDataSource()
        val presenter = RoomListPresenter(
            FakeMatrixClient(
                sessionId = SessionId("sessionId"),
                roomSummaryDataSource = roomSummaryDataSource
            ),
            LastMessageFormatter()
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            roomSummaryDataSource.postRoomSummary(listOf(aRoomSummaryFilled()))
            skipItems(3)
            val loadedState = awaitItem()
            // Test filtering with result
            loadedState.eventSink.invoke(RoomListEvents.UpdateFilter(A_ROOM_NAME.substring(0, 3)))
            val withNotFilteredRoomState = awaitItem()
            assertThat(withNotFilteredRoomState.filter).isEqualTo(A_ROOM_NAME.substring(0, 3))
            assertThat(withNotFilteredRoomState.roomList.size).isEqualTo(1)
            assertThat(withNotFilteredRoomState.roomList.first()).isEqualTo(aRoomListRoomSummary)
            // Test filtering without result
            withNotFilteredRoomState.eventSink.invoke(RoomListEvents.UpdateFilter("tada"))
            skipItems(1) // Filter update
            val withFilteredRoomState = awaitItem()
            assertThat(withFilteredRoomState.filter).isEqualTo("tada")
            assertThat(withFilteredRoomState.roomList.size).isEqualTo(0)
        }
    }
}

private val aRoomListRoomSummary = RoomListRoomSummary(
    id = A_ROOM_ID_VALUE,
    roomId = A_ROOM_ID,
    name = A_ROOM_NAME,
    hasUnread = true,
    timestamp = "",
    lastMessage = A_LAST_MESSAGE,
    avatarData = AvatarData(name = A_ROOM_NAME),
    isPlaceholder = false,
)
