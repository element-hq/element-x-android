/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.room.threads

import io.element.android.libraries.matrix.api.room.threads.ThreadListItem
import io.element.android.libraries.matrix.api.room.threads.ThreadListPaginationStatus
import io.element.android.libraries.matrix.api.room.threads.ThreadsListService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeThreadsListService(
    private val items: MutableStateFlow<List<ThreadListItem>> = MutableStateFlow(emptyList()),
    private val paginationStatus: MutableStateFlow<ThreadListPaginationStatus> = MutableStateFlow(ThreadListPaginationStatus.Idle(hasMoreToLoad = true)),
    private val subscribeToItemUpdates: () -> Flow<List<ThreadListItem>> = { items },
    private val subscribeToPaginationUpdates: () -> Flow<ThreadListPaginationStatus> = { paginationStatus },
    private val paginate: suspend () -> Result<Unit> = { Result.success(Unit) },
    private val reset: suspend () -> Result<Unit> = { Result.success(Unit) },
    private val destroy: () -> Unit = {},
) : ThreadsListService {
    override fun subscribeToItemUpdates(): Flow<List<ThreadListItem>> {
        return subscribeToItemUpdates.invoke()
    }

    override fun subscribeToPaginationUpdates(): Flow<ThreadListPaginationStatus> {
        return subscribeToPaginationUpdates.invoke()
    }

    override suspend fun paginate(): Result<Unit> {
        return paginate.invoke()
    }

    override suspend fun reset(): Result<Unit> {
        return reset.invoke()
    }

    override fun destroy() {
        return destroy.invoke()
    }

    suspend fun emit(items: List<ThreadListItem>) {
        this.items.emit(items)
    }
}
