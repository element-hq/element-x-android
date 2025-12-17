/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomdirectory

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustRoomDescription
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID_3
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.RoomDirectorySearchEntryUpdate

class RoomDirectorySearchProcessorTest {
    private val rustRoom1 = aRustRoomDescription(roomId = A_ROOM_ID.value)
    private val rustRoom2 = aRustRoomDescription(roomId = A_ROOM_ID_2.value)
    private val rustRoom3 = aRustRoomDescription(roomId = A_ROOM_ID_3.value)
    private val mapper = RoomDescriptionMapper()
    private val room1 = mapper.map(rustRoom1)
    private val room2 = mapper.map(rustRoom2)
    private val room3 = mapper.map(rustRoom3)

    @Test
    fun test() = runTest {
        val sut = RoomDirectorySearchProcessor(
            coroutineContext = StandardTestDispatcher(testScheduler),
        )
        sut.roomDescriptionsFlow.test {
            sut.postUpdates(listOf(RoomDirectorySearchEntryUpdate.Reset(listOf(rustRoom1))))
            assertThat(awaitItem()).isEqualTo(listOf(room1))
            sut.postUpdates(listOf(RoomDirectorySearchEntryUpdate.Append(listOf(rustRoom2))))
            assertThat(awaitItem()).isEqualTo(listOf(room1, room2))
            sut.postUpdates(listOf(RoomDirectorySearchEntryUpdate.PushFront(rustRoom3)))
            assertThat(awaitItem()).isEqualTo(listOf(room3, room1, room2))
            sut.postUpdates(listOf(RoomDirectorySearchEntryUpdate.PopFront))
            assertThat(awaitItem()).isEqualTo(listOf(room1, room2))
            sut.postUpdates(listOf(RoomDirectorySearchEntryUpdate.PushBack(rustRoom3)))
            assertThat(awaitItem()).isEqualTo(listOf(room1, room2, room3))
            sut.postUpdates(listOf(RoomDirectorySearchEntryUpdate.PopBack))
            assertThat(awaitItem()).isEqualTo(listOf(room1, room2))
            sut.postUpdates(listOf(RoomDirectorySearchEntryUpdate.Insert(1u, rustRoom3)))
            assertThat(awaitItem()).isEqualTo(listOf(room1, room3, room2))
            sut.postUpdates(listOf(RoomDirectorySearchEntryUpdate.Remove(1u)))
            assertThat(awaitItem()).isEqualTo(listOf(room1, room2))

            sut.postUpdates(listOf(RoomDirectorySearchEntryUpdate.Reset(listOf(rustRoom1, rustRoom2))))
            assertThat(awaitItem()).isEqualTo(listOf(room1, room2))
            sut.postUpdates(listOf(RoomDirectorySearchEntryUpdate.Set(1u, rustRoom3)))
            assertThat(awaitItem()).isEqualTo(listOf(room1, room3))
            sut.postUpdates(listOf(RoomDirectorySearchEntryUpdate.Truncate(1u)))
            assertThat(awaitItem()).isEqualTo(listOf(room1))

            sut.postUpdates(listOf(RoomDirectorySearchEntryUpdate.Clear))
            assertThat(awaitItem()).isEmpty()

            // Check that all the actions are performed
            sut.postUpdates(
                listOf(
                    RoomDirectorySearchEntryUpdate.PushBack(rustRoom1),
                    RoomDirectorySearchEntryUpdate.PushBack(rustRoom2),
                    RoomDirectorySearchEntryUpdate.PushBack(rustRoom3),
                )
            )
            assertThat(awaitItem()).isEqualTo(listOf(room1, room2, room3))
        }
    }
}
