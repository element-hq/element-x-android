/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.features.share.api.SharingRoomInfo
import io.element.android.features.share.test.FakeDirectShareShortcutsPublisher
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.libraries.matrix.test.roomlist.FakeDynamicRoomList
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultDirectShareShortcutsObserverTest {
    @Test
    fun `start - excludes spaces so that only chats become share targets`() = runTest {
        val roomList = FakeDynamicRoomList()
        val observer = createObserver(roomList)

        observer.start()
        runCurrent()

        assertThat(roomList.currentFilter.value).isEqualTo(
            RoomListFilter.any(
                RoomListFilter.Category.Group,
                RoomListFilter.Category.People,
            )
        )
    }

    @Test
    fun `start - publishes the rooms as share targets`() = runTest {
        val roomList = FakeDynamicRoomList()
        val published = mutableListOf<List<SharingRoomInfo>>()
        val observer = createObserver(roomList, published)

        observer.start()
        roomList.summaries.value = listOf(
            aRoomSummary(roomId = A_ROOM_ID, name = "Room one", avatarUrl = "an-avatar-url"),
        )
        runCurrent()

        assertThat(published.last()).isEqualTo(
            listOf(
                SharingRoomInfo(
                    sessionId = A_SESSION_ID,
                    roomId = A_ROOM_ID,
                    displayName = "Room one",
                    avatarUrl = "an-avatar-url",
                )
            )
        )
    }

    @Test
    fun `start - does not republish when a field irrelevant to the shortcut changes`() = runTest {
        val roomList = FakeDynamicRoomList()
        val published = mutableListOf<List<SharingRoomInfo>>()
        val observer = createObserver(roomList, published)

        observer.start()
        roomList.summaries.value = listOf(aRoomSummary(roomId = A_ROOM_ID, name = "Room one"))
        runCurrent()
        val countAfterFirstEmission = published.size

        // Same room, but a new message arrived: nothing a shortcut renders has changed.
        roomList.summaries.value = listOf(aRoomSummary(roomId = A_ROOM_ID, name = "Room one", notificationCount = 5))
        runCurrent()

        assertThat(published.size).isEqualTo(countAfterFirstEmission)
    }

    @Test
    fun `start - republishes when the room name changes`() = runTest {
        val roomList = FakeDynamicRoomList()
        val published = mutableListOf<List<SharingRoomInfo>>()
        val observer = createObserver(roomList, published)

        observer.start()
        roomList.summaries.value = listOf(aRoomSummary(roomId = A_ROOM_ID, name = "Room one"))
        runCurrent()
        val countAfterFirstEmission = published.size

        roomList.summaries.value = listOf(aRoomSummary(roomId = A_ROOM_ID, name = "Renamed room"))
        runCurrent()

        assertThat(published.size).isEqualTo(countAfterFirstEmission + 1)
        assertThat(published.last().single().displayName).isEqualTo("Renamed room")
    }

    @Test
    fun `start - is a no-op when already started`() = runTest {
        val roomList = FakeDynamicRoomList()
        val published = mutableListOf<List<SharingRoomInfo>>()
        val observer = createObserver(roomList, published)

        observer.start()
        observer.start()
        roomList.summaries.value = listOf(aRoomSummary(roomId = A_ROOM_ID_2, name = "Room two"))
        runCurrent()

        assertThat(published.count { it.isNotEmpty() }).isEqualTo(1)
    }

    private fun TestScope.createObserver(
        roomList: FakeDynamicRoomList,
        published: MutableList<List<SharingRoomInfo>> = mutableListOf(),
    ) = DefaultDirectShareShortcutsObserver(
        client = FakeMatrixClient(
            roomListService = FakeRoomListService(createRoomListLambda = { roomList }),
        ),
        directShareShortcutsPublisher = FakeDirectShareShortcutsPublisher(
            publishShortcutsForRoomsLambda = { published.add(it) },
        ),
        sessionCoroutineScope = backgroundScope,
    )
}
