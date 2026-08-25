/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.space.impl.root

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import com.google.common.truth.Truth.assertThat
import io.element.android.features.invite.api.acceptdecline.anAcceptDeclineInviteState
import io.element.android.features.invite.test.InMemorySeenInvitesStore
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.api.spaces.SpaceRoomList
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID_3
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.join.FakeJoinRoom
import io.element.android.libraries.matrix.test.spaces.FakeSpaceRoomList
import io.element.android.libraries.matrix.test.spaces.FakeSpaceService
import io.element.android.libraries.previewutils.room.aSpaceRoom
import io.element.android.tests.testutils.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SpaceSearchPresenterTest {
    private val mathsLecture = aSpaceRoom(roomId = A_ROOM_ID, displayName = "Maths lecture")
    private val physicsLecture = aSpaceRoom(roomId = A_ROOM_ID_2, displayName = "Physics lecture")
    private val announcements = aSpaceRoom(
        roomId = A_ROOM_ID_3,
        displayName = "Announcements",
        canonicalAlias = RoomAlias("#maths-announcements:example.com"),
    )

    @Test
    fun `present - searching filters the children on their name and their alias`() = runTest {
        val presenter = createSpacePresenter(listOf(mathsLecture, physicsLecture, announcements))
        presenter.test {
            awaitItem()
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertThat(state.children).hasSize(3)
            assertThat(state.searchResults).isEqualTo(SearchBarResultState.Initial)
            state.eventSink(SpaceEvent.OnSearchActiveChanged(true))
            awaitItem()
            state.searchQuery.setTextAndPlaceCursorAtEnd("maths")
            advanceUntilIdle()
            val searchState = expectMostRecentItem()
            assertThat(searchState.isSearchActive).isTrue()
            assertThat(searchState.searchResults).isEqualTo(
                SearchBarResultState.Results(persistentListOf(mathsLecture, announcements))
            )
        }
    }

    @Test
    fun `present - searching for something absent reports no results`() = runTest {
        val presenter = createSpacePresenter(listOf(mathsLecture))
        presenter.test {
            awaitItem()
            advanceUntilIdle()
            val state = expectMostRecentItem()
            state.eventSink(SpaceEvent.OnSearchActiveChanged(true))
            awaitItem()
            state.searchQuery.setTextAndPlaceCursorAtEnd("chemistry")
            advanceUntilIdle()
            assertThat(expectMostRecentItem().searchResults).isEqualTo(SearchBarResultState.NoResultsFound)
        }
    }

    @Test
    fun `present - leaving the search clears the query`() = runTest {
        val presenter = createSpacePresenter(listOf(mathsLecture))
        presenter.test {
            val state = awaitItem()
            state.eventSink(SpaceEvent.OnSearchActiveChanged(true))
            awaitItem()
            state.searchQuery.setTextAndPlaceCursorAtEnd("maths")
            awaitItem()
            state.eventSink(SpaceEvent.OnSearchActiveChanged(false))
            advanceUntilIdle()
            val finalState = expectMostRecentItem()
            assertThat(finalState.isSearchActive).isFalse()
            assertThat(finalState.searchQuery.text.toString()).isEmpty()
            assertThat(finalState.searchResults).isEqualTo(SearchBarResultState.Initial)
        }
    }

    private fun TestScope.createSpacePresenter(children: List<SpaceRoom>): SpacePresenter {
        val spaceRoomList: SpaceRoomList = FakeSpaceRoomList(
            initialSpaceRoomsValue = children,
            paginateResult = { Result.success(Unit) },
        )
        return SpacePresenter(
            client = FakeMatrixClient(),
            room = FakeBaseRoom(),
            spaceRoomList = spaceRoomList,
            seenInvitesStore = InMemorySeenInvitesStore(),
            joinRoom = FakeJoinRoom(lambda = { _, _, _ -> Result.success(Unit) }),
            acceptDeclineInvitePresenter = Presenter { anAcceptDeclineInviteState() },
            sessionCoroutineScope = this,
            spaceService = FakeSpaceService(),
        )
    }
}
