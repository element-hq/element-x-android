/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomlist

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.test.room.aRoomSummary
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomListFilterTest {
    private val regularRoom = aRoomSummary(
        isDirect = false,
    )
    private val dmRoom = aRoomSummary(
        isDirect = true,
        activeMembersCount = 2
    )
    private val favoriteRoom = aRoomSummary(
        isFavorite = true
    )
    private val markedAsUnreadRoom = aRoomSummary(
        isMarkedUnread = true
    )
    private val unreadNotificationRoom = aRoomSummary(
        numUnreadNotifications = 1
    )
    private val roomToSearch = aRoomSummary(
        name = "Room to search"
    )
    private val roomWithAccent = aRoomSummary(
        name = "Frédéric"
    )
    private val invitedRoom = aRoomSummary(
        currentUserMembership = CurrentUserMembership.INVITED
    )

    private val space = aRoomSummary(
        isSpace = true
    )
    private val invitedSpace = aRoomSummary(
        isSpace = true,
        currentUserMembership = CurrentUserMembership.INVITED
    )

    private val roomSummaries = listOf(
        regularRoom,
        dmRoom,
        favoriteRoom,
        markedAsUnreadRoom,
        unreadNotificationRoom,
        roomToSearch,
        roomWithAccent,
        invitedRoom,
        space,
        invitedSpace,
    )

    @Test
    fun `Room list filter all empty`() = runTest {
        val filter = RoomListFilter.all()
        assertThat(roomSummaries.filter(filter)).isEqualTo(roomSummaries - space)
    }

    @Test
    fun `Room list filter none`() = runTest {
        val filter = RoomListFilter.None
        assertThat(roomSummaries.filter(filter)).isEmpty()
    }

    @Test
    fun `Room list filter people`() = runTest {
        val filter = RoomListFilter.Category.People
        assertThat(roomSummaries.filter(filter)).containsExactly(dmRoom)
    }

    @Test
    fun `Room list filter group`() = runTest {
        val filter = RoomListFilter.Category.Group
        assertThat(roomSummaries.filter(filter)).containsExactly(
            regularRoom,
            favoriteRoom,
            markedAsUnreadRoom,
            unreadNotificationRoom,
            roomToSearch,
            roomWithAccent,
        )
    }

    @Test
    fun `Room list filter space`() = runTest {
        val filter = RoomListFilter.Category.Space
        assertThat(roomSummaries.filter(filter)).containsExactly(space, invitedSpace)
    }

    @Test
    fun `Room list filter favorite`() = runTest {
        val filter = RoomListFilter.Favorite
        assertThat(roomSummaries.filter(filter)).containsExactly(favoriteRoom)
    }

    @Test
    fun `Room list filter unread`() = runTest {
        val filter = RoomListFilter.Unread
        assertThat(roomSummaries.filter(filter)).containsExactly(markedAsUnreadRoom, unreadNotificationRoom)
    }

    @Test
    fun `Room list filter invites`() = runTest {
        val filter = RoomListFilter.Invite
        assertThat(roomSummaries.filter(filter)).containsExactly(invitedRoom, invitedSpace)
    }

    @Test
    fun `Room list filter normalized match room name`() = runTest {
        val filter = RoomListFilter.NormalizedMatchRoomName("search")
        assertThat(roomSummaries.filter(filter)).containsExactly(roomToSearch)
    }

    @Test
    fun `Room list filter normalized match room name with accent`() = runTest {
        val filter = RoomListFilter.NormalizedMatchRoomName("Fred")
        assertThat(roomSummaries.filter(filter)).containsExactly(roomWithAccent)
    }

    @Test
    fun `Room list filter normalized match room name with accent when searching with accent`() = runTest {
        val filter = RoomListFilter.NormalizedMatchRoomName("Fréd")
        assertThat(roomSummaries.filter(filter)).containsExactly(roomWithAccent)
    }

    @Test
    fun `Room list filter all with one match`() = runTest {
        val filter = RoomListFilter.all(
            RoomListFilter.Category.Group,
            RoomListFilter.Favorite
        )
        assertThat(roomSummaries.filter(filter)).containsExactly(favoriteRoom)
    }

    @Test
    fun `Room list filter all with no match`() = runTest {
        val filter = RoomListFilter.all(
            RoomListFilter.Category.People,
            RoomListFilter.Favorite
        )
        assertThat(roomSummaries.filter(filter)).isEmpty()
    }
}
