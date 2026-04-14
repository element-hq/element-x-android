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
import io.element.android.libraries.matrix.impl.util.cancelAndDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.matrix.rustcomponents.sdk.ThreadListEntriesListener
import org.matrix.rustcomponents.sdk.ThreadListPaginationStateListener
import org.matrix.rustcomponents.sdk.ThreadListUpdate
import uniffi.matrix_sdk_ui.ThreadListPaginationState
import kotlin.apply
import kotlin.collections.plus
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
        val updatesFlow = callbackFlow {
            val handle = inner.subscribeToItemsUpdates(object : ThreadListEntriesListener {
                override fun onUpdate(diff: List<ThreadListUpdate>) {
                    trySend(diff)
                }
            })

            awaitClose {
                handle.cancelAndDestroy()
            }
        }

        return updatesFlow
            .onStart { items.value = inner.items().map { it.map(contentMapper) } }
            .onEach { diff ->
                items.value = diff.apply(items.value, contentMapper)
            }
            .launchIn(roomCoroutineScope)
    }

    override fun subscribeToPaginationUpdates(): Flow<ThreadListPaginationStatus> {
        return callbackFlow {
            val paginationHandle = inner.subscribeToPaginationStateUpdates(object : ThreadListPaginationStateListener {
                override fun onUpdate(state: ThreadListPaginationState) {
                    trySend(state.map())
                }
            })

            trySend(inner.paginationState().map())

            awaitClose {
                paginationHandle.cancelAndDestroy()
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
        if (itemSubscriptionJob?.isActive == true) {
            itemSubscriptionJob?.cancel()
        }
        inner.destroy()
    }
}

private fun List<ThreadListUpdate>.apply(
    current: List<ThreadListItem>,
    contentMapper: TimelineEventContentMapper
): List<ThreadListItem> {
    var items = current
    for (diffItem in this) {
        items = when (diffItem) {
            is ThreadListUpdate.Append -> {
                val newItems = diffItem.values.map { it.map(contentMapper) }
                items + newItems
            }
            ThreadListUpdate.Clear -> emptyList()
            is ThreadListUpdate.Insert -> {
                items.toMutableList().apply {
                    add(diffItem.index.toInt(), diffItem.value.map(contentMapper))
                }
            }
            ThreadListUpdate.PopBack -> {
                items.dropLast(1)
            }
            ThreadListUpdate.PopFront -> {
                items.drop(1)
            }
            is ThreadListUpdate.PushBack -> {
                items + diffItem.value.map(contentMapper)
            }
            is ThreadListUpdate.PushFront -> {
                listOf(diffItem.value.map(contentMapper)) + items
            }
            is ThreadListUpdate.Remove -> {
                items.toMutableList().apply {
                    removeAt(diffItem.index.toInt())
                }
            }
            is ThreadListUpdate.Reset -> {
                diffItem.values.map { it.map(contentMapper) }
            }
            is ThreadListUpdate.Set -> {
                items.toMutableList().apply {
                    set(diffItem.index.toInt(), diffItem.value.map(contentMapper))
                }
            }
            is ThreadListUpdate.Truncate -> {
                items.take(diffItem.length.toInt())
            }
        }
    }

    return items
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
