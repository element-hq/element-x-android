/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.members

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.roomdetails.impl.members.RoomMemberListDataSource
import io.element.android.features.roomdetails.impl.members.RoomMemberListEvents
import io.element.android.features.roomdetails.impl.members.RoomMemberListNavigator
import io.element.android.features.roomdetails.impl.members.RoomMemberListPresenter
import io.element.android.features.roomdetails.impl.members.aRoomMemberList
import io.element.android.features.roomdetails.impl.members.aVictor
import io.element.android.features.roomdetails.impl.members.aWalter
import io.element.android.features.roomdetails.impl.members.moderation.RoomMembersModerationEvents
import io.element.android.features.roomdetails.impl.members.moderation.RoomMembersModerationState
import io.element.android.features.roomdetails.impl.members.moderation.aRoomMembersModerationState
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class RoomMemberListPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `member loading is done automatically on start, but is async`() = runTest {
        val room = FakeMatrixRoom(
            updateMembersResult = { Result.success(Unit) },
            canInviteResult = { Result.success(true) }
        ).apply {
            // Needed to avoid discarding the loaded members as a partial and invalid result
            givenRoomInfo(aRoomInfo(joinedMembersCount = 2))
        }
        val presenter = createPresenter(matrixRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.roomMembers.isLoading()).isTrue()
            assertThat(initialState.searchQuery).isEmpty()
            assertThat(initialState.searchResults).isInstanceOf(SearchBarResultState.Initial::class.java)
            assertThat(initialState.isSearchActive).isFalse()
            room.givenRoomMembersState(MatrixRoomMembersState.Ready(aRoomMemberList()))
            // Skip item while the new members state is processed
            skipItems(1)
            val loadedMembersState = awaitItem()
            assertThat(loadedMembersState.roomMembers.isLoading()).isFalse()
            assertThat(loadedMembersState.roomMembers.dataOrNull()?.invited).isEqualTo(listOf(aVictor(), aWalter()))
            assertThat(loadedMembersState.roomMembers.dataOrNull()?.joined).isNotEmpty()
        }
    }

    @Test
    fun `open search`() = runTest {
        val presenter = createPresenter(
            matrixRoom = FakeMatrixRoom(
                updateMembersResult = { Result.success(Unit) },
                canInviteResult = { Result.success(true) }
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val loadedState = awaitItem()
            loadedState.eventSink(RoomMemberListEvents.OnSearchActiveChanged(true))
            skipItems(1)
            val searchActiveState = awaitItem()
            assertThat(searchActiveState.isSearchActive).isTrue()
        }
    }

    @Test
    fun `search for something which is not found`() = runTest {
        val presenter = createPresenter(
            matrixRoom = FakeMatrixRoom(
                updateMembersResult = { Result.success(Unit) },
                canInviteResult = { Result.success(true) }
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val loadedState = awaitItem()
            loadedState.eventSink(RoomMemberListEvents.OnSearchActiveChanged(true))
            val searchActiveState = awaitItem()
            searchActiveState.eventSink(RoomMemberListEvents.UpdateSearchQuery("something"))
            skipItems(1)
            val searchQueryUpdatedState = awaitItem()
            assertThat(searchQueryUpdatedState.searchQuery).isEqualTo("something")
            val searchSearchResultDelivered = awaitItem()
            assertThat(searchSearchResultDelivered.searchResults).isInstanceOf(SearchBarResultState.NoResultsFound::class.java)
        }
    }

    @Test
    fun `search for something which is found`() = runTest {
        val presenter = createPresenter(
            matrixRoom = FakeMatrixRoom(
                updateMembersResult = { Result.success(Unit) },
                canInviteResult = { Result.success(true) }
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val loadedState = awaitItem()
            loadedState.eventSink(RoomMemberListEvents.OnSearchActiveChanged(true))
            val searchActiveState = awaitItem()
            searchActiveState.eventSink(RoomMemberListEvents.UpdateSearchQuery("Alice"))
            skipItems(1)
            val searchQueryUpdatedState = awaitItem()
            assertThat(searchQueryUpdatedState.searchQuery).isEqualTo("Alice")
            val searchSearchResultDelivered = awaitItem()
            assertThat(searchSearchResultDelivered.searchResults).isInstanceOf(SearchBarResultState.Results::class.java)
            assertThat((searchSearchResultDelivered.searchResults as SearchBarResultState.Results).results.dataOrNull()!!.joined.first().displayName)
                .isEqualTo("Alice")
        }
    }

    @Test
    fun `present - asynchronously sets canInvite when user has correct power level`() = runTest {
        val presenter = createPresenter(
            matrixRoom = FakeMatrixRoom(
                canInviteResult = { Result.success(true) },
                updateMembersResult = { Result.success(Unit) }
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val loadedState = awaitItem()
            assertThat(loadedState.canInvite).isTrue()
        }
    }

    @Test
    fun `present - asynchronously sets canInvite when user does not have correct power level`() = runTest {
        val presenter = createPresenter(
            matrixRoom = FakeMatrixRoom(
                canInviteResult = { Result.success(false) },
                updateMembersResult = { Result.success(Unit) }
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val loadedState = awaitItem()
            assertThat(loadedState.canInvite).isFalse()
        }
    }

    @Test
    fun `present - asynchronously sets canInvite when power level check fails`() = runTest {
        val presenter = createPresenter(
            matrixRoom = FakeMatrixRoom(
                canInviteResult = { Result.failure(Throwable("Eek")) },
                updateMembersResult = { Result.success(Unit) }
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val loadedState = awaitItem()
            assertThat(loadedState.canInvite).isFalse()
        }
    }

    @Test
    fun `present - RoomMemberSelected by default opens the room member details through the navigator`() = runTest {
        val navigator = FakeRoomMemberListNavigator()
        val roomMembersModerationStateLambda = { aRoomMembersModerationState(canDisplayModerationActions = false) }
        val presenter = createPresenter(
            roomMembersModerationStateLambda = roomMembersModerationStateLambda,
            navigator = navigator,
            matrixRoom = FakeMatrixRoom(
                updateMembersResult = { Result.success(Unit) },
                canInviteResult = { Result.success(true) }
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            awaitItem().eventSink(RoomMemberListEvents.RoomMemberSelected(aVictor()))
            assertThat(navigator.openRoomMemberDetailsCallCount).isEqualTo(1)
        }
    }

    @Test
    fun `present - RoomMemberSelected will open the moderation options if the current user can use them`() = runTest {
        val navigator = FakeRoomMemberListNavigator()
        val eventsRecorder = EventsRecorder<RoomMembersModerationEvents>()
        val roomMembersModerationStateLambda = {
            aRoomMembersModerationState(
                canDisplayModerationActions = true,
                eventSink = eventsRecorder,
            )
        }
        val presenter = createPresenter(
            roomMembersModerationStateLambda = roomMembersModerationStateLambda,
            navigator = navigator,
            matrixRoom = FakeMatrixRoom(
                updateMembersResult = { Result.success(Unit) },
                canInviteResult = { Result.success(true) }
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            awaitItem().eventSink(RoomMemberListEvents.RoomMemberSelected(aVictor()))
            eventsRecorder.assertSingle(RoomMembersModerationEvents.SelectRoomMember(aVictor()))
        }
    }
}

private class FakeRoomMemberListNavigator : RoomMemberListNavigator {
    var openRoomMemberDetailsCallCount = 0
        private set

    override fun openRoomMemberDetails(roomMemberId: UserId) {
        openRoomMemberDetailsCallCount++
    }
}

@ExperimentalCoroutinesApi
private fun TestScope.createDataSource(
    matrixRoom: MatrixRoom = FakeMatrixRoom().apply {
        givenRoomMembersState(MatrixRoomMembersState.Ready(aRoomMemberList()))
    },
    coroutineDispatchers: CoroutineDispatchers = testCoroutineDispatchers()
) = RoomMemberListDataSource(matrixRoom, coroutineDispatchers)

@ExperimentalCoroutinesApi
private fun TestScope.createPresenter(
    coroutineDispatchers: CoroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true),
    matrixRoom: MatrixRoom = FakeMatrixRoom(
        updateMembersResult = { Result.success(Unit) }
    ),
    roomMemberListDataSource: RoomMemberListDataSource = createDataSource(coroutineDispatchers = coroutineDispatchers),
    roomMembersModerationStateLambda: () -> RoomMembersModerationState = { aRoomMembersModerationState() },
    navigator: RoomMemberListNavigator = object : RoomMemberListNavigator {}
) = RoomMemberListPresenter(
    room = matrixRoom,
    roomMemberListDataSource = roomMemberListDataSource,
    coroutineDispatchers = coroutineDispatchers,
    roomMembersModerationPresenter = { roomMembersModerationStateLambda() },
    navigator = navigator
)
