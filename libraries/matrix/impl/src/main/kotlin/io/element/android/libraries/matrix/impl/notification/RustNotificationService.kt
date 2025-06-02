/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.notification

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.notification.NotificationData
import io.element.android.libraries.matrix.api.notification.NotificationService
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.NotificationClient
import org.matrix.rustcomponents.sdk.NotificationItemsRequest
import timber.log.Timber

class RustNotificationService(
    private val sessionId: SessionId,
    private val notificationClient: NotificationClient,
    private val dispatchers: CoroutineDispatchers,
    clock: SystemClock,
) : NotificationService {
    private val notificationMapper: NotificationMapper = NotificationMapper(clock)

    override suspend fun getNotifications(
        ids: Map<RoomId, List<EventId>>
    ): Result<Map<EventId, NotificationData>> = withContext(dispatchers.io) {
        runCatchingExceptions {
            val requests = ids.map { (roomId, eventIds) ->
                NotificationItemsRequest(
                    roomId = roomId.value,
                    eventIds = eventIds.map { it.value }
                )
            }
            val items = notificationClient.getNotifications(requests)
            buildMap {
                val eventIds = requests.flatMap { it.eventIds }
                for (eventId in eventIds) {
                    val item = items[eventId]
                    if (item != null) {
                        val roomId = RoomId(requests.find { it.eventIds.contains(eventId) }?.roomId!!)
                        put(EventId(eventId), notificationMapper.map(sessionId, EventId(eventId), roomId, item))
                    } else {
                        Timber.e("Could not retrieve event for notification with $eventId")
                    }
                }
            }
        }
    }

    fun close() {
        notificationClient.close()
    }
}
