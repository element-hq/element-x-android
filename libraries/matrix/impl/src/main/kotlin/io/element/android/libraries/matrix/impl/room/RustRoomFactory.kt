/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.appconfig.TimelineConfig
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.awaitLoaded
import io.element.android.libraries.matrix.impl.roomlist.fullRoomWithTimeline
import io.element.android.libraries.matrix.impl.roomlist.roomOrNull
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.FilterTimelineEventType
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.RoomListException
import org.matrix.rustcomponents.sdk.RoomListItem
import org.matrix.rustcomponents.sdk.TimelineEventTypeFilter
import timber.log.Timber
import org.matrix.rustcomponents.sdk.RoomListService as InnerRoomListService

class RustRoomFactory(
    private val sessionId: SessionId,
    private val notificationSettingsService: NotificationSettingsService,
    private val sessionCoroutineScope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
    private val systemClock: SystemClock,
    private val roomContentForwarder: RoomContentForwarder,
    private val roomListService: RoomListService,
    private val innerRoomListService: InnerRoomListService,
    private val isKeyBackupEnabled: suspend () -> Boolean,
    private val getSessionData: suspend () -> SessionData,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val createRoomDispatcher = dispatchers.io.limitedParallelism(1)
    private val mutex = Mutex()

    private val matrixRoomInfoMapper = MatrixRoomInfoMapper()

    private val roomSyncSubscriber: RoomSyncSubscriber = RoomSyncSubscriber(innerRoomListService, dispatchers)

    private val eventFilters = TimelineConfig.excludedEvents
        .takeIf { it.isNotEmpty() }
        ?.let { listStateEventType ->
            TimelineEventTypeFilter.exclude(
                listStateEventType.map { stateEventType ->
                    FilterTimelineEventType.State(stateEventType.map())
                }
            )
        }

    suspend fun create(roomId: RoomId): MatrixRoom? = withContext(createRoomDispatcher) {
        var cachedPairOfRoom: Pair<RoomListItem, Room>?
        mutex.withLock {
            // Check if already in memory...
            cachedPairOfRoom = pairOfRoom(roomId)
            if (cachedPairOfRoom == null) {
                // ... otherwise, lets wait for the SS to load all rooms and check again.
                roomListService.allRooms.awaitLoaded()
                cachedPairOfRoom = pairOfRoom(roomId)
            }
        }
        if (cachedPairOfRoom == null) {
            Timber.d("No room found for $roomId")
            return@withContext null
        }
        cachedPairOfRoom?.let { (roomListItem, fullRoom) ->
            RustMatrixRoom(
                sessionId = sessionId,
                isKeyBackupEnabled = isKeyBackupEnabled(),
                roomListItem = roomListItem,
                innerRoom = fullRoom,
                innerTimeline = fullRoom.timeline(),
                notificationSettingsService = notificationSettingsService,
                sessionCoroutineScope = sessionCoroutineScope,
                coroutineDispatchers = dispatchers,
                systemClock = systemClock,
                roomContentForwarder = roomContentForwarder,
                sessionData = getSessionData(),
                roomSyncSubscriber = roomSyncSubscriber,
                matrixRoomInfoMapper = matrixRoomInfoMapper,
            )
        }
    }

    private suspend fun pairOfRoom(roomId: RoomId): Pair<RoomListItem, Room>? {
        val cachedRoomListItem = innerRoomListService.roomOrNull(roomId.value)
        val fullRoom = try {
            cachedRoomListItem?.fullRoomWithTimeline(filter = eventFilters)
        } catch (e: RoomListException) {
            Timber.e(e, "Failed to get full room with timeline for $roomId")
            null
        }
        return if (cachedRoomListItem == null || fullRoom == null) {
            Timber.d("No room cached for $roomId")
            null
        } else {
            Timber.d("Found room cached for $roomId")
            Pair(cachedRoomListItem, fullRoom)
        }
    }
}
