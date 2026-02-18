/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.workmanager

import androidx.work.Data
import androidx.work.workDataOf
import dev.zacsweers.metro.Inject
import io.element.android.libraries.androidutils.json.JsonProvider
import io.element.android.libraries.core.extensions.mapCatchingExceptions
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.push.GroupedNotificationEventRequest
import io.element.android.libraries.push.api.push.NotificationEventRequest
import timber.log.Timber

@Inject
class GroupedSyncNotificationsWorkerDataConverter(
    private val json: JsonProvider,
) {
    fun serialize(notificationEventRequests: List<NotificationEventRequest>): Result<List<Data>> {
        // First try to serialize all requests at once. In the vast majority of cases this will work.
        return serializeRequests(notificationEventRequests)
            .map { listOf(it) }
            .recoverCatching { t ->
                if (t is DataForWorkManagerIsTooBig) {
                    // Perform serialization on sublists, workDataOf have failed because of size limit
                    Timber.w(t, "Failed to serialize ${notificationEventRequests.size} notification requests, trying with a smaller chunk size.")
                    // Try serializing them again, with a smaller chunk size
                    listOfNotNull(serializeRequests(notificationEventRequests, CHUNK_SIZE / 2).getOrNull())
                } else {
                    throw t
                }
            }
    }

    private fun groupRequestsByChunks(notificationEventRequests: List<NotificationEventRequest>, size: Int): List<GroupedNotificationEventRequest> {
        val results = mutableListOf<GroupedNotificationEventRequest>()
        val requestsBySessionId = notificationEventRequests.withIndex().groupBy { (_, request) -> request.sessionId }
        val currentChunk = mutableListOf<NotificationEventRequest>()

        fun handleFullChunk(sessionId: SessionId, chunk: MutableList<NotificationEventRequest>, into: MutableList<GroupedNotificationEventRequest>) {
            val providerInfo = chunk.firstOrNull()?.providerInfo.orEmpty()
            val resultByRoomId = currentChunk
                .groupBy { it.roomId }
                .mapValues { (_, requests) -> requests.map { it.eventId } }
            into.add(GroupedNotificationEventRequest(sessionId, resultByRoomId, providerInfo))
            chunk.clear()
        }

        for ((sessionId, requests) in requestsBySessionId) {
            val byRoom = requests.groupBy { (_, request) -> request.roomId }

            for ((_, requestsByRoom) in byRoom) {
                var sortedRequests = requestsByRoom.sortedBy { it.index }.map { it.value }

                while (sortedRequests.isNotEmpty() && currentChunk.size < size) {
                    val pending = size - currentChunk.size
                    currentChunk.addAll(sortedRequests.take(pending))

                    if (currentChunk.size == size) {
                        handleFullChunk(sessionId, currentChunk, results)
                    }

                    sortedRequests = sortedRequests.drop(pending)
                }
            }

            // There was a partially filled chunk that wasn't handled
            if (currentChunk.isNotEmpty()) {
                handleFullChunk(sessionId, currentChunk, results)
            }
        }

        return results
    }

    private fun serializeRequests(notificationEventRequests: List<NotificationEventRequest>, chunkSize: Int = CHUNK_SIZE): Result<Data> {
        val grouped = groupRequestsByChunks(notificationEventRequests, chunkSize)
        return runCatchingExceptions { json().encodeToString(grouped.map { it.toData() }) }
            .onFailure {
                Timber.e(it, "Failed to serialize notification requests")
            }
            .mapCatchingExceptions { str ->
                // Note: workDataOf can fail if the data is too large
                try {
                    workDataOf(REQUESTS_KEY to str)
                } catch (_: IllegalStateException) {
                    throw DataForWorkManagerIsTooBig()
                }
            }
    }

    fun deserialize(data: Data): List<GroupedNotificationEventRequest>? {
        val rawRequestsJson = data.getString(REQUESTS_KEY) ?: return null
        return runCatchingExceptions {
            json().decodeFromString<List<SyncNotificationWorkManagerRequestFactory.GroupedData>>(rawRequestsJson).map { it.toRequest() }
        }.fold(
            onSuccess = {
                Timber.d("Deserialized ${it.size} requests")
                it
            },
            onFailure = {
                Timber.e(it, "Failed to deserialize notification requests")
                null
            }
        )
    }

    companion object {
        private const val REQUESTS_KEY = "requests"
        internal const val CHUNK_SIZE = 20
    }
}

private fun GroupedNotificationEventRequest.toData(): SyncNotificationWorkManagerRequestFactory.GroupedData {
    return SyncNotificationWorkManagerRequestFactory.GroupedData(
        sessionId = sessionId.value,
        eventsByRoom = requestsByRoom.map { (roomId, eventIds) -> roomId.toString() to eventIds.map { it.toString() } }.toMap(),
        providerInfo = providerInfo,
    )
}

private fun SyncNotificationWorkManagerRequestFactory.GroupedData.toRequest(): GroupedNotificationEventRequest {
    return GroupedNotificationEventRequest(
        sessionId = SessionId(sessionId),
        requestsByRoom = eventsByRoom.map { (roomId, eventIds) -> RoomId(roomId) to eventIds.map(::EventId) }.toMap(),
        providerInfo = providerInfo,
    )
}

private fun SyncNotificationWorkManagerRequestFactory.Data.toRequest(): NotificationEventRequest {
    return NotificationEventRequest(
        sessionId = SessionId(sessionId),
        roomId = RoomId(roomId),
        eventId = EventId(eventId),
        providerInfo = providerInfo,
    )
}
