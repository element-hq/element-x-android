/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.matrix.impl.roomlist

import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind
import timber.log.Timber

/**
 * Simple implementation of [RoomList] where state flows are provided through constructor.
 */
internal class RustRoomList(
    override val summaries: StateFlow<List<RoomSummary>>,
    override val loadingState: StateFlow<RoomList.LoadingState>,
    private val dynamicEvents: MutableSharedFlow<RoomListDynamicEvents>,
) : RoomList {

    override suspend fun updateFilter(filter: RoomList.Filter) {
        Timber.d("updateFilter($filter)")
        dynamicEvents.emit(RoomListDynamicEvents.SetFilter(filter.toRoomListEntriesDynamicFilterKind()))
    }

    override suspend fun loadMore() {
        Timber.d("loadMore()")
        dynamicEvents.emit(RoomListDynamicEvents.LoadMore)
    }

    override suspend fun reset() {
        Timber.d("reset()")
        dynamicEvents.emit(RoomListDynamicEvents.Reset)
    }

    private fun RoomList.Filter.toRoomListEntriesDynamicFilterKind(): RoomListEntriesDynamicFilterKind {
        return when (this) {
            RoomList.Filter.All -> RoomListEntriesDynamicFilterKind.All
            is RoomList.Filter.NormalizedMatchRoomName -> RoomListEntriesDynamicFilterKind.NormalizedMatchRoomName(pattern)
        }
    }
}
