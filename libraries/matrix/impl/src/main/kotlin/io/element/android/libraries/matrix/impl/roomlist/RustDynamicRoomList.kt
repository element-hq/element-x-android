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
    initialFilter: RoomListFilter,
    private val addPagesCount: Int = DEFAULT_ADD_PAGES_COUNT
) : DynamicRoomList {
    private val mutex = Mutex()

    // The controller is created asynchronously, once the entries of the room list are observed
    private var dynamicController: RoomListDynamicEntriesController? = null

    // The last requested filter, so that it can be applied when the controller is created
    private var currentFilter: RoomListFilter = initialFilter

    /**
     * To be called when the controller is created. The controller already has the initial filter set, so apply the filter
     * only if it has been updated in the meantime.
     */
    suspend fun onControllerCreated(controller: RoomListDynamicEntriesController, initialFilter: RoomListFilter) {
        mutex.withLock {
            dynamicController = controller
            if (currentFilter != initialFilter) {
                controller.applyFilter(currentFilter)
            }
        }
    }

    override suspend fun rebuildSummaries() {
        processor.rebuildRoomSummaries()
    }

    override suspend fun updateFilter(filter: RoomListFilter) {
        mutex.withLock {
            currentFilter = filter
            dynamicController?.applyFilter(filter)
        }
    }

    private fun RoomListDynamicEntriesController.applyFilter(filter: RoomListFilter) {
        // Reset pagination when filter changes
        resetToOnePage()
        setFilter(RoomListFilterMapper.toRustFilter(filter))
        // Then preload some pages
        addPages(addPagesCount)
    }

    override suspend fun loadMore() {
        mutex.withLock {
            dynamicController?.addPages(addPagesCount)
        }
    }

    override suspend fun reset() {
        mutex.withLock {
            dynamicController?.resetToOnePage()
        }
    }

    private fun RoomListDynamicEntriesController.addPages(pageCount: Int) = repeat(pageCount) { addOnePage() }
}
