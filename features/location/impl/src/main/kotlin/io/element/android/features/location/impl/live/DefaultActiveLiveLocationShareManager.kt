/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.live

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.element.android.features.location.api.Location
import io.element.android.features.location.api.live.ActiveLiveLocationShare
import io.element.android.features.location.api.live.ActiveLiveLocationShareManager
import io.element.android.features.location.impl.live.service.LiveLocationReceiver
import io.element.android.features.location.impl.live.service.LiveLocationSharingCoordinator
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.services.appnavstate.api.ActiveRoomsHolder
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.Instant

@SingleIn(SessionScope::class)
@ContributesBinding(SessionScope::class, binding = binding<ActiveLiveLocationShareManager>())
class DefaultActiveLiveLocationShareManager(
    private val matrixClient: MatrixClient,
    private val activeRoomsHolder: ActiveRoomsHolder,
    private val coordinator: LiveLocationSharingCoordinator,
    private val clock: SystemClock,
) : ActiveLiveLocationShareManager, LiveLocationReceiver {

    private val _activeShares = MutableStateFlow<Map<RoomId, ActiveLiveLocationShare>>(emptyMap())
    override val activeShares: StateFlow<Map<RoomId, ActiveLiveLocationShare>> = _activeShares

    override suspend fun startShare(roomId: RoomId, duration: Duration): Result<Unit> {
        Timber.d("ActiveLiveLocationShareManager starting share for room $roomId with duration ${duration.inWholeSeconds}s")
        return runOnRoom(roomId) { room ->
            room.startLiveLocationShare(duration.inWholeMilliseconds)
        }.onSuccess {
            val wasEmpty = _activeShares.value.isEmpty()
            Timber.d("ActiveLiveLocationShareManager share started successfully for room $roomId (wasEmpty=$wasEmpty)")
            _activeShares.update {
                it + (roomId to ActiveLiveLocationShare(
                    sessionId = matrixClient.sessionId,
                    roomId = roomId,
                    expiresAt = Instant.fromEpochMilliseconds(clock.epochMillis() + duration.inWholeMilliseconds),
                ))
            }
            if (wasEmpty) {
                Timber.d("ActiveLiveLocationShareManager registering with coordinator for session ${matrixClient.sessionId}")
                coordinator.register(matrixClient.sessionId, this)
            }
        }.onFailure {
            Timber.e(it, "ActiveLiveLocationShareManager failed to start share for room $roomId")
        }
    }

    override suspend fun stopShare(roomId: RoomId): Result<Unit> {
        Timber.d("ActiveLiveLocationShareManager stopping share for room $roomId")
        return runOnRoom(roomId) { room ->
            room.stopLiveLocationShare()
        }.onSuccess {
            Timber.d("ActiveLiveLocationShareManager share stopped successfully for room $roomId")
            _activeShares.update { it - roomId }
            if (_activeShares.value.isEmpty()) {
                Timber.d("ActiveLiveLocationShareManager unregistering from coordinator for session ${matrixClient.sessionId}")
                coordinator.unregister(matrixClient.sessionId)
            }
        }.onFailure {
            Timber.e(it, "ActiveLiveLocationShareManager failed to stop share for room $roomId")
        }
    }

    override suspend fun onLocationUpdate(location: Location) {
        val activeSharesCount = _activeShares.value.size
        Timber.d("ActiveLiveLocationShareManager received location update for $activeSharesCount active share(s)")
        _activeShares.value.values.forEach { share ->
            Timber.d("ActiveLiveLocationShareManager sending location to room ${share.roomId}")
            runOnRoom(share.roomId) { room ->
                room.sendLiveLocation(location.toGeoUri())
            }.onFailure {
                Timber.e(it, "ActiveLiveLocationShareManager failed to send location to room ${share.roomId}")
            }
        }
    }

    private suspend fun runOnRoom(roomId: RoomId, block: suspend (JoinedRoom) -> Result<Unit>): Result<Unit> {
        val activeRoom = activeRoomsHolder.getActiveRoomMatching(matrixClient.sessionId, roomId)
        return if (activeRoom != null) {
            block(activeRoom)
        } else {
            val joinedRoom = matrixClient.getJoinedRoom(roomId)
            joinedRoom?.use { block(it) } ?: Result.failure(IllegalStateException("Room $roomId not found"))
        }
    }
}
