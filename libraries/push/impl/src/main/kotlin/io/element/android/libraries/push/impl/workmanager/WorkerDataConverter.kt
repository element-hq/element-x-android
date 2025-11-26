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
import io.element.android.libraries.push.api.push.NotificationEventRequest
import timber.log.Timber

@Inject
class WorkerDataConverter(
    private val json: JsonProvider,
) {
    fun serialize(notificationEventRequests: List<NotificationEventRequest>): Result<List<Data>> {
        // First try to serialize all requests at once. In the vast majority of cases this will work.
        return serializeRequests(notificationEventRequests)
            .map { listOf(it) }
            .recoverCatching { t ->
                if (t is DataForWorkManagerIsTooBig) {
                    // Perform serialization on sublists, workDataOf have failed because of size limit
                    Timber.w(t, "Failed to serialize ${notificationEventRequests.size} notification requests, split the requests per room.")
                    // Group the requests per rooms
                    val requestsSortedPerRoom = notificationEventRequests.groupBy { it.roomId }.values
                    // Build a list of sublist with size at most CHUNK_SIZE, and with all rooms kept together
                    buildList {
                        val currentChunk = mutableListOf<NotificationEventRequest>()
                        for (requests in requestsSortedPerRoom) {
                            if (currentChunk.size + requests.size <= CHUNK_SIZE) {
                                // Can add the whole room requests to the current chunk
                                currentChunk.addAll(requests)
                            } else {
                                // Add the current chunk
                                add(currentChunk.toList())
                                // Start a new chunk with the current room requests
                                currentChunk.clear()
                                // If a room has more requests than CHUNK_SIZE, we need to split them
                                requests.chunked(CHUNK_SIZE) { chunk ->
                                    if (chunk.size == CHUNK_SIZE) {
                                        add(chunk.toList())
                                    } else {
                                        currentChunk.addAll(chunk)
                                    }
                                }
                            }
                        }
                        // Add any remaining requests
                        add(currentChunk.toList())
                    }
                        .filter { it.isNotEmpty() }
                        .also {
                            Timber.d("Split notification requests into ${it.size} chunks for WorkManager serialization")
                            it.forEach { requests ->
                                Timber.d(" - Chunk with ${requests.size} requests")
                            }
                        }
                        .mapNotNull { serializeRequests(it).getOrNull() }
                } else {
                    throw t
                }
            }
    }

    private fun serializeRequests(notificationEventRequests: List<NotificationEventRequest>): Result<Data> {
        return runCatchingExceptions { json().encodeToString(notificationEventRequests.map { it.toData() }) }
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

    fun deserialize(data: Data): List<NotificationEventRequest>? {
        val rawRequestsJson = data.getString(REQUESTS_KEY) ?: return null
        return runCatchingExceptions {
            json().decodeFromString<List<SyncNotificationWorkManagerRequest.Data>>(rawRequestsJson).map { it.toRequest() }
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

private fun NotificationEventRequest.toData(): SyncNotificationWorkManagerRequest.Data {
    return SyncNotificationWorkManagerRequest.Data(
        sessionId = sessionId.value,
        roomId = roomId.value,
        eventId = eventId.value,
        providerInfo = providerInfo,
    )
}

private fun SyncNotificationWorkManagerRequest.Data.toRequest(): NotificationEventRequest {
    return NotificationEventRequest(
        sessionId = SessionId(sessionId),
        roomId = RoomId(roomId),
        eventId = EventId(eventId),
        providerInfo = providerInfo,
    )
}
