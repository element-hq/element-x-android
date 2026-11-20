/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search.history

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import kotlinx.serialization.json.Json
import org.junit.Test

class SearchHistoryResultSerializationTest {
    private val json = Json

    @Test
    fun `Query round-trips through serialization`() {
        val original: SearchHistoryResult = SearchHistoryResult.Query("hello world")
        val decoded = json.decodeFromString<SearchHistoryResult>(json.encodeToString(original))
        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun `Room round-trips through serialization`() {
        val original: SearchHistoryResult = SearchHistoryResult.Room(A_ROOM_ID)
        val decoded = json.decodeFromString<SearchHistoryResult>(json.encodeToString(original))
        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun `RoomId is serialized as its raw string value`() {
        val encoded = json.encodeToString<SearchHistoryResult>(SearchHistoryResult.Room(A_ROOM_ID))
        assertThat(encoded).contains(A_ROOM_ID.value)
    }

    @Test
    fun `a mixed list round-trips preserving order`() {
        val original = listOf(
            SearchHistoryResult.Query("first"),
            SearchHistoryResult.Room(A_ROOM_ID),
            SearchHistoryResult.Query("second"),
            SearchHistoryResult.Room(A_ROOM_ID_2),
        )
        val decoded = json.decodeFromString<List<SearchHistoryResult>>(json.encodeToString(original))
        assertThat(decoded).containsExactlyElementsIn(original).inOrder()
    }
}
