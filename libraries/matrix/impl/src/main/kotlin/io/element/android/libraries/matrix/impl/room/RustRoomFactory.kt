/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import androidx.collection.lruCache
import io.element.android.appconfig.TimelineConfig
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.RoomPreview
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.awaitLoaded
import io.element.android.libraries.matrix.impl.roomlist.fullRoomWithTimeline
import io.element.android.libraries.matrix.impl.roomlist.roomOrNull
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.RoomListException
import org.matrix.rustcomponents.sdk.RoomListItem
import timber.log.Timber
import org.matrix.rustcomponents.sdk.RoomListService as InnerRoomListService

private const val CACHE_SIZE = 16

class RustRoomFactory(
    private val sessionId: SessionId,
    private val deviceId: DeviceId,
    private val notificationSettingsService: NotificationSettingsService,
    private val sessionCoroutineScope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
    private val systemClock: SystemClock,
    private val roomContentForwarder: RoomContentForwarder,
    private val roomListService: RoomListService,
    private val innerRoomListService: InnerRoomListService,
    private val roomSyncSubscriber: RoomSyncSubscriber,
    private val timelineEventTypeFilterFactory: TimelineEventTypeFilterFactory,
    private val featureFlagService: FeatureFlagService,
    private val roomMembershipObserver: RoomMembershipObserver,
) {
    private val dispatcher = dispatchers.io.limitedParallelism(1)
    private val mutex = Mutex()
    private var isDestroyed: Boolean = false

    private data class RustRoomReferences(
        val roomListItem: RoomListItem,
        val fullRoom: Room,
    )

    private val cache = lruCache<RoomId, RustRoomReferences>(
        maxSize = CACHE_SIZE,
        onEntryRemoved = { evicted, roomId, oldRoom, _ ->
            Timber.d("On room removed from cache: $roomId, evicted: $evicted")
            oldRoom.roomListItem.close()
            oldRoom.fullRoom.close()
        }
    )

    private val matrixRoomInfoMapper = MatrixRoomInfoMapper()

    private val eventFilters = TimelineConfig.excludedEvents
        .takeIf { it.isNotEmpty() }
        ?.let { listStateEventType ->
            timelineEventTypeFilterFactory.create(listStateEventType)
        }

    suspend fun destroy() {
        withContext(NonCancellable + dispatcher) {
            mutex.withLock {
                Timber.d("Destroying room factory")
                cache.snapshot().values.forEach { (listItem, innerRoom) ->
                    innerRoom.destroy()
                    listItem.destroy()
                }
                cache.evictAll()
                isDestroyed = true
            }
        }
    }

    suspend fun create(roomId: RoomId): MatrixRoom? = withContext(dispatcher) {
        mutex.withLock {
            if (isDestroyed) {
                Timber.d("Room factory is destroyed, returning null for $roomId")
                return@withContext null
            }
            var roomReferences: RustRoomReferences? = getRoomReferences(roomId)
            if (roomReferences == null) {
                // ... otherwise, lets wait for the SS to load all rooms and check again.
                roomListService.allRooms.awaitLoaded()
                roomReferences = getRoomReferences(roomId)
            }
            if (roomReferences == null) {
                Timber.d("No room found for $roomId, returning null")
                return@withContext null
            }
            val liveTimeline = roomReferences.fullRoom.timeline()
            val initialRoomInfo = roomReferences.fullRoom.roomInfo()
            RustMatrixRoom(
                sessionId = sessionId,
                deviceId = deviceId,
                innerRoom = roomReferences.fullRoom,
                innerTimeline = liveTimeline,
                sessionCoroutineScope = sessionCoroutineScope,
                notificationSettingsService = notificationSettingsService,
                coroutineDispatchers = dispatchers,
                systemClock = systemClock,
                roomContentForwarder = roomContentForwarder,
                roomSyncSubscriber = roomSyncSubscriber,
                matrixRoomInfoMapper = matrixRoomInfoMapper,
                featureFlagService = featureFlagService,
                roomMembershipObserver = roomMembershipObserver,
                initialRoomInfo = matrixRoomInfoMapper.map(initialRoomInfo),
            )
        }
    }

    suspend fun createRoomPreview(roomId: RoomId): RoomPreview? = withContext(dispatcher) {
        if (isDestroyed) {
            Timber.d("Room factory is destroyed, returning null for $roomId")
            return@withContext null
        }
        val roomListItem = innerRoomListService.roomOrNull(roomId.value)
        if (roomListItem == null) {
            Timber.d("Room not found for $roomId")
            return@withContext null
        }
        if (roomListItem.membership() !in RustRoomPreview.ALLOWED_MEMBERSHIPS) {
            Timber.d("Room $roomId is not in allowed membership")
            return@withContext null
        }
        val innerRoom = try {
            roomListItem.previewRoom(via = emptyList())
        } catch (e: Exception) {
            Timber.e(e, "Failed to get room preview for $roomId")
            return@withContext null
        }
        RustRoomPreview(
            sessionId = sessionId,
            inner = innerRoom,
            roomMembershipObserver = roomMembershipObserver,
        )
    }

    private suspend fun getRoomReferences(roomId: RoomId): RustRoomReferences? {
        cache[roomId]?.let {
            Timber.d("Room found in cache for $roomId")
            return it
        }
        val roomListItem = innerRoomListService.roomOrNull(roomId.value)
        if (roomListItem == null) {
            Timber.d("Room not found for $roomId")
            return null
        }
        val fullRoom = try {
            roomListItem.fullRoomWithTimeline(filter = eventFilters)
        } catch (e: RoomListException) {
            Timber.e(e, "Failed to get full room with timeline for $roomId")
            return null
        }
        Timber.d("Got full room with timeline for $roomId")
        return RustRoomReferences(
            roomListItem = roomListItem,
            fullRoom = fullRoom,
        ).also {
            cache.put(roomId, it)
        }
    }
}
