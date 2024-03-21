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
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectorySearch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.RoomDirectorySearch as InnerRoomDirectorySearch

class RustRoomDirectorySearch(
    private val inner: InnerRoomDirectorySearch,
    private val sessionCoroutineScope: CoroutineScope,
    private val sessionDispatcher: CoroutineDispatcher,
) : RoomDirectorySearch {

    private val _results: MutableStateFlow<List<RoomDescription>> =
        MutableStateFlow(emptyList())

    private val processor = RoomDirectorySearchProcessor(_results, sessionDispatcher, RoomDescriptionMapper())

    init {
        sessionCoroutineScope.launch(sessionDispatcher) {
            inner
                .resultsFlow()
                .onEach { updates ->
                    processor.postUpdates(updates)
                }
                .launchIn(this)
        }
    }

    override suspend fun updateQuery(query: String?, batchSize: Int) {
        inner.search(query, batchSize.toUInt())
    }

    override suspend fun loadMore() {
        inner.nextPage()
    }

    override suspend fun hasMoreToLoad(): Boolean {
        return !inner.isAtLastPage()
    }

    override val results: SharedFlow<List<RoomDescription>> = _results
}
