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
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.location.BeaconId
import io.element.android.libraries.matrix.api.room.location.LiveLocationException
import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
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
    private val sessionObserver: SessionObserver,
) : ActiveLiveLocationShareManager, LiveLocationReceiver {
    private val isSetup = AtomicBoolean(false)
    private val cachedRooms = ConcurrentHashMap<RoomId, JoinedRoom>()
    private val timeoutJobs = ConcurrentHashMap<RoomId, Job>()
    private val syncedActiveShareIds = MutableStateFlow<Set<BeaconId>>(emptySet())
    private val localSharingRoomIds = MutableStateFlow<Set<RoomId>>(emptySet())
    override val sharingRoomIds: StateFlow<Set<RoomId>> = localSharingRoomIds

    override suspend fun setup() = withContext(NonCancellable) {
        if (isSetup.compareAndSet(expectedValue = false, newValue = true)) {
            Timber.d("ActiveLiveLocationShareManager setup manager.")

            recoverPersistedShares()

            matrixClient.ownBeaconInfoUpdates
                .onEach { update ->
                    Timber.d("Received beaconInfoUpdate:$update")
                    // First cancel the local share in this room if any.
                    if (update.roomId in localSharingRoomIds.value) {
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

            sessionObserver.addListener(object : SessionListener {
                override suspend fun onSessionDeleted(userId: String, wasLastSession: Boolean) {
                    if (matrixClient.sessionId.value == userId) {
                        clear()
                    }
                }
            })
        }
    }

    override suspend fun startShare(roomId: RoomId, duration: Duration): Result<Unit> = withContext(NonCancellable) {
        Timber.d("ActiveLiveLocationShareManager starting share for room $roomId with duration ${duration.inWholeSeconds}s")
        val room = cachedRooms.getOrPut(roomId) {
            matrixClient.getJoinedRoom(roomId) ?: return@withContext Result.failure(IllegalStateException("No room found for $roomId"))
        }
        // Before starting a new location share, stop the current one if any is active.
        room.stopLiveLocationShare()

        room.startLiveLocationShare(duration.inWholeMilliseconds)
            .onSuccess { beaconId ->
                Timber.d("ActiveLiveLocationShareManager wait remote echo of $beaconId")
                syncedActiveShareIds.first { beaconIds -> beaconIds.contains(beaconId) }
                val expiresAt = Instant.fromEpochMilliseconds(clock.epochMillis() + duration.inWholeMilliseconds)
                startLocalShare(roomId, expiresAt)
            }
            .onFailure {
                Timber.e(it, "ActiveLiveLocationShareManager failed to start share for room $roomId")
                stopLocalShare(roomId)
            }
            .map { }
    }

    override suspend fun stopShare(roomId: RoomId): Result<Unit> = withContext(NonCancellable) {
        Timber.d("ActiveLiveLocationShareManager stopping share for room $roomId")
        val room = cachedRooms.getOrPut(roomId) {
            matrixClient.getJoinedRoom(roomId) ?: return@withContext Result.failure(IllegalStateException("No room found for $roomId"))
        }
        room.stopLiveLocationShare()
            .onSuccess {
                Timber.d("ActiveLiveLocationShareManager share stopped successfully for room $roomId")
            }
            .onFailure {
                Timber.e(it, "ActiveLiveLocationShareManager failed to stop share for room $roomId")
            }
            .also {
                stopLocalShare(roomId)
            }
    }

    override suspend fun onLocationUpdate(location: Location) {
        val activeSharesCount = localSharingRoomIds.value.size
        Timber.d("ActiveLiveLocationShareManager received location update for $activeSharesCount active share(s)")
        localSharingRoomIds.value.forEach { roomId ->
            Timber.d("ActiveLiveLocationShareManager sending location to room $roomId")
            sendLiveLocation(roomId, location)
                .onFailure {
                    Timber.e(it, "ActiveLiveLocationShareManager failed to send location to room $roomId")
                }
        }
    }

    private suspend fun sendLiveLocation(roomId: RoomId, location: Location): Result<Unit> {
        val room = cachedRooms.getOrPut(roomId) {
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
        val wasEmpty = localSharingRoomIds.value.isEmpty()
        Timber.d("ActiveLiveLocationShareManager share started successfully for room $roomId (wasEmpty=$wasEmpty)")
        localSharingRoomIds.update { it + roomId }
        liveLocationStore.setLiveLocationExpiry(roomId, expiresAt)
        scheduleTimeout(roomId, expiresAt)
        if (wasEmpty) {
            Timber.d("ActiveLiveLocationShareManager registering with coordinator for session ${matrixClient.sessionId}")
            coordinator.register(matrixClient.sessionId, this@DefaultActiveLiveLocationShareManager)
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
        val delayMillis = expiresAt.toEpochMilliseconds() - clock.epochMillis()
        timeoutJobs[roomId] = matrixClient.sessionCoroutineScope.launch {
            delay(delayMillis)
            stopShare(roomId)
                .onFailure { error ->
                    Timber.e(error, "ActiveLiveLocationShareManager failed to stop timed out share for room $roomId")
                }
        }
    }

    private suspend fun stopLocalShare(roomId: RoomId) {
        Timber.d("ActiveLiveLocationShareManager stop local share in $roomId")
        timeoutJobs.remove(roomId)?.cancel()
        localSharingRoomIds.getAndUpdate { it - roomId }
        cachedRooms.remove(roomId)?.close()
        liveLocationStore.removeLiveLocationExpiry(roomId)
        if (localSharingRoomIds.value.isEmpty()) {
            Timber.d("ActiveLiveLocationShareManager unregistering from coordinator for session ${matrixClient.sessionId}")
            coordinator.unregister(matrixClient.sessionId)
        }
    }

    private suspend fun clear() {
        Timber.d("ActiveLiveLocationShareManager clear state")
        coordinator.unregister(matrixClient.sessionId)
        liveLocationStore.clear()
        for (room in cachedRooms.values) {
            room.close()
            timeoutJobs[room.roomId]?.cancel()
        }
        timeoutJobs.clear()
        cachedRooms.clear()
        localSharingRoomIds.value = emptySet()
        syncedActiveShareIds.value = emptySet()
    }
}
