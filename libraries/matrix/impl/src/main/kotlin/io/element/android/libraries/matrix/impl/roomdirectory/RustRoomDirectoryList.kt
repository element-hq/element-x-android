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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import org.matrix.rustcomponents.sdk.RoomDirectorySearch as InnerRoomDirectorySearch

class RustRoomDirectoryList(
    private val inner: InnerRoomDirectorySearch,
    private val sessionCoroutineScope: CoroutineScope,
    private val sessionDispatcher: CoroutineDispatcher,
) : RoomDirectoryList {

    private val _items = MutableSharedFlow<List<RoomDescription>>()

    private val processor = RoomDirectorySearchProcessor(_items, sessionDispatcher, RoomDescriptionMapper())

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

    override suspend fun filter(filter: String?, batchSize: Int) {
        inner.search(filter, batchSize.toUInt())
    }

    override suspend fun loadMore() {
        inner.nextPage()
    }

    override suspend fun hasMoreToLoad(): Boolean {
        return !inner.isAtLastPage()
    }

    @OptIn(FlowPreview::class)
    override val items: Flow<List<RoomDescription>> = _items.debounce(200.milliseconds)
}
