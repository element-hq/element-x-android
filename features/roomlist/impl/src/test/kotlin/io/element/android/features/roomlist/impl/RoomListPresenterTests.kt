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

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth
import io.element.android.features.leaveroom.api.LeaveRoomEvent
import io.element.android.features.leaveroom.api.LeaveRoomPresenter
import io.element.android.features.leaveroom.fake.FakeLeaveRoomPresenter
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.test.FakeNetworkMonitor
import io.element.android.features.roomlist.impl.datasource.FakeInviteDataSource
import io.element.android.features.roomlist.impl.datasource.InviteStateDataSource
import io.element.android.features.roomlist.impl.datasource.RoomListDataSource
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.model.aRoomListRoomSummary
import io.element.android.libraries.dateformatter.api.LastMessageTimestampFormatter
import io.element.android.libraries.dateformatter.test.FakeLastMessageTimestampFormatter
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.eventformatter.api.RoomLastMessageFormatter
import io.element.android.libraries.eventformatter.test.FakeRoomLastMessageFormatter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.room.aRoomSummaryFilled
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class RoomListPresenterTests {

    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - should start with no user and then load user with success`() = runTest {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.matrixUser).isNull()
            val withUserState = awaitItem()
            Truth.assertThat(withUserState.matrixUser).isNotNull()
            Truth.assertThat(withUserState.matrixUser!!.userId).isEqualTo(A_USER_ID)
            Truth.assertThat(withUserState.matrixUser!!.displayName).isEqualTo(A_USER_NAME)
            Truth.assertThat(withUserState.matrixUser!!.avatarUrl).isEqualTo(AN_AVATAR_URL)
            scope.cancel()
        }
    }

    @Test
    fun `present - should start with no user and then load user with error`() = runTest {
        val matrixClient = FakeMatrixClient(
            userDisplayName = Result.failure(AN_EXCEPTION),
            userAvatarURLString = Result.failure(AN_EXCEPTION),
        )
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(client = matrixClient, coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.matrixUser).isNull()
            val withUserState = awaitItem()
            Truth.assertThat(withUserState.matrixUser).isNotNull()
            scope.cancel()
        }
    }

    @Test
    fun `present - should filter room with success`() = runTest {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val withUserState = awaitItem()
            Truth.assertThat(withUserState.filter).isEqualTo("")
            withUserState.eventSink.invoke(RoomListEvents.UpdateFilter("t"))
            val withFilterState = awaitItem()
            Truth.assertThat(withFilterState.filter).isEqualTo("t")
            cancelAndIgnoreRemainingEvents()
            scope.cancel()
        }
    }

    @Test
    fun `present - load 1 room with success`() = runTest {
        val roomListService = FakeRoomListService()
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService
        )
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(client =  matrixClient, coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = consumeItemsUntilPredicate { state -> state.roomList.size == 16 }.last()
            // Room list is loaded with 16 placeholders
            Truth.assertThat(initialState.roomList.size).isEqualTo(16)
            Truth.assertThat(initialState.roomList.all { it.isPlaceholder }).isTrue()
            roomListService.postAllRooms(listOf(aRoomSummaryFilled()))
            val withRoomState = consumeItemsUntilPredicate { state -> state.roomList.size == 1 }.last()
            Truth.assertThat(withRoomState.roomList.size).isEqualTo(1)
            Truth.assertThat(withRoomState.roomList.first())
                .isEqualTo(aRoomListRoomSummary)
            scope.cancel()
        }
    }

    @Test
    fun `present - load 1 room with success and filter rooms`() = runTest {
        val roomListService = FakeRoomListService()
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService
        )
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(client = matrixClient, coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            roomListService.postAllRooms(listOf(aRoomSummaryFilled()))
            val loadedState = consumeItemsUntilPredicate { state -> state.roomList.size == 1 }.last()
            // Test filtering with result
            loadedState.eventSink.invoke(RoomListEvents.UpdateFilter(A_ROOM_NAME.substring(0, 3)))
            val withFilteredRoomState = consumeItemsUntilPredicate { state -> state.filteredRoomList.size == 1 }.last()
            Truth.assertThat(withFilteredRoomState.filter).isEqualTo(A_ROOM_NAME.substring(0, 3))
            Truth.assertThat(withFilteredRoomState.filteredRoomList.size).isEqualTo(1)
            Truth.assertThat(withFilteredRoomState.filteredRoomList.first())
                .isEqualTo(aRoomListRoomSummary)
            // Test filtering without result
            withFilteredRoomState.eventSink.invoke(RoomListEvents.UpdateFilter("tada"))
            val withNotFilteredRoomState = consumeItemsUntilPredicate { state -> state.filteredRoomList.size == 0 }.last()
            Truth.assertThat(withNotFilteredRoomState.filter).isEqualTo("tada")
            Truth.assertThat(withNotFilteredRoomState.filteredRoomList).isEmpty()
            scope.cancel()
        }
    }

    @Test
    fun `present - update visible range`() = runTest {
        val roomListService = FakeRoomListService()
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService
        )
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(client = matrixClient, coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            roomListService.postAllRooms(listOf(aRoomSummaryFilled()))
            val loadedState = awaitItem()
            // check initial value
            Truth.assertThat(roomListService.latestSlidingSyncRange).isNull()
            // Test empty range
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(1, 0)))
            Truth.assertThat(roomListService.latestSlidingSyncRange).isNull()
            // Update visible range and check that range is transmitted to the SDK after computation
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(0, 0)))
            Truth.assertThat(roomListService.latestSlidingSyncRange)
                .isEqualTo(IntRange(0, 20))
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(0, 1)))
            Truth.assertThat(roomListService.latestSlidingSyncRange)
                .isEqualTo(IntRange(0, 21))
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(19, 29)))
            Truth.assertThat(roomListService.latestSlidingSyncRange)
                .isEqualTo(IntRange(0, 49))
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(49, 59)))
            Truth.assertThat(roomListService.latestSlidingSyncRange)
                .isEqualTo(IntRange(29, 79))
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(149, 159)))
            Truth.assertThat(roomListService.latestSlidingSyncRange)
                .isEqualTo(IntRange(129, 179))
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(149, 259)))
            Truth.assertThat(roomListService.latestSlidingSyncRange)
                .isEqualTo(IntRange(129, 279))
            cancelAndIgnoreRemainingEvents()
            scope.cancel()
        }
    }

    @Test
    fun `present - handle DismissRequestVerificationPrompt`() = runTest {
        val roomListService = FakeRoomListService()
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService,
        )
        val scope = CoroutineScope(context = coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(
            client = matrixClient,
            sessionVerificationService = FakeSessionVerificationService().apply {
                givenIsReady(true)
                givenVerifiedStatus(SessionVerifiedStatus.NotVerified)
            },
            coroutineScope = scope,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val eventSink = awaitItem().eventSink
            Truth.assertThat(awaitItem().displayVerificationPrompt).isTrue()

            eventSink(RoomListEvents.DismissRequestVerificationPrompt)
            Truth.assertThat(awaitItem().displayVerificationPrompt).isFalse()
            scope.cancel()
        }
    }

    @Test
    fun `present - sets invite state`() = runTest {
        val inviteStateFlow = MutableStateFlow(InvitesState.NoInvites)
        val inviteStateDataSource = FakeInviteDataSource(inviteStateFlow)
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(inviteStateDataSource = inviteStateDataSource, coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            Truth.assertThat(awaitItem().invitesState).isEqualTo(InvitesState.NoInvites)

            inviteStateFlow.value = InvitesState.SeenInvites
            Truth.assertThat(awaitItem().invitesState).isEqualTo(InvitesState.SeenInvites)

            inviteStateFlow.value = InvitesState.NewInvites
            Truth.assertThat(awaitItem().invitesState).isEqualTo(InvitesState.NewInvites)

            inviteStateFlow.value = InvitesState.NoInvites
            Truth.assertThat(awaitItem().invitesState).isEqualTo(InvitesState.NoInvites)
            scope.cancel()
        }
    }

    @Test
    fun `present - show context menu`() = runTest {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)

            val initialState = awaitItem()
            val summary = aRoomListRoomSummary()
            initialState.eventSink(RoomListEvents.ShowContextMenu(summary))

            val shownState = awaitItem()
            Truth.assertThat(shownState.contextMenu)
                .isEqualTo(RoomListState.ContextMenu.Shown(summary.roomId, summary.name))
            scope.cancel()
        }
    }

    @Test
    fun `present - hide context menu`() = runTest {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)

            val initialState = awaitItem()
            val summary = aRoomListRoomSummary()
            initialState.eventSink(RoomListEvents.ShowContextMenu(summary))

            val shownState = awaitItem()
            Truth.assertThat(shownState.contextMenu)
                .isEqualTo(RoomListState.ContextMenu.Shown(summary.roomId, summary.name))
            shownState.eventSink(RoomListEvents.HideContextMenu)

            val hiddenState = awaitItem()
            Truth.assertThat(hiddenState.contextMenu).isEqualTo(RoomListState.ContextMenu.Hidden)
            scope.cancel()
        }
    }

    @Test
    fun `present - leave room calls into leave room presenter`() = runTest {
        val leaveRoomPresenter = FakeLeaveRoomPresenter()
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(leaveRoomPresenter = leaveRoomPresenter, coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomListEvents.LeaveRoom(A_ROOM_ID))
            Truth.assertThat(leaveRoomPresenter.events).containsExactly(LeaveRoomEvent.ShowConfirmation(A_ROOM_ID))
            cancelAndIgnoreRemainingEvents()
            scope.cancel()
        }
    }

    @Test
    fun `present - change in notification settings updates the summary for decorations`() = runTest {
        val userDefinedMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
        val notificationSettingsService = FakeNotificationSettingsService()
        val roomListService = FakeRoomListService()
        roomListService.postAllRooms(listOf(aRoomSummaryFilled(notificationMode = userDefinedMode)))
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService,
            notificationSettingsService = notificationSettingsService
        )
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(client = matrixClient , coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            notificationSettingsService.setRoomNotificationMode(A_ROOM_ID, userDefinedMode)

            val updatedState = consumeItemsUntilPredicate { state ->
                state.roomList.any { it.id == A_ROOM_ID.value && it.notificationMode == userDefinedMode }
            }.last()

            val room = updatedState.roomList.find { it.id == A_ROOM_ID.value }
            Truth.assertThat(room?.notificationMode).isEqualTo(userDefinedMode)
            cancelAndIgnoreRemainingEvents()
            scope.cancel()
        }
    }

    private fun TestScope.createRoomListPresenter(
        client: MatrixClient = FakeMatrixClient(),
        sessionVerificationService: SessionVerificationService = FakeSessionVerificationService(),
        networkMonitor: NetworkMonitor = FakeNetworkMonitor(),
        snackbarDispatcher: SnackbarDispatcher = SnackbarDispatcher(),
        inviteStateDataSource: InviteStateDataSource = FakeInviteDataSource(),
        leaveRoomPresenter: LeaveRoomPresenter = FakeLeaveRoomPresenter(),
        lastMessageTimestampFormatter: LastMessageTimestampFormatter = FakeLastMessageTimestampFormatter().apply {
            givenFormat(A_FORMATTED_DATE)
        },
        roomLastMessageFormatter: RoomLastMessageFormatter = FakeRoomLastMessageFormatter(),
        coroutineScope: CoroutineScope = this,
    ) = RoomListPresenter(
        client = client,
        sessionVerificationService = sessionVerificationService,
        networkMonitor = networkMonitor,
        snackbarDispatcher = snackbarDispatcher,
        inviteStateDataSource = inviteStateDataSource,
        leaveRoomPresenter = leaveRoomPresenter,
        roomListDataSource = RoomListDataSource(
            client.roomListService,
            lastMessageTimestampFormatter,
            roomLastMessageFormatter,
            coroutineDispatchers = testCoroutineDispatchers(),
            notificationSettingsService = client.notificationSettingsService(),
            appScope = coroutineScope
        )
    )
}

private const val A_FORMATTED_DATE = "formatted_date"

private val aRoomListRoomSummary = RoomListRoomSummary(
    id = A_ROOM_ID.value,
    roomId = A_ROOM_ID,
    name = A_ROOM_NAME,
    hasUnread = true,
    timestamp = A_FORMATTED_DATE,
    lastMessage = "",
    avatarData = AvatarData(id = A_ROOM_ID.value, name = A_ROOM_NAME, size = AvatarSize.RoomListItem),
    isPlaceholder = false,
)
