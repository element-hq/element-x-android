/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search.history

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSearchHistoryStore : SearchHistoryStore {
    private val state = MutableStateFlow<List<SearchHistoryResult>>(emptyList())

    override val history: Flow<List<SearchHistoryResult>> = state

    override suspend fun add(result: SearchHistoryResult) {
        state.value = buildList {
            add(result)
            addAll(state.value.filterNot { it == result })
        }.take(MAX_SEARCH_HISTORY_SIZE)
    }

    override suspend fun clear() {
        state.value = emptyList()
    }
}
