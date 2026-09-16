/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search

import io.element.android.libraries.matrix.api.search.MessageSearchResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.SearchServiceResultsUpdate
import kotlin.coroutines.CoroutineContext

/**
 * Applies the SDK's diff stream to a local list of search results.
 *
 * The local list is kept strictly index-parallel to the SDK's: every result is mapped, nothing is
 * filtered or dropped, so the positional updates ([SearchServiceResultsUpdate.Insert],
 * [SearchServiceResultsUpdate.Set], [SearchServiceResultsUpdate.Remove],
 * [SearchServiceResultsUpdate.Truncate]) always address the slot the SDK meant. Filtering for
 * display, if ever needed, belongs at render time — not here.
 */
class MessageSearchResultsProcessor(
    private val results: MutableSharedFlow<List<MessageSearchResult>>,
    private val coroutineContext: CoroutineContext,
    private val mapper: MessageSearchResultMapper = MessageSearchResultMapper(),
) {
    private val mutex = Mutex()

    suspend fun postUpdates(updates: List<SearchServiceResultsUpdate>) = withContext(coroutineContext) {
        mutex.withLock {
            val current = results.replayCache.lastOrNull().orEmpty().toMutableList()
            updates.forEach { update ->
                current.applyUpdate(update)
            }
            results.emit(current)
        }
    }

    private fun MutableList<MessageSearchResult>.applyUpdate(update: SearchServiceResultsUpdate) {
        // Exhaustive on purpose, with no `else`: a future SDK bump adding a variant must fail
        // compilation rather than silently dropping updates and corrupting the list.
        when (update) {
            is SearchServiceResultsUpdate.Append -> {
                addAll(update.values.map(mapper::map))
            }
            is SearchServiceResultsUpdate.PushBack -> {
                add(mapper.map(update.value))
            }
            is SearchServiceResultsUpdate.PushFront -> {
                add(0, mapper.map(update.value))
            }
            is SearchServiceResultsUpdate.Set -> {
                this[update.index.toInt()] = mapper.map(update.value)
            }
            is SearchServiceResultsUpdate.Insert -> {
                add(update.index.toInt(), mapper.map(update.value))
            }
            is SearchServiceResultsUpdate.Remove -> {
                removeAt(update.index.toInt())
            }
            is SearchServiceResultsUpdate.Truncate -> {
                subList(update.length.toInt(), size).clear()
            }
            is SearchServiceResultsUpdate.Reset -> {
                clear()
                addAll(update.values.map(mapper::map))
            }
            SearchServiceResultsUpdate.PopBack -> {
                removeLastOrNull()
            }
            SearchServiceResultsUpdate.PopFront -> {
                removeFirstOrNull()
            }
            SearchServiceResultsUpdate.Clear -> {
                clear()
            }
        }
    }
}
