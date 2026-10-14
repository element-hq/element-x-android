/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.threads

import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.threads.ThreadListDiff
import io.element.android.libraries.matrix.api.room.threads.ThreadListItem
import io.element.android.libraries.matrix.api.room.threads.ThreadListItemEvent
import io.element.android.libraries.matrix.api.room.threads.ThreadListPaginationStatus
import io.element.android.libraries.matrix.api.room.threads.ThreadsListService
import io.element.android.libraries.matrix.impl.timeline.item.event.TimelineEventContentMapper
import io.element.android.libraries.matrix.impl.timeline.item.event.map
import io.element.android.libraries.matrix.impl.util.mxCallbackFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.matrix.rustcomponents.sdk.ThreadListEntriesListener
import org.matrix.rustcomponents.sdk.ThreadListPaginationStateListener
import org.matrix.rustcomponents.sdk.ThreadListUpdate
import uniffi.matrix_sdk_ui.ThreadListPaginationState
import org.matrix.rustcomponents.sdk.ThreadListService as InnerThreadListService

class RustThreadsListService(
    private val inner: InnerThreadListService,
    private val contentMapper: TimelineEventContentMapper = TimelineEventContentMapper(),
) : ThreadsListService {
    override fun subscribeToItemDiffs(): Flow<List<ThreadListDiff>> {
        return mxCallbackFlow {
            inner.subscribeToItemsUpdates(object : ThreadListEntriesListener {
                override fun onUpdate(diff: List<ThreadListUpdate>) {
                    trySend(diff)
                }
            })
        }.map { diffs ->
            diffs.map { it.toApiDiff(contentMapper) }
        }
    }

    override fun subscribeToPaginationUpdates(): Flow<ThreadListPaginationStatus> {
        return mxCallbackFlow {
            inner.subscribeToPaginationStateUpdates(object : ThreadListPaginationStateListener {
                override fun onUpdate(state: ThreadListPaginationState) {
                    trySend(state.map())
                }
            }).also {
                // Send the initial state
                trySend(inner.paginationState().map())
            }
        }
    }

    override suspend fun paginate(): Result<Unit> = runCatchingExceptions {
        inner.paginate()
    }

    override suspend fun reset(): Result<Unit> = runCatchingExceptions {
        inner.reset()
    }

    override fun destroy() {
        inner.destroy()
    }
}

private fun ThreadListUpdate.toApiDiff(contentMapper: TimelineEventContentMapper): ThreadListDiff {
    return when (this) {
        is ThreadListUpdate.Append -> ThreadListDiff.Append(
            values = values.map { it.map(contentMapper) }
        )
        is ThreadListUpdate.Clear -> ThreadListDiff.Clear
        is ThreadListUpdate.Insert -> ThreadListDiff.Insert(
            index = index.toInt(),
            value = value.map(contentMapper)
        )
        is ThreadListUpdate.PushBack -> ThreadListDiff.PushBack(
            value = value.map(contentMapper)
        )
        is ThreadListUpdate.PushFront -> ThreadListDiff.PushFront(
            value = value.map(contentMapper)
        )
        is ThreadListUpdate.PopBack -> ThreadListDiff.PopBack
        is ThreadListUpdate.PopFront -> ThreadListDiff.PopFront
        is ThreadListUpdate.Remove -> ThreadListDiff.Remove(
            index = index.toInt()
        )
        is ThreadListUpdate.Reset -> ThreadListDiff.Reset(
            values = values.map { it.map(contentMapper) }
        )
        is ThreadListUpdate.Set -> ThreadListDiff.Set(
            index = index.toInt(),
            value = value.map(contentMapper)
        )
        is ThreadListUpdate.Truncate -> ThreadListDiff.Truncate(
            length = length.toInt()
        )
    }
}

fun org.matrix.rustcomponents.sdk.ThreadListItem.map(contentMapper: TimelineEventContentMapper): ThreadListItem = ThreadListItem(
    rootEvent = rootEvent.map(contentMapper),
    latestEvent = latestEvent?.map(contentMapper),
    numberOfReplies = numReplies.toLong(),
)

fun org.matrix.rustcomponents.sdk.ThreadListItemEvent.map(contentMapper: TimelineEventContentMapper): ThreadListItemEvent = ThreadListItemEvent(
    eventId = EventId(eventId),
    senderId = UserId(sender),
    isOwn = isOwn,
    senderProfile = senderProfile.map(),
    content = content?.let(contentMapper::map),
    timestamp = timestamp.toLong(),
)

fun ThreadListPaginationState.map(): ThreadListPaginationStatus = when (this) {
    is ThreadListPaginationState.Idle -> ThreadListPaginationStatus.Idle(hasMoreToLoad = !endReached)
    ThreadListPaginationState.Loading -> ThreadListPaginationStatus.Loading
}
