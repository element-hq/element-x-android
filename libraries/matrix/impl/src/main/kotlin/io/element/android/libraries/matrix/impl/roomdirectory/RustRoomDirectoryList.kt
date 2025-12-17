/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomdirectory

import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.matrix.rustcomponents.sdk.RoomDirectorySearch
import kotlin.coroutines.CoroutineContext

class RustRoomDirectoryList(
    private val inner: RoomDirectorySearch,
    coroutineScope: CoroutineScope,
    private val coroutineContext: CoroutineContext,
) : RoomDirectoryList {
    private val hasMoreToLoad = MutableStateFlow(true)
    private val processor = RoomDirectorySearchProcessor(coroutineContext)

    init {
        launchIn(coroutineScope)
    }

    private fun launchIn(coroutineScope: CoroutineScope) {
        inner
            .resultsFlow()
            .onEach { updates ->
                processor.postUpdates(updates)
            }
            .flowOn(coroutineContext)
            .launchIn(coroutineScope)
    }

    override suspend fun filter(filter: String?, batchSize: Int, viaServerName: String?): Result<Unit> {
        return execute {
            inner.search(filter = filter, batchSize = batchSize.toUInt(), viaServerName = null)
        }
    }

    override suspend fun loadMore(): Result<Unit> {
        return execute {
            inner.nextPage()
        }
    }

    private suspend fun execute(action: suspend () -> Unit): Result<Unit> {
        return try {
            // We always assume there is more to load until we know there isn't.
            // As accessing hasMoreToLoad is otherwise blocked by the current action.
            hasMoreToLoad.value = true
            action()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            hasMoreToLoad.value = hasMoreToLoad()
        }
    }

    private suspend fun hasMoreToLoad(): Boolean {
        return !inner.isAtLastPage()
    }

    override val state: Flow<RoomDirectoryList.SearchResult> =
        combine(hasMoreToLoad, processor.roomDescriptionsFlow) { hasMoreToLoad, items ->
            RoomDirectoryList.SearchResult(
                hasMoreToLoad = hasMoreToLoad,
                items = items
            )
        }
            .flowOn(coroutineContext)
}
