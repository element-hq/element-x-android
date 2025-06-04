/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.roommembermoderation.api.RoomMemberModerationState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomMembersState
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
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
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                updateMembersResult = { Result.success(Unit) },
                canInviteResult = { Result.success(true) }
            ).apply {
                // Needed to avoid discarding the loaded members as a partial and invalid result
                givenRoomInfo(aRoomInfo(joinedMembersCount = 2))
            }
        )
        val presenter = createPresenter(joinedRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.roomMembers.isLoading()).isTrue()
            assertThat(initialState.searchQuery).isEmpty()
            assertThat(initialState.searchResults).isInstanceOf(SearchBarResultState.Initial::class.java)
            assertThat(initialState.isSearchActive).isFalse()
            room.givenRoomMembersState(RoomMembersState.Ready(aRoomMemberList()))
            // Skip item while the new members state is processed
            skipItems(1)
            val loadedMembersState = awaitItem()
            assertThat(loadedMembersState.roomMembers.isLoading()).isFalse()
            assertThat(loadedMembersState.roomMembers.dataOrNull()?.invited)
                .isEqualTo(listOf(RoomMemberWithIdentityState(aVictor(), null), RoomMemberWithIdentityState(aWalter(), null)))
            assertThat(loadedMembersState.roomMembers.dataOrNull()?.joined).isNotEmpty()
        }
    }

    @Test
    fun `open search`() = runTest {
        val presenter = createPresenter(
            joinedRoom = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(
                    updateMembersResult = { Result.success(Unit) },
                    canInviteResult = { Result.success(true) }
                )
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
            joinedRoom = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(
                    updateMembersResult = { Result.success(Unit) },
                    canInviteResult = { Result.success(true) }
                )
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
            joinedRoom = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(
                    updateMembersResult = { Result.success(Unit) },
                    canInviteResult = { Result.success(true) }
                )
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
            assertThat((searchSearchResultDelivered.searchResults as SearchBarResultState.Results).results.dataOrNull()!!.joined.first().roomMember.displayName)
                .isEqualTo("Alice")
        }
    }

    @Test
    fun `present - asynchronously sets canInvite when user has correct power level`() = runTest {
        val presenter = createPresenter(
            joinedRoom = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(
                    canInviteResult = { Result.success(true) },
                    updateMembersResult = { Result.success(Unit) }
                )
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
            joinedRoom = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(
                    canInviteResult = { Result.success(false) },
                    updateMembersResult = { Result.success(Unit) }
                )
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
            joinedRoom = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(
                    canInviteResult = { Result.failure(RuntimeException("Eek")) },
                    updateMembersResult = { Result.success(Unit) }
                )
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
    fun `present - RoomMemberSelected will open the moderation options when target user is not banned`() = runTest {
        val roomMemberModerationPresenter = Presenter {
            aRoomMemberModerationState(canBan = true, canKick = true)
        }
        val presenter = createPresenter(
            roomMemberModerationPresenter = roomMemberModerationPresenter,
            joinedRoom = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(
                    updateMembersResult = { Result.success(Unit) },
                    canInviteResult = { Result.success(true) }
                )
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            awaitItem().eventSink(RoomMemberListEvents.RoomMemberSelected(aVictor()))
        }
    }
}

@ExperimentalCoroutinesApi
private fun TestScope.createDataSource(
    room: BaseRoom = FakeBaseRoom().apply {
        givenRoomMembersState(RoomMembersState.Ready(aRoomMemberList()))
    },
    coroutineDispatchers: CoroutineDispatchers = testCoroutineDispatchers()
) = RoomMemberListDataSource(room, coroutineDispatchers)

@ExperimentalCoroutinesApi
private fun TestScope.createPresenter(
    coroutineDispatchers: CoroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true),
    joinedRoom: JoinedRoom = FakeJoinedRoom(
        baseRoom = FakeBaseRoom(
            updateMembersResult = { Result.success(Unit) }
        )
    ),
    roomMemberListDataSource: RoomMemberListDataSource = createDataSource(coroutineDispatchers = coroutineDispatchers),
    encryptedService: FakeEncryptionService = FakeEncryptionService(),
    roomMemberModerationPresenter: Presenter<RoomMemberModerationState> = Presenter {
        aRoomMemberModerationState()
    },
) = RoomMemberListPresenter(
    room = joinedRoom,
    roomMemberListDataSource = roomMemberListDataSource,
    coroutineDispatchers = coroutineDispatchers,
    roomMembersModerationPresenter = roomMemberModerationPresenter,
    encryptionService = encryptedService,
)
