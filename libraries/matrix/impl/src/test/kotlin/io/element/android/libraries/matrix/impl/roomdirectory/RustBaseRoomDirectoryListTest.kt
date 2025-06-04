/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomdirectory

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryList
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustRoomDescription
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiRoomDirectorySearch
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.RoomDirectorySearch
import org.matrix.rustcomponents.sdk.RoomDirectorySearchEntryUpdate

@OptIn(ExperimentalCoroutinesApi::class)
class RustBaseRoomDirectoryListTest {
    @Test
    fun `check that the state emits the expected values`() = runTest {
        val roomDirectorySearch = FakeFfiRoomDirectorySearch()
        val mapper = RoomDescriptionMapper()
        val sut = createRustRoomDirectoryList(
            roomDirectorySearch = roomDirectorySearch,
        )
        // Let the mxCallback be ready
        runCurrent()
        sut.state.test {
            sut.filter(filter = "", batchSize = 20, viaServerName = null)
            roomDirectorySearch.emitResult(
                listOf(
                    RoomDirectorySearchEntryUpdate.Append(listOf(aRustRoomDescription()))
                )
            )
            val initialItem = awaitItem()
            assertThat(initialItem).isEqualTo(
                RoomDirectoryList.State(
                    hasMoreToLoad = true,
                    items = listOf(mapper.map(aRustRoomDescription()))
                )
            )
            assertThat(initialItem.hasMoreToLoad).isTrue()
            roomDirectorySearch.isAtLastPage = true
            sut.loadMore()
            roomDirectorySearch.emitResult(
                listOf(
                    RoomDirectorySearchEntryUpdate.Append(listOf(aRustRoomDescription(A_ROOM_ID_2.value)))
                )
            )
            val nextItem = awaitItem()
            assertThat(nextItem).isEqualTo(
                RoomDirectoryList.State(
                    hasMoreToLoad = false,
                    items = listOf(
                        mapper.map(aRustRoomDescription()),
                    )
                )
            )
            val finalItem = awaitItem()
            assertThat(finalItem).isEqualTo(
                RoomDirectoryList.State(
                    hasMoreToLoad = false,
                    items = listOf(
                        mapper.map(aRustRoomDescription()),
                        mapper.map(aRustRoomDescription(A_ROOM_ID_2.value)),
                    )
                )
            )
        }
    }

    private fun TestScope.createRustRoomDirectoryList(
        roomDirectorySearch: RoomDirectorySearch = FakeFfiRoomDirectorySearch(),
    ) = RustRoomDirectoryList(
        inner = roomDirectorySearch,
        coroutineScope = backgroundScope,
        coroutineContext = StandardTestDispatcher(testScheduler),
    )
}
