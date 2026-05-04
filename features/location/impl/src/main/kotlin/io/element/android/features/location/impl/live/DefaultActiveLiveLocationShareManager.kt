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
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration
import kotlin.time.Instant

@OptIn(ExperimentalAtomicApi::class)
@SingleIn(SessionScope::class)
@ContributesBinding(SessionScope::class, binding = binding<ActiveLiveLocationShareManager>())
class DefaultActiveLiveLocationShareManager(
    private val matrixClient: MatrixClient,
    private val coordinator: LiveLocationSharingCoordinator,
    private val liveLocationStore: LiveLocationStore,
    private val clock: SystemClock,
) : ActiveLiveLocationShareManager, LiveLocationReceiver {

    private val isSetup = AtomicBoolean(false)
    private val activeRooms = ConcurrentHashMap<RoomId, JoinedRoom>()
    private val timeoutJobs = ConcurrentHashMap<RoomId, Job>()
    private val syncedActiveShareIds = MutableStateFlow<Set<EventId>>(emptySet())
    private val activeRoomIds = MutableStateFlow<Set<RoomId>>(emptySet())
    private val lastKnownLocation = AtomicReference<Location?>(null)
    override val activeShares: StateFlow<Set<RoomId>> = activeRoomIds

    override fun setup() {
        if (isSetup.compareAndSet(expectedValue = false, newValue = true)) {
            Timber.d("ActiveLiveLocationShareManager setup manager.")
            matrixClient.ownBeaconInfoUpdates
                .onEach { update ->
                    Timber.d("Received beaconInfoUpdate:$update")
                    // First cancel the local share in this room if any.
                    if (update.roomId in activeRoomIds.value) {
                        stopLocalShare(roomId = update.roomId)
                    }
                    syncedActiveShareIds.update {
                        if (update.isLive) {
                            it + update.beaconId
                        } else {
                            it - update.beaconId
                        }
                    }
                }
                .launchIn(matrixClient.sessionCoroutineScope)

            matrixClient.sessionCoroutineScope.launch {
                recoverPersistedShares()
            }
        }
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
                syncedActiveShareIds.first { beaconIds -> beaconIds.contains(beaconId) }
                val expiresAt = Instant.fromEpochMilliseconds(clock.epochMillis() + duration.inWholeMilliseconds)
                startLocalShare(roomId, expiresAt)
            }.onFailure {
                Timber.e(it, "ActiveLiveLocationShareManager failed to start share for room $roomId")
                stopLocalShare(roomId)
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
            }.onFailure {
                Timber.e(it, "ActiveLiveLocationShareManager failed to stop share for room $roomId")
            }.also {
                stopLocalShare(roomId)
            }
    }

    override suspend fun onLocationUpdate(location: Location) {
        val activeSharesCount = activeRoomIds.value.size
        Timber.d("ActiveLiveLocationShareManager received location update for $activeSharesCount active share(s)")
        lastKnownLocation.store(location)
        activeRoomIds.value.forEach { roomId ->
            Timber.d("ActiveLiveLocationShareManager sending location to room $roomId")
            sendLiveLocation(roomId, location).onFailure {
                Timber.e(it, "ActiveLiveLocationShareManager failed to send location to room $roomId")
            }
        }
    }

    private suspend fun sendLiveLocation(roomId: RoomId, location: Location): Result<Unit> {
        val room = activeRooms.getOrPut(roomId) {
            matrixClient.getJoinedRoom(roomId) ?: return Result.failure(IllegalStateException("No room found for $roomId"))
        }
        return room.sendLiveLocation(location.toGeoUri())
            .recoverCatching { exception ->
                when (exception) {
                    is LiveLocationException.NotLive -> {
                        stopLocalShare(roomId)
                        throw exception
                    }
                    else -> throw exception
                }
            }
    }

    private suspend fun startLocalShare(roomId: RoomId, expiresAt: Instant) {
        val wasEmpty = activeRoomIds.value.isEmpty()
        Timber.d("ActiveLiveLocationShareManager share started successfully for room $roomId (wasEmpty=$wasEmpty)")
        activeRoomIds.update { it + roomId }
        liveLocationStore.setLiveLocationExpiry(roomId, expiresAt)
        scheduleTimeout(roomId, expiresAt)
        if (wasEmpty) {
            Timber.d("ActiveLiveLocationShareManager registering with coordinator for session ${matrixClient.sessionId}")
            coordinator.register(matrixClient.sessionId, this@DefaultActiveLiveLocationShareManager)
        }
        // Send the last received location if any
        lastKnownLocation.load()?.let { location ->
            sendLiveLocation(roomId, location)
        }
    }

    private suspend fun recoverPersistedShares() {
        val now = Instant.fromEpochMilliseconds(clock.epochMillis())
        liveLocationStore.getLiveLocationExpiries().forEach { (roomId, expiresAt) ->
            if (expiresAt > now) {
                // Only starts locally as the share is already started remotely
                startLocalShare(roomId, expiresAt)
            } else {
                // Explicitly stop the share on the server.
                stopShare(roomId)
            }
        }
    }

    private fun scheduleTimeout(roomId: RoomId, expiresAt: Instant) {
        timeoutJobs.remove(roomId)?.cancel()
        val delayMillis = (expiresAt.toEpochMilliseconds() - clock.epochMillis())
        timeoutJobs[roomId] = matrixClient.sessionCoroutineScope.launch {
            delay(delayMillis)
            stopShare(roomId).onFailure { error ->
                Timber.e(error, "ActiveLiveLocationShareManager failed to stop timed out share for room $roomId")
            }
        }
    }

    private suspend fun stopLocalShare(roomId: RoomId) {
        Timber.d("ActiveLiveLocationShareManager stop local share in $roomId")
        timeoutJobs.remove(roomId)?.cancel()
        activeRooms.remove(roomId)?.close()
        activeRoomIds.getAndUpdate { it - roomId }
        liveLocationStore.removeLiveLocationExpiry(roomId)
        if (activeRoomIds.value.isEmpty()) {
            Timber.d("ActiveLiveLocationShareManager unregistering from coordinator for session ${matrixClient.sessionId}")
            coordinator.unregister(matrixClient.sessionId)
            lastKnownLocation.store(null)
        }
    }
}
