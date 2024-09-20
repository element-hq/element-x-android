/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomdirectory

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryList
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustRoomDescription
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeRoomDirectorySearch
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.tests.testutils.runCancellableScopeTestWithTestScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import org.junit.Test
import org.matrix.rustcomponents.sdk.RoomDirectorySearch
import org.matrix.rustcomponents.sdk.RoomDirectorySearchEntryUpdate

@OptIn(ExperimentalCoroutinesApi::class)
class RustRoomDirectoryListTest {
    @Test
    fun `check that the state emits the expected values`() = runCancellableScopeTestWithTestScope { testScope, cancellableScope ->
        val fakeRoomDirectorySearch = FakeRoomDirectorySearch()
        val mapper = RoomDescriptionMapper()
        val sut = testScope.createRustRoomDirectoryList(
            roomDirectorySearch = fakeRoomDirectorySearch,
            scope = cancellableScope,
        )
        // Let the mxCallback be ready
        testScope.runCurrent()
        sut.state.test {
            sut.filter("", 20)
            fakeRoomDirectorySearch.emitResult(
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
            fakeRoomDirectorySearch.isAtLastPage = true
            sut.loadMore()
            fakeRoomDirectorySearch.emitResult(
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
        roomDirectorySearch: RoomDirectorySearch = FakeRoomDirectorySearch(),
        scope: CoroutineScope,
    ) = RustRoomDirectoryList(
        inner = roomDirectorySearch,
        coroutineScope = scope,
        coroutineContext = StandardTestDispatcher(testScheduler),
    )
}
