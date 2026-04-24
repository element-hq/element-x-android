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
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.location.LiveLocationException
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@OptIn(ExperimentalAtomicApi::class)
@SingleIn(SessionScope::class)
@ContributesBinding(SessionScope::class, binding = binding<ActiveLiveLocationShareManager>())
class DefaultActiveLiveLocationShareManager(
    private val matrixClient: MatrixClient,
    private val coordinator: LiveLocationSharingCoordinator,
    private val clock: SystemClock,
) : ActiveLiveLocationShareManager, LiveLocationReceiver {

    private val activeRooms = ConcurrentHashMap<RoomId, JoinedRoom>()
    private val syncedActiveShares = MutableStateFlow<Set<EventId>>(emptySet())
    private val localActiveShares = MutableStateFlow<Map<RoomId, ActiveLiveLocationShare>>(emptyMap())
    private val lastKnownLocation = AtomicReference<Location?>(null)
    override val activeShares: StateFlow<Map<RoomId, ActiveLiveLocationShare>> = localActiveShares

    init {
        matrixClient.ownBeaconInfoUpdates
            .onEach { update ->
                Timber.d("Received beaconInfoUpdate:$update")
                // First cancel the local share in this room if any.
                if (localActiveShares.value.contains(update.roomId)) {
                    stopLocalShare(roomId = update.roomId)
                }
                syncedActiveShares.update {
                    if (update.isLive) {
                        it + update.beaconId
                    } else {
                        it - update.beaconId
                    }
                }
            }
            .launchIn(matrixClient.sessionCoroutineScope)
    }

    override suspend fun startShare(roomId: RoomId, duration: Duration): Result<Unit> = withContext(NonCancellable) {
        Timber.d("ActiveLiveLocationShareManager starting share for room $roomId with duration ${duration.inWholeSeconds}s")
        val room = activeRooms.getOrPut(roomId) {
            matrixClient.getJoinedRoom(roomId) ?: return@withContext Result.failure(IllegalStateException("No room found for $roomId"))
        }
        // Stop the current live location share if any before starting one.
        room.stopLiveLocationShare()

        room.startLiveLocationShare(duration.inWholeMilliseconds)
            .onSuccess { beaconId ->
                Timber.d("ActiveLiveLocationShareManager wait remote echo of $beaconId")
                syncedActiveShares.first { beaconIds -> beaconIds.contains(beaconId) }
                startLocalShare(roomId, beaconId, duration)
            }.onFailure {
                Timber.e(it, "ActiveLiveLocationShareManager failed to start share for room $roomId")
            }
            .map { }
    }

    override suspend fun stopShare(roomId: RoomId): Result<Unit> = withContext(NonCancellable) {
        Timber.d("ActiveLiveLocationShareManager stopping share for room $roomId")
        val room = activeRooms.getOrPut(roomId) {
            matrixClient.getJoinedRoom(roomId) ?: return@withContext Result.failure(IllegalStateException("No room found for $roomId"))
        }
        room.stopLiveLocationShare()
            .onSuccess {
                Timber.d("ActiveLiveLocationShareManager share stopped successfully for room $roomId")
                stopLocalShare(roomId)
            }.onFailure {
                Timber.e(it, "ActiveLiveLocationShareManager failed to stop share for room $roomId")
            }
    }

    override suspend fun onLocationUpdate(location: Location) {
        val activeSharesCount = localActiveShares.value.size
        Timber.d("ActiveLiveLocationShareManager received location update for $activeSharesCount active share(s)")
        lastKnownLocation.store(location)
        localActiveShares.value.values.forEach { share ->
            Timber.d("ActiveLiveLocationShareManager sending location to room ${share.roomId}")
            sendLiveLocation(share.roomId, location).onFailure {
                Timber.e(it, "ActiveLiveLocationShareManager failed to send location to room ${share.roomId}")
            }
        }
    }

    private suspend fun sendLiveLocation(roomId: RoomId, location: Location, retryCount: Int = 3): Result<Unit> {
        val room = activeRooms.getOrPut(roomId) {
            matrixClient.getJoinedRoom(roomId) ?: return Result.failure(IllegalStateException("No room found for $roomId"))
        }
        return room.sendLiveLocation(location.toGeoUri())
            .recoverCatching { exception ->
                when (exception) {
                    is LiveLocationException.Network -> {
                        if (retryCount > 0) {
                            Timber.d("ActiveLiveLocationShareManager failed to send location to room $roomId, retry...")
                            delay(3.seconds)
                            sendLiveLocation(roomId, location, retryCount - 1).getOrThrow()
                        } else {
                            stopShare(roomId)
                            throw exception
                        }
                    }
                    else -> {
                        stopShare(roomId)
                        throw exception
                    }
                }
            }
    }

    private suspend fun startLocalShare(
        roomId: RoomId,
        beaconId: EventId,
        duration: Duration
    ) {
        val wasEmpty = localActiveShares.value.isEmpty()
        Timber.d("ActiveLiveLocationShareManager share started successfully for room $roomId (wasEmpty=$wasEmpty)")
        localActiveShares.update {
            it + (roomId to ActiveLiveLocationShare(
                beaconId = beaconId,
                roomId = roomId,
                expiresAt = Instant.fromEpochMilliseconds(clock.epochMillis() + duration.inWholeMilliseconds),
            ))
        }
        if (wasEmpty) {
            Timber.d("ActiveLiveLocationShareManager registering with coordinator for session ${matrixClient.sessionId}")
            coordinator.register(matrixClient.sessionId, this@DefaultActiveLiveLocationShareManager)
        }
        // Send the last received location if any
        lastKnownLocation.load()?.let { location ->
            sendLiveLocation(roomId, location)
        }
    }


    private fun stopLocalShare(roomId: RoomId) {
        Timber.d("ActiveLiveLocationShareManager stop local share in $roomId")
        localActiveShares.getAndUpdate { it - roomId }
        activeRooms.remove(roomId)?.close()
        if (localActiveShares.value.isEmpty()) {
            Timber.d("ActiveLiveLocationShareManager unregistering from coordinator for session ${matrixClient.sessionId}")
            coordinator.unregister(matrixClient.sessionId)
            lastKnownLocation.store(null)
        }
    }
}
