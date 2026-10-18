/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.search.MessageSearchResult
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustSearchServiceResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.SearchServiceResultsUpdate

class MessageSearchResultsProcessorTest {
    private val results = MutableStateFlow<List<MessageSearchResult>>(emptyList())

    private fun createProcessor() = MessageSearchResultsProcessor(
        results = results,
        coroutineContext = kotlin.coroutines.EmptyCoroutineContext,
    )

    private fun eventIds() = results.value.map { it.eventId.value }

    @Test
    fun `Append adds entries at the end of the list`() = runTest {
        val processor = createProcessor()
        processor.postUpdates(listOf(SearchServiceResultsUpdate.Append(listOf(aRustSearchServiceResult("\$1"), aRustSearchServiceResult("\$2")))))

        assertThat(eventIds()).isEqualTo(listOf("\$1", "\$2"))
    }

    @Test
    fun `PushBack adds an entry at the end of the list`() = runTest {
        val processor = createProcessor()
        processor.postUpdates(listOf(SearchServiceResultsUpdate.Append(listOf(aRustSearchServiceResult("\$1")))))
        processor.postUpdates(listOf(SearchServiceResultsUpdate.PushBack(aRustSearchServiceResult("\$2"))))

        assertThat(eventIds()).isEqualTo(listOf("\$1", "\$2"))
    }

    @Test
    fun `PushFront inserts an entry at the start of the list`() = runTest {
        val processor = createProcessor()
        processor.postUpdates(listOf(SearchServiceResultsUpdate.Append(listOf(aRustSearchServiceResult("\$1")))))
        processor.postUpdates(listOf(SearchServiceResultsUpdate.PushFront(aRustSearchServiceResult("\$0"))))

        assertThat(eventIds()).isEqualTo(listOf("\$0", "\$1"))
    }

    @Test
    fun `PopBack removes the last entry`() = runTest {
        val processor = createProcessor()
        processor.postUpdates(listOf(SearchServiceResultsUpdate.Append(listOf(aRustSearchServiceResult("\$1"), aRustSearchServiceResult("\$2")))))
        processor.postUpdates(listOf(SearchServiceResultsUpdate.PopBack))

        assertThat(eventIds()).isEqualTo(listOf("\$1"))
    }

    @Test
    fun `PopFront removes the first entry`() = runTest {
        val processor = createProcessor()
        processor.postUpdates(listOf(SearchServiceResultsUpdate.Append(listOf(aRustSearchServiceResult("\$1"), aRustSearchServiceResult("\$2")))))
        processor.postUpdates(listOf(SearchServiceResultsUpdate.PopFront))

        assertThat(eventIds()).isEqualTo(listOf("\$2"))
    }

    @Test
    fun `Insert puts an entry at the given index`() = runTest {
        val processor = createProcessor()
        processor.postUpdates(listOf(SearchServiceResultsUpdate.Append(listOf(aRustSearchServiceResult("\$1"), aRustSearchServiceResult("\$3")))))
        processor.postUpdates(listOf(SearchServiceResultsUpdate.Insert(1u, aRustSearchServiceResult("\$2"))))

        assertThat(eventIds()).isEqualTo(listOf("\$1", "\$2", "\$3"))
    }

    @Test
    fun `Set replaces the entry at the given index`() = runTest {
        val processor = createProcessor()
        processor.postUpdates(listOf(SearchServiceResultsUpdate.Append(listOf(aRustSearchServiceResult("\$1"), aRustSearchServiceResult("\$2")))))
        processor.postUpdates(listOf(SearchServiceResultsUpdate.Set(1u, aRustSearchServiceResult("\$9"))))

        assertThat(eventIds()).isEqualTo(listOf("\$1", "\$9"))
    }

