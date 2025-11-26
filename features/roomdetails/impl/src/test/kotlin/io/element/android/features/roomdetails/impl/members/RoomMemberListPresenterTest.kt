/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members

import com.google.common.truth.Truth.assertThat
import io.element.android.features.roommembermoderation.api.RoomMemberModerationState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.collections.immutable.toImmutableList
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
    fun `initial state is loading`() = runTest {
        val presenter = createPresenter()
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.filteredRoomMembers.isLoading()).isTrue()
            assertThat(initialState.searchQuery).isEmpty()
            assertThat(initialState.selectedSection).isEqualTo(SelectedSection.MEMBERS)
        }
    }

    @Test
    fun `hide banned section when there is no banned users`() = runTest {
        val allRoomMembers = aRoomMemberList()
        val noBannedMembers = allRoomMembers
            .filterNot { it.membership == RoomMembershipState.BAN }
            .toImmutableList()
        val room = createFakeJoinedRoom()
            .apply {
                givenRoomMembersState(RoomMembersState.Ready(allRoomMembers))
            }
        val presenter = createPresenter(
            joinedRoom = room,
            roomMemberModerationState = aRoomMemberModerationState(canBan = true),
        )
        presenter.test {
            skipItems(1)
            val loadedState = awaitItem()
            assertThat(loadedState.showBannedSection).isTrue()
            loadedState.eventSink(RoomMemberListEvents.ChangeSelectedSection(SelectedSection.BANNED))
            val bannedSectionState = awaitItem()
            assertThat(bannedSectionState.selectedSection).isEqualTo(SelectedSection.BANNED)
            // Now update the room members to have no banned users
            room.givenRoomMembersState(RoomMembersState.Ready(noBannedMembers))
            skipItems(1)
            val noBannedMembersState = awaitItem()
            assertThat(noBannedMembersState.showBannedSection).isFalse()
            skipItems(1)
            val finalState = awaitItem()
            assertThat(finalState.selectedSection).isEqualTo(SelectedSection.MEMBERS)
        }
    }

    @Test
    fun `member loading is done automatically on start, but is async`() = runTest {
        val room = createFakeJoinedRoom()
        val presenter = createPresenter(joinedRoom = room)
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.filteredRoomMembers.isLoading()).isTrue()
            assertThat(initialState.searchQuery).isEmpty()
            room.givenRoomMembersState(RoomMembersState.Ready(aRoomMemberList()))
            // Skip items while the new members state is processed
            skipItems(2)
            val loadedState = awaitItem()
            val loadedRoomMembers = loadedState.filteredRoomMembers.dataOrNull()!!
            assertThat(loadedRoomMembers.joined).isNotEmpty()
            assertThat(loadedRoomMembers.banned).isNotEmpty()
            assertThat(loadedRoomMembers.invited).isNotEmpty()
            assertThat(loadedRoomMembers.isEmpty(SelectedSection.MEMBERS)).isFalse()
            assertThat(loadedRoomMembers.isEmpty(SelectedSection.BANNED)).isFalse()
        }
    }

    @Test
    fun `search for something which is not found`() = runTest {
        val room = createFakeJoinedRoom().apply {
            givenRoomMembersState(RoomMembersState.Ready(aRoomMemberList()))
        }
        val presenter = createPresenter(joinedRoom = room)
        presenter.test {
            skipItems(1)
            val loadedState = awaitItem()
            val loadedRoomMembers = loadedState.filteredRoomMembers.dataOrNull()!!
            assertThat(loadedRoomMembers.joined).isNotEmpty()
            assertThat(loadedRoomMembers.banned).isNotEmpty()
            assertThat(loadedRoomMembers.invited).isNotEmpty()
            assertThat(loadedRoomMembers.isEmpty(SelectedSection.MEMBERS)).isFalse()
            assertThat(loadedRoomMembers.isEmpty(SelectedSection.BANNED)).isFalse()
            loadedState.eventSink(RoomMemberListEvents.UpdateSearchQuery("something"))
            val searchQueryUpdatedState = awaitItem()
            assertThat(searchQueryUpdatedState.searchQuery).isEqualTo("something")
            val searchSearchResultDelivered = awaitItem()
            val emptyRoomMembers = searchSearchResultDelivered.filteredRoomMembers.dataOrNull()!!
            assertThat(emptyRoomMembers.joined).isEmpty()
            assertThat(emptyRoomMembers.banned).isEmpty()
            assertThat(emptyRoomMembers.invited).isEmpty()
            assertThat(emptyRoomMembers.isEmpty(SelectedSection.MEMBERS)).isTrue()
            assertThat(emptyRoomMembers.isEmpty(SelectedSection.BANNED)).isTrue()
        }
    }

    @Test
    fun `search for something which is found`() = runTest {
        val room = createFakeJoinedRoom().apply {
            givenRoomMembersState(RoomMembersState.Ready(aRoomMemberList()))
        }
        val presenter = createPresenter(joinedRoom = room)
        presenter.test {
            skipItems(1)
            val loadedState = awaitItem()
            val loadedRoomMembers = loadedState.filteredRoomMembers.dataOrNull()!!
            assertThat(loadedRoomMembers.joined).isNotEmpty()
            assertThat(loadedRoomMembers.banned).isNotEmpty()
            assertThat(loadedRoomMembers.invited).isNotEmpty()
            assertThat(loadedRoomMembers.isEmpty(SelectedSection.MEMBERS)).isFalse()
            assertThat(loadedRoomMembers.isEmpty(SelectedSection.BANNED)).isFalse()
            loadedState.eventSink(RoomMemberListEvents.UpdateSearchQuery("alice"))
            val searchQueryUpdatedState = awaitItem()
            assertThat(searchQueryUpdatedState.searchQuery).isEqualTo("alice")
            val searchSearchResultDelivered = awaitItem()
            val emptyRoomMembers = searchSearchResultDelivered.filteredRoomMembers.dataOrNull()!!
            assertThat(emptyRoomMembers.joined).isNotEmpty()
            assertThat(emptyRoomMembers.banned).isEmpty()
            assertThat(emptyRoomMembers.invited).isEmpty()
            assertThat(emptyRoomMembers.isEmpty(SelectedSection.MEMBERS)).isFalse()
            assertThat(emptyRoomMembers.isEmpty(SelectedSection.BANNED)).isTrue()
        }
    }

    @Test
    fun `present - asynchronously sets canInvite when user has correct power level`() = runTest {
        val presenter = createPresenter()
        presenter.test {
            skipItems(1)
            val loadedState = awaitItem()
            assertThat(loadedState.canInvite).isTrue()
        }
    }

    @Test
    fun `present - asynchronously sets canInvite when user does not have correct power level`() = runTest {
        val presenter = createPresenter(
            joinedRoom = createFakeJoinedRoom(
                canInviteResult = { Result.success(false) },
            )
        )
        presenter.test {
            val loadedState = awaitItem()
            assertThat(loadedState.canInvite).isFalse()
        }
    }

    @Test
    fun `present - asynchronously sets canInvite when power level check fails`() = runTest {
        val presenter = createPresenter(
            joinedRoom = createFakeJoinedRoom(
                canInviteResult = { Result.failure(RuntimeException("Eek")) },
            )
        )
        presenter.test {
            val loadedState = awaitItem()
            assertThat(loadedState.canInvite).isFalse()
        }
    }

    @Test
    fun `present - RoomMemberSelected will open the moderation options`() = runTest {
        val presenter = createPresenter(
            roomMemberModerationState = aRoomMemberModerationState(canBan = true, canKick = true)
        )
        presenter.test {
            skipItems(1)
            awaitItem().eventSink(RoomMemberListEvents.RoomMemberSelected(anInvitedVictor()))
        }
    }
}

private fun createFakeJoinedRoom(
    updateMembersResult: () -> Unit = { },
    canInviteResult: (UserId) -> Result<Boolean> = { Result.success(true) },
): FakeJoinedRoom {
    return FakeJoinedRoom(
        baseRoom = FakeBaseRoom(
            updateMembersResult = updateMembersResult,
            canInviteResult = canInviteResult,
        ).apply {
            // Needed to avoid discarding the loaded members as a partial and invalid result
            givenRoomInfo(aRoomInfo(joinedMembersCount = 2))
        }
    )
}

@ExperimentalCoroutinesApi
private fun TestScope.createPresenter(
    coroutineDispatchers: CoroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true),
    joinedRoom: JoinedRoom = createFakeJoinedRoom(),
    encryptedService: FakeEncryptionService = FakeEncryptionService(),
    roomMemberModerationState: RoomMemberModerationState = aRoomMemberModerationState(),
) = RoomMemberListPresenter(
    room = joinedRoom,
    coroutineDispatchers = coroutineDispatchers,
    roomMembersModerationPresenter = Presenter {
        roomMemberModerationState
    },
    encryptionService = encryptedService,
)
