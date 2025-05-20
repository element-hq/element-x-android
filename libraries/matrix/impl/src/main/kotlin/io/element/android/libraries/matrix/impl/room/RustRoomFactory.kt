/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.appconfig.TimelineConfig
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.awaitLoaded
import io.element.android.libraries.matrix.impl.room.preview.RoomPreviewInfoMapper
import io.element.android.libraries.matrix.impl.roomlist.fullRoomWithTimeline
import io.element.android.libraries.matrix.impl.roomlist.roomOrNull
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.Membership
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.RoomListItem
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import org.matrix.rustcomponents.sdk.RoomListService as InnerRoomListService

class RustRoomFactory(
    private val sessionId: SessionId,
    private val deviceId: DeviceId,
    private val innerClient: Client,
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
    private val isDestroyed: AtomicBoolean = AtomicBoolean(false)

    private val roomInfoMapper = RoomInfoMapper()

    private val eventFilters = TimelineConfig.excludedEvents
        .takeIf { it.isNotEmpty() }
        ?.let { listStateEventType ->
            timelineEventTypeFilterFactory.create(listStateEventType)
        }

    suspend fun destroy() {
        withContext(NonCancellable + dispatcher) {
            mutex.withLock {
                Timber.d("Destroying room factory")
                isDestroyed.set(true)
            }
        }
    }

    suspend fun getBaseRoom(roomId: RoomId): RustBaseRoom? = withContext(dispatcher) {
        mutex.withLock {
            if (isDestroyed.get()) {
                Timber.d("Room factory is destroyed, returning null for $roomId")
                return@withContext null
            }
            val roomListItem = awaitRoomListItem(roomId) ?: return@withContext null
            getBaseRoom(roomListItem)
        }
    }

    private suspend fun getBaseRoom(roomListItem: RoomListItem): RustBaseRoom? {
        val sdkRoom = innerClient.getRoom(roomListItem.id()) ?: return null
        return getBaseRoom(sdkRoom)
    }

    private suspend fun getBaseRoom(sdkRoom: Room): RustBaseRoom {
        val initialRoomInfo = sdkRoom.roomInfo()
        return RustBaseRoom(
            sessionId = sessionId,
            deviceId = deviceId,
            innerRoom = sdkRoom,
            coroutineDispatchers = dispatchers,
            roomSyncSubscriber = roomSyncSubscriber,
            roomMembershipObserver = roomMembershipObserver,
            roomInfoMapper = roomInfoMapper,
            initialRoomInfo = roomInfoMapper.map(initialRoomInfo),
            sessionCoroutineScope = sessionCoroutineScope,
        )
    }

    suspend fun getJoinedRoomOrPreview(roomId: RoomId): GetRoomResult? = withContext(dispatcher) {
        mutex.withLock {
            if (isDestroyed.get()) {
                Timber.d("Room factory is destroyed, returning null for $roomId")
                return@withContext null
            }
            val roomListItem = awaitRoomListItem(roomId) ?: return@withContext null

            if (roomListItem.membership() == Membership.JOINED) {
                // Init the live timeline in the SDK from the RoomListItem
                val sdkRoom = roomListItem.fullRoomWithTimeline(eventFilters)

                GetRoomResult.Joined(
                    JoinedRustRoom(
                        baseRoom = getBaseRoom(sdkRoom),
                        notificationSettingsService = notificationSettingsService,
                        roomContentForwarder = roomContentForwarder,
                        liveInnerTimeline = sdkRoom.timeline(),
                        coroutineDispatchers = dispatchers,
                        systemClock = systemClock,
                        featureFlagService = featureFlagService,
                    )
                )
            } else {
                val preview = try {
                    roomListItem.previewRoom(via = emptyList())
                } catch (e: Exception) {
                    Timber.e(e, "Failed to get room preview for $roomId")
                    return@withContext null
                }

                GetRoomResult.NotJoined(
                    NotJoinedRustRoom(
                        sessionId = sessionId,
                        localRoom = getBaseRoom(roomListItem),
                        previewInfo = RoomPreviewInfoMapper.map(preview.info()),
                    )
                )
            }
        }
    }

    /**
     * Get the Rust room list item for a room, retrying after the room list is loaded if necessary.
     */
    private suspend fun awaitRoomListItem(roomId: RoomId): RoomListItem? {
        var roomListItem = innerRoomListService.roomOrNull(roomId.value)
        if (roomListItem == null) {
            // ... otherwise, lets wait for the SS to load all rooms and check again.
            roomListService.allRooms.awaitLoaded()
            roomListItem = innerRoomListService.roomOrNull(roomId.value)
        }

        if (roomListItem == null) {
            Timber.d("Room not found for $roomId")
            return null
        }

        return roomListItem
    }
}

sealed interface GetRoomResult {
    data class Joined(val joinedRoom: JoinedRoom) : GetRoomResult
    data class NotJoined(val notJoinedRoom: NotJoinedRustRoom) : GetRoomResult

    val room: BaseRoom?
        get() = when (this) {
            is Joined -> joinedRoom
            is NotJoined -> notJoinedRoom.localRoom
        }
}
