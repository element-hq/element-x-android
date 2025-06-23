/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.coroutine.parallelMap
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.services.appnavstate.api.ActiveRoomsHolder
import io.element.android.services.appnavstate.api.AppForegroundStateService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class SyncOnNotifiableEvent @Inject constructor(
    private val matrixClientProvider: MatrixClientProvider,
    private val featureFlagService: FeatureFlagService,
    private val appForegroundStateService: AppForegroundStateService,
    private val dispatchers: CoroutineDispatchers,
    private val activeRoomsHolder: ActiveRoomsHolder,
) {
    suspend operator fun invoke(notifiableEvents: List<NotifiableEvent>) = withContext(dispatchers.io) {
        if (!featureFlagService.isFeatureEnabled(FeatureFlags.SyncOnPush)) {
            return@withContext
        }

        try {
            val eventsBySession = notifiableEvents.groupBy { it.sessionId }

            appForegroundStateService.updateIsSyncingNotificationEvent(true)

            for ((sessionId, events) in eventsBySession) {
                val client = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: continue
                val eventsByRoomId = events.groupBy { it.roomId }

                client.roomListService.subscribeToVisibleRooms(eventsByRoomId.keys.toList())

                if (!appForegroundStateService.isInForeground.value) {
                    for ((roomId, eventsInRoom) in eventsByRoomId) {
                        val activeRoom = activeRoomsHolder.getActiveRoomMatching(sessionId, roomId)
                        val room = activeRoom ?: client.getJoinedRoom(roomId)

                        if (room != null) {
                            eventsInRoom.parallelMap { event ->
                                room.waitsUntilEventIsKnown(event.eventId, timeout = 10.seconds)
                            }
                        }

                        if (room != null && activeRoom == null) {
                            // Destroy the room we just instantiated to reset its live timeline
                            room.destroy()
                        }
                    }
                }
            }
        } finally {
            appForegroundStateService.updateIsSyncingNotificationEvent(false)
        }
    }

    private suspend fun JoinedRoom.waitsUntilEventIsKnown(eventId: EventId, timeout: Duration) {
        withTimeoutOrNull(timeout) {
            liveTimeline.timelineItems.first { timelineItems ->
                timelineItems.any { timelineItem ->
                    when (timelineItem) {
                        is MatrixTimelineItem.Event -> timelineItem.eventId == eventId
                        else -> false
                    }
                }
            }
        }
    }
}
