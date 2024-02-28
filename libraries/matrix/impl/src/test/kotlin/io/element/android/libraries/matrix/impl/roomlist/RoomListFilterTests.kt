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

package io.element.android.libraries.matrix.impl.roomlist

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.test.room.aRoomSummaryDetails
import io.element.android.libraries.matrix.test.room.aRoomSummaryFilled
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomListFilterTests {
    private val regularRoom = aRoomSummaryFilled(
        aRoomSummaryDetails(
            isDirect = false
        )
    )
    private val directRoom = aRoomSummaryFilled(
        aRoomSummaryDetails(
            isDirect = true
        )
    )
    private val favoriteRoom = aRoomSummaryFilled(
        aRoomSummaryDetails(
            isFavorite = true
        )
    )
    private val markedAsUnreadRoom = aRoomSummaryFilled(
        aRoomSummaryDetails(
            isMarkedUnread = true
        )
    )
    private val unreadNotificationRoom = aRoomSummaryFilled(
        aRoomSummaryDetails(
            numUnreadNotifications = 1
        )
    )
    private val roomToSearch = aRoomSummaryFilled(
        aRoomSummaryDetails(
            name = "Room to search"
        )
    )

    private val roomSummaries = listOf(
        regularRoom,
        directRoom,
        favoriteRoom,
        markedAsUnreadRoom,
        unreadNotificationRoom,
        roomToSearch
    )

    @Test
    fun `Room list filter all empty`() = runTest {
        val filter = RoomListFilter.all()
        assertThat(roomSummaries.filter(filter)).isEqualTo(roomSummaries)
    }

    @Test
    fun `Room list filter none`() = runTest {
        val filter = RoomListFilter.None
        assertThat(roomSummaries.filter(filter)).isEmpty()
    }

    @Test
    fun `Room list filter people`() = runTest {
        val filter = RoomListFilter.Category.People
        assertThat(roomSummaries.filter(filter)).containsExactly(directRoom)
    }

    @Test
    fun `Room list filter group`() = runTest {
        val filter = RoomListFilter.Category.Group
        assertThat(roomSummaries.filter(filter)).containsExactly(regularRoom, favoriteRoom, markedAsUnreadRoom, unreadNotificationRoom, roomToSearch)
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
    fun `Room list filter normalized match room name`() = runTest {
        val filter = RoomListFilter.NormalizedMatchRoomName("search")
        assertThat(roomSummaries.filter(filter)).containsExactly(roomToSearch)
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
