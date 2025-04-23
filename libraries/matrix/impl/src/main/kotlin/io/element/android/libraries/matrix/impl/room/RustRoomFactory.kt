/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.appconfig.TimelineConfig
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.data.tryOrNull
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
import io.element.android.libraries.matrix.impl.roomlist.roomOrNull
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.Membership
import org.matrix.rustcomponents.sdk.RoomListItem
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import org.matrix.rustcomponents.sdk.Room as SdkRoom
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

    private data class RustRoomReferences(
        val roomListItem: RoomListItem,
        val room: SdkRoom,
    )

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
            val roomReferences = awaitRoomReferences(roomId) ?: return@withContext null
            getBaseRoom(roomReferences)
        }
    }

    private suspend fun getBaseRoom(roomReferences: RustRoomReferences): RustBaseRoom? {
        val initialRoomInfo = roomReferences.room.roomInfo()
        return RustBaseRoom(
            sessionId = sessionId,
            deviceId = deviceId,
            innerRoom = roomReferences.room,
            coroutineDispatchers = dispatchers,
            roomSyncSubscriber = roomSyncSubscriber,
            roomMembershipObserver = roomMembershipObserver,
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
            val roomReferences = awaitRoomReferences(roomId) ?: return@withContext null

            if (roomReferences.room.membership() == Membership.JOINED) {
                val baseRoom = getBaseRoom(roomReferences) ?: return@withContext null

                // Init the live timeline in the SDK from the RoomListItem
                if (!roomReferences.roomListItem.isTimelineInitialized()) {
                    roomReferences.roomListItem.initTimeline(eventFilters, "LIVE")
                }

                GetRoomResult.Joined(
                    JoinedRustRoom(
                        baseRoom = baseRoom,
                        notificationSettingsService = notificationSettingsService,
                        roomContentForwarder = roomContentForwarder,
                        liveInnerTimeline = roomReferences.room.timeline(),
                        coroutineDispatchers = dispatchers,
                        systemClock = systemClock,
                        roomInfoMapper = roomInfoMapper,
                        featureFlagService = featureFlagService,
                    )
                )
            } else {
                val preview = try {
                    roomReferences.roomListItem.previewRoom(via = emptyList())
                } catch (e: Exception) {
                    Timber.e(e, "Failed to get room preview for $roomId")
                    return@withContext null
                }

                GetRoomResult.NotJoined(
                    NotJoinedRustRoom(
                        sessionId = sessionId,
                        localRoom = getBaseRoom(roomReferences),
                        previewInfo = RoomPreviewInfoMapper.map(preview.info()),
                    )
                )
            }
        }
    }

    private fun getRoomReferences(roomId: RoomId): RustRoomReferences? {
        val roomListItem = innerRoomListService.roomOrNull(roomId.value)
        if (roomListItem == null) {
            Timber.d("Room not found for $roomId")
            return null
        }
        val room = tryOrNull {
            innerClient.getRoom(roomId.value)
        } ?: error("Failed to get room for room id: $roomId")

        Timber.d("Got room for $roomId")
        return RustRoomReferences(
            roomListItem = roomListItem,
            room = room,
        )
    }

    /**
     * Get the Rust room references for a room, retrying after the room list is loaded if necessary.
     */
    private suspend fun awaitRoomReferences(roomId: RoomId): RustRoomReferences? {
        var roomReferences = getRoomReferences(roomId)

        if (roomReferences == null) {
            // ... otherwise, lets wait for the SS to load all rooms and check again.
            roomListService.allRooms.awaitLoaded()
            roomReferences = getRoomReferences(roomId)
        }

        return roomReferences
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
