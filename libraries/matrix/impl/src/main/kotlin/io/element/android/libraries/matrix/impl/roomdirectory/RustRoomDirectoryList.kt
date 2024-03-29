/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.matrix.impl.roomdirectory

import io.element.android.libraries.matrix.api.roomdirectory.RoomDescription
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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
    private val items = MutableSharedFlow<List<RoomDescription>>(replay = 1)
    private val processor = RoomDirectorySearchProcessor(items, coroutineContext, RoomDescriptionMapper())

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

    override suspend fun filter(filter: String?, batchSize: Int): Result<Unit> {
        return execute {
            inner.search(filter = filter, batchSize = batchSize.toUInt())
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

    override val state: Flow<RoomDirectoryList.State> =
        combine(hasMoreToLoad, items) { hasMoreToLoad, items ->
            RoomDirectoryList.State(
                hasMoreToLoad = hasMoreToLoad,
                items = items
            )
        }
            .flowOn(coroutineContext)
}
