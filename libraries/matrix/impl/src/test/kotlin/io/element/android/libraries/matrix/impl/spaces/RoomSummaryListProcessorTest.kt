/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.spaces

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustSpaceRoom
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID_3
import io.element.android.libraries.previewutils.room.aSpaceRoom
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Ignore
import org.junit.Test
import org.matrix.rustcomponents.sdk.SpaceListUpdate

class RoomSummaryListProcessorTest {
    private val spaceRoomsFlow = MutableStateFlow<List<SpaceRoom>>(emptyList())

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `Append adds new entries at the end of the list`() = runTest {
        spaceRoomsFlow.value = listOf(aSpaceRoom())
        val processor = createProcessor()

        val newEntry = aRustSpaceRoom(roomId = A_ROOM_ID_2)
        processor.postUpdates(listOf(SpaceListUpdate.Append(listOf(newEntry, newEntry, newEntry))))

        assertThat(spaceRoomsFlow.value.count()).isEqualTo(4)
        assertThat(spaceRoomsFlow.value.subList(1, 4).all { it.roomId == A_ROOM_ID_2 }).isTrue()
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `PushBack adds a new entry at the end of the list`() = runTest {
        spaceRoomsFlow.value = listOf(aSpaceRoom())
        val processor = createProcessor()
        processor.postUpdates(listOf(SpaceListUpdate.PushBack(aRustSpaceRoom(roomId = A_ROOM_ID_2))))

        assertThat(spaceRoomsFlow.value.count()).isEqualTo(2)
        assertThat(spaceRoomsFlow.value.last().roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `PushFront inserts a new entry at the start of the list`() = runTest {
        spaceRoomsFlow.value = listOf(aSpaceRoom())
        val processor = createProcessor()
        processor.postUpdates(listOf(SpaceListUpdate.PushFront(aRustSpaceRoom(roomId = A_ROOM_ID_2))))

        assertThat(spaceRoomsFlow.value.count()).isEqualTo(2)
        assertThat(spaceRoomsFlow.value.first().roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `Set replaces an entry at some index`() = runTest {
        spaceRoomsFlow.value = listOf(aSpaceRoom())
        val processor = createProcessor()
        val index = 0

        processor.postUpdates(listOf(SpaceListUpdate.Set(index.toUInt(), aRustSpaceRoom(roomId = A_ROOM_ID_2))))

        assertThat(spaceRoomsFlow.value.count()).isEqualTo(1)
        assertThat(spaceRoomsFlow.value[index].roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `Insert inserts a new entry at the provided index`() = runTest {
        spaceRoomsFlow.value = listOf(aSpaceRoom())
        val processor = createProcessor()
        val index = 0

        processor.postUpdates(listOf(SpaceListUpdate.Insert(index.toUInt(), aRustSpaceRoom(roomId = A_ROOM_ID_2))))

        assertThat(spaceRoomsFlow.value.count()).isEqualTo(2)
        assertThat(spaceRoomsFlow.value[index].roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `Remove removes an entry at some index`() = runTest {
        spaceRoomsFlow.value = listOf(
            aSpaceRoom(roomId = A_ROOM_ID),
            aSpaceRoom(roomId = A_ROOM_ID_2)
        )
        val processor = createProcessor()
        val index = 0

        processor.postUpdates(listOf(SpaceListUpdate.Remove(index.toUInt())))

        assertThat(spaceRoomsFlow.value.count()).isEqualTo(1)
        assertThat(spaceRoomsFlow.value[index].roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `PopBack removes an entry at the end of the list`() = runTest {
        spaceRoomsFlow.value = listOf(
            aSpaceRoom(roomId = A_ROOM_ID),
            aSpaceRoom(roomId = A_ROOM_ID_2)
        )
        val processor = createProcessor()
        val index = 0

        processor.postUpdates(listOf(SpaceListUpdate.PopBack))

        assertThat(spaceRoomsFlow.value.count()).isEqualTo(1)
        assertThat(spaceRoomsFlow.value[index].roomId).isEqualTo(A_ROOM_ID)
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `PopFront removes an entry at the start of the list`() = runTest {
        spaceRoomsFlow.value = listOf(
            aSpaceRoom(roomId = A_ROOM_ID),
            aSpaceRoom(roomId = A_ROOM_ID_2)
        )
        val processor = createProcessor()
        val index = 0

        processor.postUpdates(listOf(SpaceListUpdate.PopFront))

        assertThat(spaceRoomsFlow.value.count()).isEqualTo(1)
        assertThat(spaceRoomsFlow.value[index].roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `Clear removes all the entries`() = runTest {
        spaceRoomsFlow.value = listOf(
            aSpaceRoom(roomId = A_ROOM_ID),
            aSpaceRoom(roomId = A_ROOM_ID_2)
        )
        val processor = createProcessor()

        processor.postUpdates(listOf(SpaceListUpdate.Clear))

        assertThat(spaceRoomsFlow.value).isEmpty()
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `Truncate removes all entries after the provided length`() = runTest {
        spaceRoomsFlow.value = listOf(
            aSpaceRoom(roomId = A_ROOM_ID),
            aSpaceRoom(roomId = A_ROOM_ID_2)
        )
        val processor = createProcessor()
        val index = 0

        processor.postUpdates(listOf(SpaceListUpdate.Truncate(1u)))

        assertThat(spaceRoomsFlow.value.count()).isEqualTo(1)
        assertThat(spaceRoomsFlow.value[index].roomId).isEqualTo(A_ROOM_ID)
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `Reset removes all entries and add the provided ones`() = runTest {
        spaceRoomsFlow.value = listOf(
            aSpaceRoom(roomId = A_ROOM_ID),
            aSpaceRoom(roomId = A_ROOM_ID_2)
        )
        val processor = createProcessor()
        val index = 0

        processor.postUpdates(listOf(SpaceListUpdate.Reset(listOf(aRustSpaceRoom(A_ROOM_ID_3)))))

        assertThat(spaceRoomsFlow.value.count()).isEqualTo(1)
        assertThat(spaceRoomsFlow.value[index].roomId).isEqualTo(A_ROOM_ID_3)
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `When there is no replay cache SpaceListUpdateProcessor starts with an empty list`() = runTest {
        val spaceRoomsSharedFlow = MutableSharedFlow<List<SpaceRoom>>(replay = 1)
        val processor = createProcessor(
            spaceRoomsFlow = spaceRoomsSharedFlow,
        )
        assertThat(spaceRoomsSharedFlow.replayCache).isEmpty()
        val newEntry = aRustSpaceRoom(roomId = A_ROOM_ID)
        processor.postUpdates(listOf(SpaceListUpdate.Append(listOf(newEntry))))
        assertThat(spaceRoomsSharedFlow.replayCache).hasSize(1)
        assertThat(spaceRoomsSharedFlow.replayCache.first()).hasSize(1)
    }

    private fun createProcessor(
        spaceRoomsFlow: MutableSharedFlow<List<SpaceRoom>> = this.spaceRoomsFlow,
    ) = SpaceListUpdateProcessor(
        spaceRoomsFlow = spaceRoomsFlow,
        mapper = SpaceRoomMapper(),
    )
}
