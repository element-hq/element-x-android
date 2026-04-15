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
import io.element.android.libraries.matrix.api.room.threads.ThreadListItem
import io.element.android.libraries.matrix.api.room.threads.ThreadListItemEvent
import io.element.android.libraries.matrix.api.room.threads.ThreadListPaginationStatus
import io.element.android.libraries.matrix.api.room.threads.ThreadsListService
import io.element.android.libraries.matrix.impl.timeline.item.event.TimelineEventContentMapper
import io.element.android.libraries.matrix.impl.timeline.item.event.map
import io.element.android.libraries.matrix.impl.util.mxCallbackFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.matrix.rustcomponents.sdk.ThreadListEntriesListener
import org.matrix.rustcomponents.sdk.ThreadListPaginationStateListener
import org.matrix.rustcomponents.sdk.ThreadListUpdate
import uniffi.matrix_sdk_ui.ThreadListPaginationState
import org.matrix.rustcomponents.sdk.ThreadListService as InnerThreadListService

class RustThreadsListService(
    private val inner: InnerThreadListService,
    private val roomCoroutineScope: CoroutineScope,
    private val contentMapper: TimelineEventContentMapper = TimelineEventContentMapper(),
) : ThreadsListService {
    private var itemSubscriptionJob: Job? = null

    private val items = MutableStateFlow<List<ThreadListItem>>(emptyList())

    override fun subscribeToItemUpdates(): Flow<List<ThreadListItem>> {
        if (itemSubscriptionJob?.isActive != true) {
            itemSubscriptionJob = doSubscribeToItemUpdates()
        }

        return items
    }

    private fun doSubscribeToItemUpdates(): Job {
        val updatesFlow = mxCallbackFlow {
            inner.subscribeToItemsUpdates(object : ThreadListEntriesListener {
                override fun onUpdate(diff: List<ThreadListUpdate>) {
                    trySend(diff)
                }
            })
        }

        return updatesFlow
            .onStart { items.value = inner.items().map { it.map(contentMapper) } }
            .onEach { diff ->
                val updated = items.value.toMutableList()
                updated.apply(diff, contentMapper)
                items.value = updated
            }
            .launchIn(roomCoroutineScope)
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
        itemSubscriptionJob?.cancel()
        inner.destroy()
    }
}

private fun MutableList<ThreadListItem>.apply(
    diff: List<ThreadListUpdate>,
    contentMapper: TimelineEventContentMapper
) {
    for (diffItem in diff) {
        when (diffItem) {
            is ThreadListUpdate.Append -> {
                val newItems = diffItem.values.map { it.map(contentMapper) }
                addAll(newItems)
            }
            ThreadListUpdate.Clear -> clear()
            is ThreadListUpdate.Insert -> {
                add(diffItem.index.toInt(), diffItem.value.map(contentMapper))
            }
            ThreadListUpdate.PopBack -> {
                removeAt(lastIndex)
            }
            ThreadListUpdate.PopFront -> {
                removeAt(0)
            }
            is ThreadListUpdate.PushBack -> {
                add(diffItem.value.map(contentMapper))
            }
            is ThreadListUpdate.PushFront -> {
                add(0, diffItem.value.map(contentMapper))
            }
            is ThreadListUpdate.Remove -> {
                removeAt(diffItem.index.toInt())
            }
            is ThreadListUpdate.Reset -> {
                clear()
                addAll(diffItem.values.map { it.map(contentMapper) })
            }
            is ThreadListUpdate.Set -> {
                set(diffItem.index.toInt(), diffItem.value.map(contentMapper))
            }
            is ThreadListUpdate.Truncate -> {
                subList(diffItem.length.toInt(), size).clear()
            }
        }
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
