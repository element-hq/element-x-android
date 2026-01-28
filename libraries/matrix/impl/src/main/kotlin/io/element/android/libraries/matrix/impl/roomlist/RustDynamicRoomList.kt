/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomlist

import io.element.android.libraries.matrix.api.roomlist.DynamicRoomList
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.matrix.rustcomponents.sdk.RoomListDynamicEntriesController

private const val DEFAULT_ADD_PAGES_COUNT = 3

internal class RustDynamicRoomList(
    override val summaries: MutableSharedFlow<List<RoomSummary>>,
    override val loadingState: MutableStateFlow<RoomList.LoadingState>,
    private val processor: RoomSummaryListProcessor,
    override val pageSize: Int,
    private val dynamicController: () -> RoomListDynamicEntriesController?,
    private val addPagesCount: Int = DEFAULT_ADD_PAGES_COUNT
) : DynamicRoomList {

    private val mutex = Mutex()

    override suspend fun rebuildSummaries() {
        processor.rebuildRoomSummaries()
    }

    override suspend fun updateFilter(filter: RoomListFilter) {
        mutex.withLock {
            dynamicController()?.let { controller ->
                // Reset pagination when filter changes
                controller.resetToOnePage()
                val rustFilter = RoomListFilterMapper.toRustFilter(filter)
                controller.setFilter(rustFilter)
                // Then preload some pages
                controller.addPages(addPagesCount)
            }
        }
    }

    override suspend fun loadMore() {
        mutex.withLock {
            dynamicController()?.addPages(addPagesCount)
        }
    }

    override suspend fun reset() {
        mutex.withLock {
            dynamicController()?.resetToOnePage()
        }
    }

    private fun RoomListDynamicEntriesController.addPages(pageCount: Int) = repeat(pageCount) { addOnePage() }
}