    @Test
    fun `Remove drops the entry at the given index`() = runTest {
        val processor = createProcessor()
        processor.postUpdates(
            listOf(SearchServiceResultsUpdate.Append(listOf(aRustSearchServiceResult("\$1"), aRustSearchServiceResult("\$2"), aRustSearchServiceResult("\$3"))))
        )
        processor.postUpdates(listOf(SearchServiceResultsUpdate.Remove(1u)))

        assertThat(eventIds()).isEqualTo(listOf("\$1", "\$3"))
    }

    @Test
    fun `Truncate keeps only the first N entries`() = runTest {
        val processor = createProcessor()
        processor.postUpdates(
            listOf(SearchServiceResultsUpdate.Append(listOf(aRustSearchServiceResult("\$1"), aRustSearchServiceResult("\$2"), aRustSearchServiceResult("\$3"))))
        )
        processor.postUpdates(listOf(SearchServiceResultsUpdate.Truncate(2u)))

        assertThat(eventIds()).isEqualTo(listOf("\$1", "\$2"))
    }

    @Test
    fun `Clear empties the list`() = runTest {
        val processor = createProcessor()
        processor.postUpdates(listOf(SearchServiceResultsUpdate.Append(listOf(aRustSearchServiceResult("\$1")))))
        processor.postUpdates(listOf(SearchServiceResultsUpdate.Clear))

        assertThat(eventIds()).isEmpty()
    }

    @Test
    fun `Reset replaces the whole list`() = runTest {
        val processor = createProcessor()
        processor.postUpdates(listOf(SearchServiceResultsUpdate.Append(listOf(aRustSearchServiceResult("\$1"), aRustSearchServiceResult("\$2")))))
        processor.postUpdates(listOf(SearchServiceResultsUpdate.Reset(listOf(aRustSearchServiceResult("\$7")))))

        assertThat(eventIds()).isEqualTo(listOf("\$7"))
    }

    /**
     * Regression test for the index-desync bug present in the element-x-ios bridge, where positional
     * updates from the SDK were applied to a list that had already been filtered, so every index
     * landed in the wrong slot. Our list must stay index-parallel to the SDK's throughout a batch.
     */
    @Test
    fun `positional updates in one batch stay index-parallel with the SDK`() = runTest {
        val processor = createProcessor()
        processor.postUpdates(
            listOf(
                SearchServiceResultsUpdate.Append(
                    listOf(aRustSearchServiceResult("\$1"), aRustSearchServiceResult("\$2"), aRustSearchServiceResult("\$3"))
                ),
                SearchServiceResultsUpdate.Insert(1u, aRustSearchServiceResult("\$b")),
                SearchServiceResultsUpdate.Remove(0u),
                SearchServiceResultsUpdate.Set(0u, aRustSearchServiceResult("\$z")),
                SearchServiceResultsUpdate.PushFront(aRustSearchServiceResult("\$a")),
                SearchServiceResultsUpdate.Truncate(3u),
            )
        )

        // Append      -> [1, 2, 3]
        // Insert(1,b) -> [1, b, 2, 3]
        // Remove(0)   -> [b, 2, 3]
        // Set(0,z)    -> [z, 2, 3]
        // PushFront(a)-> [a, z, 2, 3]
        // Truncate(3) -> [a, z, 2]
        assertThat(eventIds()).isEqualTo(listOf("\$a", "\$z", "\$2"))
    }

    @Test
    fun `a result carries roomId, sender and timestamp through the mapping`() = runTest {
        val processor = createProcessor()
        processor.postUpdates(
            listOf(
                SearchServiceResultsUpdate.Append(
                    listOf(aRustSearchServiceResult(eventId = "\$1", roomId = "!room:server", sender = "@bob:server", timestamp = 1234uL))
                )
            )
        )

        val result = results.value.single()
        assertThat(result.roomId.value).isEqualTo("!room:server")
        assertThat(result.senderId.value).isEqualTo("@bob:server")
        assertThat(result.timestamp).isEqualTo(1234L)
    }
}
