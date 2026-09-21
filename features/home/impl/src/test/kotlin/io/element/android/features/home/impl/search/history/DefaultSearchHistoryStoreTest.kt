/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search.history

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test

class DefaultSearchHistoryStoreTest {
    @Test
    fun `history is empty when nothing has been persisted`() = runTest {
        val store = createStore()
        assertThat(store.history.first()).isEmpty()
    }

    @Test
    fun `add prepends new entries so the most recent is first`() = runTest {
        val store = createStore()
        store.add(SearchHistoryResult.Query("first"))
        store.add(SearchHistoryResult.Query("second"))
        assertThat(store.history.first()).containsExactly(
            SearchHistoryResult.Query("second"),
            SearchHistoryResult.Query("first"),
        ).inOrder()
    }

    @Test
    fun `add supports both queries and rooms`() = runTest {
        val store = createStore()
        store.add(SearchHistoryResult.Query("term"))
        store.add(SearchHistoryResult.Room(A_ROOM_ID))
        assertThat(store.history.first()).containsExactly(
            SearchHistoryResult.Room(A_ROOM_ID),
            SearchHistoryResult.Query("term"),
        ).inOrder()
    }

    @Test
    fun `adding an existing entry moves it to the front without duplicating`() = runTest {
        val store = createStore()
        store.add(SearchHistoryResult.Query("a"))
        store.add(SearchHistoryResult.Query("b"))
        store.add(SearchHistoryResult.Query("a"))
        assertThat(store.history.first()).containsExactly(
            SearchHistoryResult.Query("a"),
            SearchHistoryResult.Query("b"),
        ).inOrder()
    }

    @Test
    fun `history is capped to the maximum size, dropping the oldest entries`() = runTest {
        val store = createStore()
        repeat(MAX_SEARCH_HISTORY_SIZE + 10) { index ->
            store.add(SearchHistoryResult.Query("query$index"))
        }
        val history = store.history.first()
        assertThat(history).hasSize(MAX_SEARCH_HISTORY_SIZE)
        // Newest entry is first...
        assertThat(history.first()).isEqualTo(SearchHistoryResult.Query("query${MAX_SEARCH_HISTORY_SIZE + 9}"))
        // ...and the oldest ones have been dropped.
        assertThat(history).doesNotContain(SearchHistoryResult.Query("query0"))
        assertThat(history.last()).isEqualTo(SearchHistoryResult.Query("query10"))
    }

    @Test
    fun `persisted history is deserialized back into SearchHistoryResult items`() = runTest {
        val fileStore = FakeSearchHistoryFileStore()
        createStore(fileStore).apply {
            add(SearchHistoryResult.Query("persisted term"))
            add(SearchHistoryResult.Room(A_ROOM_ID))
        }

        // A brand new store backed by the same file reads the same content back.
        val reloaded = createStore(fileStore)
        assertThat(reloaded.history.first()).containsExactly(
            SearchHistoryResult.Room(A_ROOM_ID),
            SearchHistoryResult.Query("persisted term"),
        ).inOrder()
    }

    @Test
    fun `clear removes every entry and deletes the persisted content`() = runTest {
        val fileStore = FakeSearchHistoryFileStore()
        val store = createStore(fileStore)
        store.add(SearchHistoryResult.Query("term"))

        store.clear()

        assertThat(store.history.first()).isEmpty()
        assertThat(fileStore.content).isNull()
    }

    @Test
    fun `corrupted content is ignored and results in an empty history`() = runTest {
        val fileStore = FakeSearchHistoryFileStore(initialContent = "not valid json".toByteArray())
        val store = createStore(fileStore)
        assertThat(store.history.first()).isEmpty()
        // The store recovers and can still add new entries.
        store.add(SearchHistoryResult.Room(RoomId("!recovered:domain")))
        assertThat(store.history.first()).containsExactly(SearchHistoryResult.Room(RoomId("!recovered:domain")))
    }

    private fun TestScope.createStore(
        fileStore: SearchHistoryFileStore = FakeSearchHistoryFileStore(),
    ) = DefaultSearchHistoryStore(
        fileStore = fileStore,
        dispatchers = testCoroutineDispatchers(),
        jsonProvider = { Json { ignoreUnknownKeys = true } }
    )
}
