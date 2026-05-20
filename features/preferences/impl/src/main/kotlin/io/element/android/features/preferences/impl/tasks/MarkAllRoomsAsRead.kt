/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.tasks

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomlist.DynamicRoomList
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.api.roomlist.awaitLoaded
import io.element.android.libraries.push.api.notifications.NotificationCleaner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber

data class MarkAllRoomsAsReadResult(
    val processedCount: Int,
    val failedCount: Int,
)

interface MarkAllRoomsAsRead {
    suspend operator fun invoke(): Result<MarkAllRoomsAsReadResult>
}

@ContributesBinding(SessionScope::class)
@Inject
class DefaultMarkAllRoomsAsRead(
    private val client: MatrixClient,
    private val roomListService: RoomListService,
    private val markRoomAsRead: MarkRoomAsRead,
    private val notificationCleaner: NotificationCleaner,
    private val coroutineDispatchers: CoroutineDispatchers,
    @param:SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) : MarkAllRoomsAsRead {
    override suspend fun invoke(): Result<MarkAllRoomsAsReadResult> = withContext(coroutineDispatchers.io) {
        val unreadRoomList = createUnreadRoomList()
        val roomIds = unreadRoomList.collectAllUnreadRoomIds()
        if (roomIds.isEmpty()) {
            return@withContext Result.success(MarkAllRoomsAsReadResult(processedCount = 0, failedCount = 0))
        }

        var processedCount = 0
        var failedCount = 0
        for (roomId in roomIds) {
            markRoomAsRead(roomId)
                .onSuccess { processedCount++ }
                .onFailure { error ->
                    failedCount++
                    Timber.w(error, "Failed to mark room $roomId as read")
                }
        }
        notificationCleaner.clearAllMessagesEvents(client.sessionId)

        if (processedCount == 0 && failedCount > 0) {
            Result.failure(IllegalStateException("Failed to mark all rooms as read"))
        } else {
            Result.success(MarkAllRoomsAsReadResult(processedCount = processedCount, failedCount = failedCount))
        }
    }

    private fun createUnreadRoomList(): DynamicRoomList {
        return roomListService.createRoomList(
            pageSize = UNREAD_ROOM_LIST_PAGE_SIZE,
            source = RoomList.Source.All,
            coroutineScope = sessionCoroutineScope,
        )
    }

    private suspend fun DynamicRoomList.collectAllUnreadRoomIds(): Set<RoomId> {
        updateFilter(RoomListFilter.Unread)
        awaitLoaded()

        val roomIds = linkedSetOf<RoomId>()
        var previousSize = -1
        while (true) {
            roomIds.addAll(summaries.currentSummaries().map { it.roomId })

            val totalRooms = (loadingState.value as? RoomList.LoadingState.Loaded)?.numberOfRooms ?: roomIds.size
            if (roomIds.size >= totalRooms) {
                break
            }
            if (roomIds.size == previousSize) {
                break
            }
            previousSize = roomIds.size
            loadMore()
            summaries.first { list ->
                list.map { it.roomId }.toSet().size > previousSize || list.map { it.roomId }.toSet().size >= totalRooms
            }
        }
        return roomIds
    }

    private fun SharedFlow<List<RoomSummary>>.currentSummaries(): List<RoomSummary> {
        return (this as? StateFlow)?.value ?: replayCache.lastOrNull().orEmpty()
    }

    private companion object {
        const val UNREAD_ROOM_LIST_PAGE_SIZE = 50
    }
}
