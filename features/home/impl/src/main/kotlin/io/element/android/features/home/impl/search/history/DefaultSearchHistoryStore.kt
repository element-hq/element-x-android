/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search.history

import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.androidutils.json.JsonProvider
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.SessionScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber

@ContributesBinding(SessionScope::class)
class DefaultSearchHistoryStore(
    private val fileStore: SearchHistoryFileStore,
    private val dispatchers: CoroutineDispatchers,
    private val jsonProvider: JsonProvider,
) : SearchHistoryStore {
    private val json by lazy { jsonProvider() }

    // Guards [fileStore] access and the lazy initialization of [cache].
    private val mutex = Mutex()
    private val cache = MutableStateFlow<List<SearchHistoryResult>>(emptyList())
    private var isLoaded = false

    override val history: Flow<List<SearchHistoryResult>> = flow {
        ensureLoaded()
        emitAll(cache)
    }

    override suspend fun add(result: SearchHistoryResult) = withContext(dispatchers.io) {
        mutex.withLock {
            ensureLoadedLocked()
            val updated = buildList {
                add(result)
                // Deduplicate: an equal entry is moved to the front rather than being duplicated.
                addAll(cache.value.filterNot { it == result })
            }
                .distinct()
                .take(MAX_SEARCH_HISTORY_SIZE)
            cache.value = updated
            persist(updated)
        }
    }

    override suspend fun clear() = withContext<Unit>(dispatchers.io) {
        mutex.withLock {
            isLoaded = true
            cache.value = emptyList()
            runCatchingExceptions { fileStore.delete() }
                .onFailure { Timber.e(it, "Failed to delete search history") }
        }
    }

    private suspend fun ensureLoaded() = withContext(dispatchers.io) {
        mutex.withLock {
            ensureLoadedLocked()
        }
    }

    /**
     * Must be called while holding [mutex].
     */
    private fun ensureLoadedLocked() {
        if (isLoaded) return
        cache.value = readFromDisk()
        isLoaded = true
    }

    private fun readFromDisk(): List<SearchHistoryResult> {
        return runCatchingExceptions {
            val bytes = fileStore.read() ?: return@runCatchingExceptions emptyList()
            json.decodeFromString<List<SearchHistoryResult>>(bytes.decodeToString())
        }.getOrElse {
            Timber.e(it, "Failed to read search history, resetting it")
            emptyList()
        }
    }

    private fun persist(items: List<SearchHistoryResult>) {
        runCatchingExceptions {
            fileStore.write(json.encodeToString(items).encodeToByteArray())
        }.onFailure { Timber.e(it, "Failed to persist search history") }
    }
}
