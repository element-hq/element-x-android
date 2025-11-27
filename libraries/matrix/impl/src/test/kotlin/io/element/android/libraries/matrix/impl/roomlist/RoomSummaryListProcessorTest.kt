/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomlist

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiRoom
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiRoomListService
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID_3
import io.element.android.libraries.matrix.test.room.aRoomSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Ignore
import org.junit.Test
import org.matrix.rustcomponents.sdk.RoomListEntriesUpdate

class RoomSummaryListProcessorTest {
    private val summaries = MutableStateFlow<List<RoomSummary>>(emptyList())

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `Append adds new entries at the end of the list`() = runTest {
        summaries.value = listOf(aRoomSummary())
        val processor = createProcessor()

        val newEntry = aRustRoom(A_ROOM_ID_2)
        processor.postUpdate(listOf(RoomListEntriesUpdate.Append(listOf(newEntry, newEntry, newEntry))))

        assertThat(summaries.value.count()).isEqualTo(4)
        assertThat(summaries.value.subList(1, 4).all { it.roomId == A_ROOM_ID_2 }).isTrue()
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `PushBack adds a new entry at the end of the list`() = runTest {
        summaries.value = listOf(aRoomSummary())
        val processor = createProcessor()
        processor.postUpdate(listOf(RoomListEntriesUpdate.PushBack(aRustRoom(A_ROOM_ID_2))))

        assertThat(summaries.value.count()).isEqualTo(2)
        assertThat(summaries.value.last().roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `PushFront inserts a new entry at the start of the list`() = runTest {
        summaries.value = listOf(aRoomSummary())
        val processor = createProcessor()
        processor.postUpdate(listOf(RoomListEntriesUpdate.PushFront(aRustRoom(A_ROOM_ID_2))))

        assertThat(summaries.value.count()).isEqualTo(2)
        assertThat(summaries.value.first().roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `Set replaces an entry at some index`() = runTest {
        summaries.value = listOf(aRoomSummary())
        val processor = createProcessor()
        val index = 0

        processor.postUpdate(listOf(RoomListEntriesUpdate.Set(index.toUInt(), aRustRoom(A_ROOM_ID_2))))

        assertThat(summaries.value.count()).isEqualTo(1)
        assertThat(summaries.value[index].roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `Insert inserts a new entry at the provided index`() = runTest {
        summaries.value = listOf(aRoomSummary())
        val processor = createProcessor()
        val index = 0

        processor.postUpdate(listOf(RoomListEntriesUpdate.Insert(index.toUInt(), aRustRoom(A_ROOM_ID_2))))

        assertThat(summaries.value.count()).isEqualTo(2)
        assertThat(summaries.value[index].roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `Remove removes an entry at some index`() = runTest {
        summaries.value = listOf(
            aRoomSummary(roomId = A_ROOM_ID),
            aRoomSummary(A_ROOM_ID_2)
        )
        val processor = createProcessor()
        val index = 0

        processor.postUpdate(listOf(RoomListEntriesUpdate.Remove(index.toUInt())))

        assertThat(summaries.value.count()).isEqualTo(1)
        assertThat(summaries.value[index].roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `PopBack removes an entry at the end of the list`() = runTest {
        summaries.value = listOf(
            aRoomSummary(roomId = A_ROOM_ID),
            aRoomSummary(A_ROOM_ID_2)
        )
        val processor = createProcessor()
        val index = 0

        processor.postUpdate(listOf(RoomListEntriesUpdate.PopBack))

        assertThat(summaries.value.count()).isEqualTo(1)
        assertThat(summaries.value[index].roomId).isEqualTo(A_ROOM_ID)
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `PopFront removes an entry at the start of the list`() = runTest {
        summaries.value = listOf(
            aRoomSummary(roomId = A_ROOM_ID),
            aRoomSummary(A_ROOM_ID_2)
        )
        val processor = createProcessor()
        val index = 0

        processor.postUpdate(listOf(RoomListEntriesUpdate.PopFront))

        assertThat(summaries.value.count()).isEqualTo(1)
        assertThat(summaries.value[index].roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `Clear removes all the entries`() = runTest {
        summaries.value = listOf(
            aRoomSummary(roomId = A_ROOM_ID),
            aRoomSummary(A_ROOM_ID_2)
        )
        val processor = createProcessor()

        processor.postUpdate(listOf(RoomListEntriesUpdate.Clear))

        assertThat(summaries.value).isEmpty()
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `Truncate removes all entries after the provided length`() = runTest {
        summaries.value = listOf(
            aRoomSummary(roomId = A_ROOM_ID),
            aRoomSummary(A_ROOM_ID_2)
        )
        val processor = createProcessor()
        val index = 0

        processor.postUpdate(listOf(RoomListEntriesUpdate.Truncate(1u)))

        assertThat(summaries.value.count()).isEqualTo(1)
        assertThat(summaries.value[index].roomId).isEqualTo(A_ROOM_ID)
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `Reset removes all entries and add the provided ones`() = runTest {
        summaries.value = listOf(
            aRoomSummary(roomId = A_ROOM_ID),
            aRoomSummary(A_ROOM_ID_2)
        )
        val processor = createProcessor()
        val index = 0

        processor.postUpdate(listOf(RoomListEntriesUpdate.Reset(listOf(aRustRoom(A_ROOM_ID_3)))))

        assertThat(summaries.value.count()).isEqualTo(1)
        assertThat(summaries.value[index].roomId).isEqualTo(A_ROOM_ID_3)
    }

    private fun aRustRoom(roomId: RoomId = A_ROOM_ID) = FakeFfiRoom(
        roomId = roomId,
        latestEventLambda = { null },
    )

    private fun TestScope.createProcessor() = RoomSummaryListProcessor(
        summaries,
        FakeFfiRoomListService(),
        coroutineContext = StandardTestDispatcher(testScheduler),
        roomSummaryFactory = RoomSummaryFactory(),
    )
}
