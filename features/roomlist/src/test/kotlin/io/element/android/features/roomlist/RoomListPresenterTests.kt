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
import io.element.android.libraries.dateformatter.LastMessageFormatter
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.core.SessionId
import io.element.android.libraries.matrixtest.AN_AVATAR_URL
import io.element.android.libraries.matrixtest.AN_EXCEPTION
import io.element.android.libraries.matrixtest.A_MESSAGE
import io.element.android.libraries.matrixtest.A_ROOM_ID
import io.element.android.libraries.matrixtest.A_ROOM_NAME
import io.element.android.libraries.matrixtest.A_USER_ID
import io.element.android.libraries.matrixtest.A_USER_NAME
import io.element.android.libraries.matrixtest.FakeMatrixClient
import io.element.android.libraries.matrixtest.room.FakeRoomSummaryDataSource
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
            createDateFormatter()
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.matrixUser).isNull()
            val withUserState = awaitItem()
            assertThat(withUserState.matrixUser).isNotNull()
            assertThat(withUserState.matrixUser!!.id).isEqualTo(A_USER_ID)
            assertThat(withUserState.matrixUser!!.username).isEqualTo(A_USER_NAME)
            assertThat(withUserState.matrixUser!!.avatarData.name).isEqualTo(A_USER_NAME)
            assertThat(withUserState.matrixUser!!.avatarData.url).isEqualTo(AN_AVATAR_URL)
        }
    }

    @Test
    fun `present - should start with no user and then load user with error`() = runTest {
        val presenter = RoomListPresenter(
            FakeMatrixClient(
                SessionId("sessionId"),
                userDisplayName = Result.failure(AN_EXCEPTION),
                userAvatarURLString = Result.failure(AN_EXCEPTION),
            ),
            createDateFormatter()
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.matrixUser).isNull()
            val withUserState = awaitItem()
            assertThat(withUserState.matrixUser).isNotNull()
            // username fallback to user id value
            assertThat(withUserState.matrixUser!!.username).isEqualTo(A_USER_ID.value)
        }
    }

    @Test
    fun `present - should filter room with success`() = runTest {
        val presenter = RoomListPresenter(
            FakeMatrixClient(
                SessionId("sessionId")
            ),
            createDateFormatter()
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
        val roomSummaryDataSource = FakeRoomSummaryDataSource()
        val presenter = RoomListPresenter(
            FakeMatrixClient(
                sessionId = SessionId("sessionId"),
                roomSummaryDataSource = roomSummaryDataSource
            ),
            createDateFormatter()
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
        val roomSummaryDataSource = FakeRoomSummaryDataSource()
        val presenter = RoomListPresenter(
            FakeMatrixClient(
                sessionId = SessionId("sessionId"),
                roomSummaryDataSource = roomSummaryDataSource
            ),
            createDateFormatter()
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
            assertThat(withFilteredRoomState.roomList).isEmpty()
        }
    }

    @Test
    fun `present - update visible range`() = runTest {
        val roomSummaryDataSource = FakeRoomSummaryDataSource()
        val presenter = RoomListPresenter(
            FakeMatrixClient(
                sessionId = SessionId("sessionId"),
                roomSummaryDataSource = roomSummaryDataSource
            ),
            createDateFormatter()
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            roomSummaryDataSource.postRoomSummary(listOf(aRoomSummaryFilled()))
            skipItems(3)
            val loadedState = awaitItem()
            // check initial value
            assertThat(roomSummaryDataSource.latestSlidingSyncRange).isNull()
            // Test empty range
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(1, 0)))
            assertThat(roomSummaryDataSource.latestSlidingSyncRange).isNull()
            // Update visible range and check that range is transmitted to the SDK after computation
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(0, 0)))
            assertThat(roomSummaryDataSource.latestSlidingSyncRange).isEqualTo(IntRange(0, 20))
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(0, 1)))
            assertThat(roomSummaryDataSource.latestSlidingSyncRange).isEqualTo(IntRange(0, 21))
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(19, 29)))
            assertThat(roomSummaryDataSource.latestSlidingSyncRange).isEqualTo(IntRange(0, 49))
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(49, 59)))
            assertThat(roomSummaryDataSource.latestSlidingSyncRange).isEqualTo(IntRange(29, 79))
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(149, 159)))
            assertThat(roomSummaryDataSource.latestSlidingSyncRange).isEqualTo(IntRange(129, 179))
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(149, 259)))
            assertThat(roomSummaryDataSource.latestSlidingSyncRange).isEqualTo(IntRange(129, 279))
        }
    }

    private fun createDateFormatter(): LastMessageFormatter {
        return FakeLastMessageFormatter().apply {
            givenFormat(A_FORMATTED_DATE)
        }
    }
}

private const val A_FORMATTED_DATE = "formatted_date"

private val aRoomListRoomSummary = RoomListRoomSummary(
    id = A_ROOM_ID.value,
    roomId = A_ROOM_ID,
    name = A_ROOM_NAME,
    hasUnread = true,
    timestamp = A_FORMATTED_DATE,
    lastMessage = A_MESSAGE,
    avatarData = AvatarData(id = A_ROOM_ID.value, name = A_ROOM_NAME),
    isPlaceholder = false,
)
