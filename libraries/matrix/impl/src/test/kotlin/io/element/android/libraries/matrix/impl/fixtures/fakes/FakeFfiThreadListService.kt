/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.TaskHandle
import org.matrix.rustcomponents.sdk.ThreadListEntriesListener
import org.matrix.rustcomponents.sdk.ThreadListItem
import org.matrix.rustcomponents.sdk.ThreadListPaginationStateListener
import org.matrix.rustcomponents.sdk.ThreadListService
import org.matrix.rustcomponents.sdk.ThreadListUpdate
import uniffi.matrix_sdk_ui.ThreadListPaginationState

class FakeFfiThreadListService(
    private val subscribeToItemsUpdates: (ThreadListEntriesListener) -> TaskHandle = { FakeFfiTaskHandle() },
    private val subscribeToPaginationStateUpdates: (ThreadListPaginationStateListener) -> TaskHandle = { FakeFfiTaskHandle() },
    private val items: () -> List<ThreadListItem> = { emptyList() },
    private val paginationState: () -> ThreadListPaginationState = { ThreadListPaginationState.Idle(endReached = false) },
    private val paginate: suspend () -> Unit = {},
    private val reset: suspend () -> Unit = {},
    private val destroy: () -> Unit = {},
) : ThreadListService(NoHandle) {
    private var itemsListener: ThreadListEntriesListener? = null
    private var paginationStateListener: ThreadListPaginationStateListener? = null

    override fun subscribeToItemsUpdates(listener: ThreadListEntriesListener): TaskHandle {
        itemsListener = listener
        return subscribeToItemsUpdates.invoke(listener)
    }

    override fun subscribeToPaginationStateUpdates(listener: ThreadListPaginationStateListener): TaskHandle {
        paginationStateListener = listener
        return subscribeToPaginationStateUpdates.invoke(listener)
    }

    override fun items(): List<ThreadListItem> = items.invoke()

    override fun paginationState(): ThreadListPaginationState = paginationState.invoke()

    override suspend fun paginate() = paginate.invoke()

    override suspend fun reset() = reset.invoke()

    override fun destroy() = destroy.invoke()

    fun emitUpdates(updates: List<ThreadListUpdate>) {
        itemsListener?.onUpdate(updates)
    }

    fun emitPaginationState(state: ThreadListPaginationState) {
        paginationStateListener?.onUpdate(state)
    }
}
