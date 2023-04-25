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

package io.element.android.features.roomlist.impl

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth
import io.element.android.features.networkmonitor.test.FakeNetworkMonitor
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.libraries.dateformatter.api.LastMessageTimestampFormatter
import io.element.android.libraries.dateformatter.test.FakeLastMessageTimestampFormatter
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.utils.SnackbarDispatcher
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeRoomSummaryDataSource
import io.element.android.libraries.matrix.test.room.aRoomSummaryFilled
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomListPresenterTests {

    @Test
    fun `present - should start with no user and then load user with success`() = runTest {
        val presenter = RoomListPresenter(
            FakeMatrixClient(A_SESSION_ID),
            createDateFormatter(),
            FakeRoomLastMessageFormatter(),
            FakeSessionVerificationService(),
            FakeNetworkMonitor(),
            SnackbarDispatcher(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.matrixUser).isNull()
            val withUserState = awaitItem()
            Truth.assertThat(withUserState.matrixUser).isNotNull()
            Truth.assertThat(withUserState.matrixUser!!.id).isEqualTo(A_USER_ID)
            Truth.assertThat(withUserState.matrixUser!!.username).isEqualTo(A_USER_NAME)
            Truth.assertThat(withUserState.matrixUser!!.avatarData.name).isEqualTo(A_USER_NAME)
            Truth.assertThat(withUserState.matrixUser!!.avatarData.url).isEqualTo(AN_AVATAR_URL)
        }
    }

    @Test
    fun `present - should start with no user and then load user with error`() = runTest {
        val presenter = RoomListPresenter(
            FakeMatrixClient(
                A_SESSION_ID,
                userDisplayName = Result.failure(AN_EXCEPTION),
                userAvatarURLString = Result.failure(AN_EXCEPTION),
            ),
            createDateFormatter(),
            FakeRoomLastMessageFormatter(),
            FakeSessionVerificationService(),
            FakeNetworkMonitor(),
            SnackbarDispatcher(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.matrixUser).isNull()
            val withUserState = awaitItem()
            Truth.assertThat(withUserState.matrixUser).isNotNull()
            // username fallback to user id value
            Truth.assertThat(withUserState.matrixUser!!.username).isEqualTo(A_USER_ID.value)
        }
    }

    @Test
    fun `present - should filter room with success`() = runTest {
        val presenter = RoomListPresenter(
            FakeMatrixClient(A_SESSION_ID),
            createDateFormatter(),
            FakeRoomLastMessageFormatter(),
            FakeSessionVerificationService(),
            FakeNetworkMonitor(),
            SnackbarDispatcher(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val withUserState = awaitItem()
            Truth.assertThat(withUserState.filter).isEqualTo("")
            withUserState.eventSink.invoke(RoomListEvents.UpdateFilter("t"))
            val withFilterState = awaitItem()
            Truth.assertThat(withFilterState.filter).isEqualTo("t")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - load 1 room with success`() = runTest {
        val roomSummaryDataSource = FakeRoomSummaryDataSource()
        val presenter = RoomListPresenter(
            FakeMatrixClient(
                sessionId = A_SESSION_ID,
                roomSummaryDataSource = roomSummaryDataSource
            ),
            createDateFormatter(),
            FakeRoomLastMessageFormatter(),
            FakeSessionVerificationService(),
            FakeNetworkMonitor(),
            SnackbarDispatcher(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val withUserState = awaitItem()
            // Room list is loaded with 16 placeholders
            Truth.assertThat(withUserState.roomList.size).isEqualTo(16)
            Truth.assertThat(withUserState.roomList.all { it.isPlaceholder }).isTrue()
            roomSummaryDataSource.postRoomSummary(listOf(aRoomSummaryFilled()))
            skipItems(1)
            val withRoomState = awaitItem()
            Truth.assertThat(withRoomState.roomList.size).isEqualTo(1)
            Truth.assertThat(withRoomState.roomList.first())
                .isEqualTo(aRoomListRoomSummary)
        }
    }

    @Test
    fun `present - load 1 room with success and filter rooms`() = runTest {
        val roomSummaryDataSource = FakeRoomSummaryDataSource()
        val presenter = RoomListPresenter(
            FakeMatrixClient(
                sessionId = A_SESSION_ID,
                roomSummaryDataSource = roomSummaryDataSource
            ),
            createDateFormatter(),
            FakeRoomLastMessageFormatter(),
            FakeSessionVerificationService(),
            FakeNetworkMonitor(),
            SnackbarDispatcher(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            roomSummaryDataSource.postRoomSummary(listOf(aRoomSummaryFilled()))
            skipItems(3)
            val loadedState = awaitItem()
            // Test filtering with result
            loadedState.eventSink.invoke(RoomListEvents.UpdateFilter(A_ROOM_NAME.substring(0, 3)))
            skipItems(1) // Filter update
            val withNotFilteredRoomState = awaitItem()
            Truth.assertThat(withNotFilteredRoomState.filter).isEqualTo(A_ROOM_NAME.substring(0, 3))
            Truth.assertThat(withNotFilteredRoomState.filteredRoomList.size).isEqualTo(1)
            Truth.assertThat(withNotFilteredRoomState.filteredRoomList.first())
                .isEqualTo(aRoomListRoomSummary)
            // Test filtering without result
            withNotFilteredRoomState.eventSink.invoke(RoomListEvents.UpdateFilter("tada"))
            skipItems(1) // Filter update
            val withFilteredRoomState = awaitItem()
            Truth.assertThat(withFilteredRoomState.filter).isEqualTo("tada")
            Truth.assertThat(withFilteredRoomState.filteredRoomList).isEmpty()
        }
    }

    @Test
    fun `present - update visible range`() = runTest {
        val roomSummaryDataSource = FakeRoomSummaryDataSource()
        val presenter = RoomListPresenter(
            FakeMatrixClient(
                sessionId = A_SESSION_ID,
                roomSummaryDataSource = roomSummaryDataSource
            ),
            createDateFormatter(),
            FakeRoomLastMessageFormatter(),
            FakeSessionVerificationService(),
            FakeNetworkMonitor(),
            SnackbarDispatcher(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            roomSummaryDataSource.postRoomSummary(listOf(aRoomSummaryFilled()))
            skipItems(3)
            val loadedState = awaitItem()
            // check initial value
            Truth.assertThat(roomSummaryDataSource.latestSlidingSyncRange).isNull()
            // Test empty range
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(1, 0)))
            Truth.assertThat(roomSummaryDataSource.latestSlidingSyncRange).isNull()
            // Update visible range and check that range is transmitted to the SDK after computation
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(0, 0)))
            Truth.assertThat(roomSummaryDataSource.latestSlidingSyncRange)
                .isEqualTo(IntRange(0, 20))
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(0, 1)))
            Truth.assertThat(roomSummaryDataSource.latestSlidingSyncRange)
                .isEqualTo(IntRange(0, 21))
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(19, 29)))
            Truth.assertThat(roomSummaryDataSource.latestSlidingSyncRange)
                .isEqualTo(IntRange(0, 49))
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(49, 59)))
            Truth.assertThat(roomSummaryDataSource.latestSlidingSyncRange)
                .isEqualTo(IntRange(29, 79))
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(149, 159)))
            Truth.assertThat(roomSummaryDataSource.latestSlidingSyncRange)
                .isEqualTo(IntRange(129, 179))
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(149, 259)))
            Truth.assertThat(roomSummaryDataSource.latestSlidingSyncRange)
                .isEqualTo(IntRange(129, 279))
        }
    }

    @Test
    fun `present - handle DismissRequestVerificationPrompt`() = runTest {
        val roomSummaryDataSource = FakeRoomSummaryDataSource()
        val presenter = RoomListPresenter(
            FakeMatrixClient(
                sessionId = A_SESSION_ID,
                roomSummaryDataSource = roomSummaryDataSource
            ),
            createDateFormatter(),
            FakeRoomLastMessageFormatter(),
            FakeSessionVerificationService().apply {
                givenIsReady(true)
                givenVerifiedStatus(SessionVerifiedStatus.NotVerified)
            },
            FakeNetworkMonitor(),
            SnackbarDispatcher(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val eventSink = awaitItem().eventSink
            Truth.assertThat(awaitItem().displayVerificationPrompt).isTrue()

            eventSink(RoomListEvents.DismissRequestVerificationPrompt)
            Truth.assertThat(awaitItem().displayVerificationPrompt).isFalse()
        }
    }

    @Test
    fun `present - displays invites row if any invites exist`() = runTest {
        val invitesDataSource = FakeRoomSummaryDataSource()
        val presenter = RoomListPresenter(
            FakeMatrixClient(
                sessionId = A_SESSION_ID,
                invitesDataSource = invitesDataSource
            ),
            createDateFormatter(),
            FakeRoomLastMessageFormatter(),
            FakeSessionVerificationService(),
            FakeNetworkMonitor(),
            SnackbarDispatcher(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)

            Truth.assertThat(awaitItem().displayInvites).isFalse()

            invitesDataSource.postRoomSummary(listOf(aRoomSummaryFilled()))
            Truth.assertThat(awaitItem().displayInvites).isTrue()

            invitesDataSource.postRoomSummary(listOf())
            Truth.assertThat(awaitItem().displayInvites).isFalse()
        }
    }

    private fun createDateFormatter(): LastMessageTimestampFormatter {
        return FakeLastMessageTimestampFormatter().apply {
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
    lastMessage = "",
    avatarData = AvatarData(id = A_ROOM_ID.value, name = A_ROOM_NAME),
    isPlaceholder = false,
)
