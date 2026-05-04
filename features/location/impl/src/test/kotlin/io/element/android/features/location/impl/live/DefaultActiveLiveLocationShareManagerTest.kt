/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.live

import com.google.common.truth.Truth.assertThat
import io.element.android.features.location.impl.live.service.LiveLocationSharingCoordinator
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import io.element.android.libraries.matrix.api.room.location.BeaconInfoUpdate
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.preferences.test.FakePreferenceDataStoreFactory
import io.element.android.services.toolbox.api.systemclock.SystemClock
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultActiveLiveLocationShareManagerTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `starting the first share starts the coordinator service after the beacon echo and adds an active share`() = runTest {
        val startServiceRecorder = lambdaRecorder<Unit> { }
        val stopServiceRecorder = lambdaRecorder<Unit> { }
        val coordinator = createCoordinator(
            startService = startServiceRecorder,
            stopService = stopServiceRecorder
        )
        val beaconInfoUpdates = MutableSharedFlow<BeaconInfoUpdate>(replay = 1)
        val room = FakeJoinedRoom(
            startLiveLocationShareResult = { Result.success(AN_EVENT_ID) },
            stopLiveLocationShareResult = { Result.success(Unit) },
        )
        val manager = createManager(
            client = FakeMatrixClient(
                sessionId = A_SESSION_ID,
                sessionCoroutineScope = backgroundScope,
                ownBeaconInfoUpdates = beaconInfoUpdates,
            ).also { it.givenGetRoomResult(A_ROOM_ID, room) },
            coordinator = coordinator,
            clock = FakeSystemClock(epochMillisResult = 123L),
        )
        advanceUntilIdle()

        val result = async { manager.startShare(A_ROOM_ID, 60.minutes) }
        beaconInfoUpdates.emit(BeaconInfoUpdate(roomId = A_ROOM_ID, beaconId = AN_EVENT_ID, isLive = true))

        assertThat(result.await().isSuccess).isTrue()
        assertThat(manager.activeShares.value).containsExactly(A_ROOM_ID)
        assert(startServiceRecorder).isCalledOnce()
        assert(stopServiceRecorder).isNeverCalled()
    }

    @Test
    fun `stopping the last share stops the coordinator service`() = runTest {
        val startServiceRecorder = lambdaRecorder<Unit> { }
        val stopServiceRecorder = lambdaRecorder<Unit> { }
        val coordinator = createCoordinator(
            startService = startServiceRecorder,
            stopService = stopServiceRecorder
        )
        val beaconInfoUpdates = MutableSharedFlow<BeaconInfoUpdate>(replay = 1)
        val room = FakeJoinedRoom(
            startLiveLocationShareResult = { Result.success(AN_EVENT_ID) },
            stopLiveLocationShareResult = { Result.success(Unit) },
        )
        val manager = createManager(
            client = FakeMatrixClient(
                sessionId = A_SESSION_ID,
                sessionCoroutineScope = backgroundScope,
                ownBeaconInfoUpdates = beaconInfoUpdates,
            ).also { it.givenGetRoomResult(A_ROOM_ID, room) },
            coordinator = coordinator,
        )
        advanceUntilIdle()

        val startResult = async { manager.startShare(A_ROOM_ID, 15.minutes) }
        beaconInfoUpdates.emit(BeaconInfoUpdate(roomId = A_ROOM_ID, beaconId = AN_EVENT_ID, isLive = true))
        assertThat(startResult.await().isSuccess).isTrue()

        val result = manager.stopShare(A_ROOM_ID)

        assertThat(result.isSuccess).isTrue()
        assertThat(manager.activeShares.value).isEmpty()
        assert(startServiceRecorder).isCalledOnce()
        assert(stopServiceRecorder).isCalledOnce()
    }

    @Test
    fun `two managers with the same room id keep isolated state per session`() = runTest {
        val coordinator = createCoordinator()
        val beaconInfoUpdatesOne = MutableSharedFlow<BeaconInfoUpdate>(replay = 1)
        val beaconInfoUpdatesTwo = MutableSharedFlow<BeaconInfoUpdate>(replay = 1)
        val managerOne = createManager(
            client = FakeMatrixClient(
                sessionId = A_SESSION_ID,
                sessionCoroutineScope = backgroundScope,
                ownBeaconInfoUpdates = beaconInfoUpdatesOne,
            ).also {
                it.givenGetRoomResult(
                    A_ROOM_ID,
                    FakeJoinedRoom(
                        startLiveLocationShareResult = { Result.success(AN_EVENT_ID) },
                        stopLiveLocationShareResult = { Result.success(Unit) },
                    ),
                )
            },
            coordinator = coordinator,
        )
        val managerTwo = createManager(
            client = FakeMatrixClient(
                sessionId = A_SESSION_ID_2,
                sessionCoroutineScope = backgroundScope,
                ownBeaconInfoUpdates = beaconInfoUpdatesTwo,
            ).also {
                it.givenGetRoomResult(
                    A_ROOM_ID,
                    FakeJoinedRoom(
                        startLiveLocationShareResult = { Result.success(AN_EVENT_ID) },
                        stopLiveLocationShareResult = { Result.success(Unit) },
                    ),
                )
            },
            coordinator = coordinator,
        )
        advanceUntilIdle()

        val startResult = async { managerOne.startShare(A_ROOM_ID, 15.minutes) }
        beaconInfoUpdatesOne.emit(BeaconInfoUpdate(roomId = A_ROOM_ID, beaconId = AN_EVENT_ID, isLive = true))
        assertThat(startResult.await().isSuccess).isTrue()

        assertThat(managerOne.activeShares.value).containsExactly(A_ROOM_ID)
        assertThat(managerTwo.activeShares.value).isEmpty()
    }

    @Test
    fun `start share persists room expiry after beacon echo`() = runTest {
        val liveLocationStore = createLiveLocationStore()
        val coordinator = createCoordinator()
        val beaconInfoUpdates = MutableSharedFlow<BeaconInfoUpdate>(replay = 1)
        val manager = createManager(
            client = FakeMatrixClient(
                sessionId = A_SESSION_ID,
                sessionCoroutineScope = backgroundScope,
                ownBeaconInfoUpdates = beaconInfoUpdates,
            ).also {
                it.givenGetRoomResult(
                    A_ROOM_ID,
                    FakeJoinedRoom(
                        startLiveLocationShareResult = { Result.success(AN_EVENT_ID) },
                        stopLiveLocationShareResult = { Result.success(Unit) },
                    ),
                )
            },
            coordinator = coordinator,
            liveLocationStore = liveLocationStore,
            clock = FakeSystemClock(epochMillisResult = 123L),
        )
        advanceUntilIdle()

        val result = async { manager.startShare(A_ROOM_ID, 15.minutes) }
        beaconInfoUpdates.emit(BeaconInfoUpdate(roomId = A_ROOM_ID, beaconId = AN_EVENT_ID, isLive = true))

        assertThat(result.await().isSuccess).isTrue()
        assertThat(liveLocationStore.getLiveLocationExpiries()).containsKey(A_ROOM_ID)
    }

    @Test
    fun `stop share removes persisted expiry`() = runTest {
        val liveLocationStore = createLiveLocationStore()
        val coordinator = createCoordinator()
        val beaconInfoUpdates = MutableSharedFlow<BeaconInfoUpdate>(replay = 1)
        val manager = createManager(
            client = FakeMatrixClient(
                sessionId = A_SESSION_ID,
                sessionCoroutineScope = backgroundScope,
                ownBeaconInfoUpdates = beaconInfoUpdates,
            ).also {
                it.givenGetRoomResult(
                    A_ROOM_ID,
                    FakeJoinedRoom(
                        startLiveLocationShareResult = { Result.success(AN_EVENT_ID) },
                        stopLiveLocationShareResult = { Result.success(Unit) },
                    ),
                )
            },
            coordinator = coordinator,
            liveLocationStore = liveLocationStore,
        )
        advanceUntilIdle()

        val startResult = async { manager.startShare(A_ROOM_ID, 15.minutes) }
        beaconInfoUpdates.emit(BeaconInfoUpdate(roomId = A_ROOM_ID, beaconId = AN_EVENT_ID, isLive = true))
        assertThat(startResult.await().isSuccess).isTrue()

        manager.stopShare(A_ROOM_ID)

        assertThat(liveLocationStore.getLiveLocationExpiries()).doesNotContainKey(A_ROOM_ID)
    }

    @Test
    fun `start share stops remote share and fails when expiry persistence fails`() = runTest {
        var stopCount = 0
        val liveLocationStore = createFailingLiveLocationStore()
        val coordinator = createCoordinator()
        val beaconInfoUpdates = MutableSharedFlow<BeaconInfoUpdate>(replay = 1)
        val manager = createManager(
            client = FakeMatrixClient(
                sessionId = A_SESSION_ID,
                sessionCoroutineScope = backgroundScope,
                ownBeaconInfoUpdates = beaconInfoUpdates,
            ).also {
                it.givenGetRoomResult(
                    A_ROOM_ID,
                    FakeJoinedRoom(
                        startLiveLocationShareResult = { Result.success(AN_EVENT_ID) },
                        stopLiveLocationShareResult = {
                            stopCount++
                            Result.success(Unit)
                        },
                    ),
                )
            },
            coordinator = coordinator,
            liveLocationStore = liveLocationStore,
        )
        advanceUntilIdle()

        val result = async { manager.startShare(A_ROOM_ID, 15.minutes) }
        beaconInfoUpdates.emit(BeaconInfoUpdate(roomId = A_ROOM_ID, beaconId = AN_EVENT_ID, isLive = true))

        assertThat(result.await().isFailure).isTrue()
        assertThat(manager.activeShares.value).isEmpty()
        assertThat(stopCount).isEqualTo(2)
    }

    @Test
    fun `init restores unexpired stored share and registers coordinator`() = runTest {
        val startServiceRecorder = lambdaRecorder<Unit> { }
        val stopServiceRecorder = lambdaRecorder<Unit> { }
        val liveLocationStore = createLiveLocationStore().apply {
            setLiveLocationExpiry(A_ROOM_ID, kotlin.time.Instant.fromEpochMilliseconds(10_000L)).getOrThrow()
        }
        val manager = createManager(
            client = FakeMatrixClient(
                sessionId = A_SESSION_ID,
                sessionCoroutineScope = backgroundScope,
            ),
            coordinator = createCoordinator(
                startService = startServiceRecorder,
                stopService = stopServiceRecorder,
            ),
            liveLocationStore = liveLocationStore,
            clock = FakeSystemClock(epochMillisResult = 1_000L),
        )

        advanceUntilIdle()

        assertThat(manager.activeShares.value).containsExactly(A_ROOM_ID)
        assert(startServiceRecorder).isCalledOnce()
        assert(stopServiceRecorder).isNeverCalled()
    }

    @Test
    fun `init remotely stops expired stored share and removes it from store`() = runTest {
        var stopCount = 0
        val liveLocationStore = createLiveLocationStore().apply {
            setLiveLocationExpiry(A_ROOM_ID, kotlin.time.Instant.fromEpochMilliseconds(1_000L)).getOrThrow()
        }
        createManager(
            client = FakeMatrixClient(
                sessionId = A_SESSION_ID,
                sessionCoroutineScope = backgroundScope,
            ).also {
                it.givenGetRoomResult(
                    A_ROOM_ID,
                    FakeJoinedRoom(
                        stopLiveLocationShareResult = {
                            stopCount++
                            Result.success(Unit)
                        },
                    ),
                )
            },
            coordinator = createCoordinator(),
            liveLocationStore = liveLocationStore,
            clock = FakeSystemClock(epochMillisResult = 5_000L),
        )

        advanceUntilIdle()

        assertThat(stopCount).isEqualTo(1)
        assertThat(liveLocationStore.getLiveLocationExpiries()).isEmpty()
    }

    @Test
    fun `init keeps expired stored share when remote stop fails`() = runTest {
        val liveLocationStore = createLiveLocationStore().apply {
            setLiveLocationExpiry(A_ROOM_ID, kotlin.time.Instant.fromEpochMilliseconds(1_000L)).getOrThrow()
        }
        createManager(
            client = FakeMatrixClient(
                sessionId = A_SESSION_ID,
                sessionCoroutineScope = backgroundScope,
            ).also {
                it.givenGetRoomResult(
                    A_ROOM_ID,
                    FakeJoinedRoom(
                        stopLiveLocationShareResult = { Result.failure(IllegalStateException("boom")) },
                    ),
                )
            },
            coordinator = createCoordinator(),
            liveLocationStore = liveLocationStore,
            clock = FakeSystemClock(epochMillisResult = 5_000L),
        )

        advanceUntilIdle()

        assertThat(liveLocationStore.getLiveLocationExpiries()).containsKey(A_ROOM_ID)
    }

    @Test
    fun `share is automatically stopped when timeout elapses`() = runTest {
        var stopCount = 0
        val liveLocationStore = createInMemoryLiveLocationStore()
        val beaconInfoUpdates = MutableSharedFlow<BeaconInfoUpdate>(replay = 1)
        val sessionScope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val manager = createManager(
            client = FakeMatrixClient(
                sessionId = A_SESSION_ID,
                sessionCoroutineScope = sessionScope,
                ownBeaconInfoUpdates = beaconInfoUpdates,
            ).also {
                it.givenGetRoomResult(
                    A_ROOM_ID,
                    FakeJoinedRoom(
                        startLiveLocationShareResult = { Result.success(AN_EVENT_ID) },
                        stopLiveLocationShareResult = {
                            stopCount++
                            Result.success(Unit)
                        },
                    ),
                )
            },
            coordinator = createCoordinator(),
            liveLocationStore = liveLocationStore,
            clock = FakeSystemClock(epochMillisResult = 123L),
        )
        advanceUntilIdle()

        val startResult = async { manager.startShare(A_ROOM_ID, 1.minutes) }
        beaconInfoUpdates.emit(BeaconInfoUpdate(roomId = A_ROOM_ID, beaconId = AN_EVENT_ID, isLive = true))
        assertThat(startResult.await().isSuccess).isTrue()

        advanceTimeBy(1.minutes.inWholeMilliseconds)
        advanceUntilIdle()

        assertThat(manager.activeShares.value).isEmpty()
        assertThat(liveLocationStore.getLiveLocationExpiries()).doesNotContainKey(A_ROOM_ID)
        assertThat(stopCount).isEqualTo(2)
    }

    @Test
    fun `restored share is automatically stopped when remaining timeout elapses`() = runTest {
        var stopCount = 0
        val liveLocationStore = createInMemoryLiveLocationStore().apply {
            setLiveLocationExpiry(A_ROOM_ID, kotlin.time.Instant.fromEpochMilliseconds(6_000L)).getOrThrow()
        }
        val sessionScope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val manager = createManager(
            client = FakeMatrixClient(
                sessionId = A_SESSION_ID,
                sessionCoroutineScope = sessionScope,
            ).also {
                it.givenGetRoomResult(
                    A_ROOM_ID,
                    FakeJoinedRoom(
                        stopLiveLocationShareResult = {
                            stopCount++
                            Result.success(Unit)
                        },
                    ),
                )
            },
            coordinator = createCoordinator(),
            liveLocationStore = liveLocationStore,
            clock = FakeSystemClock(epochMillisResult = 1_000L),
        )

        assertThat(manager.activeShares.value).containsExactly(A_ROOM_ID)

        advanceTimeBy(5_000L)
        advanceUntilIdle()

        assertThat(manager.activeShares.value).isEmpty()
        assertThat(liveLocationStore.getLiveLocationExpiries()).doesNotContainKey(A_ROOM_ID)
        assertThat(stopCount).isEqualTo(1)
    }

    private fun createManager(
        client: FakeMatrixClient = FakeMatrixClient(sessionId = A_SESSION_ID),
        coordinator: LiveLocationSharingCoordinator = createCoordinator(),
        liveLocationStore: LiveLocationStore = createLiveLocationStore(),
        clock: SystemClock = FakeSystemClock(),
    ): DefaultActiveLiveLocationShareManager {
        return DefaultActiveLiveLocationShareManager(
            matrixClient = client,
            coordinator = coordinator,
            liveLocationStore = liveLocationStore,
            clock = clock,
        )
    }

    private fun createCoordinator(
        startService: () -> Unit = {},
        stopService: () -> Unit = {},
        nowMillis: () -> Long = { 0L },
    ): LiveLocationSharingCoordinator {
        return LiveLocationSharingCoordinator(
            startService = startService,
            stopService = stopService,
            nowMillis = nowMillis,
        )
    }

    private fun createLiveLocationStore(
        sessionId: io.element.android.libraries.matrix.api.core.SessionId = A_SESSION_ID,
        preferenceDataStoreFactory: PreferenceDataStoreFactory = FakePreferenceDataStoreFactory(),
    ): LiveLocationStore {
        return LiveLocationStore(
            preferenceDataStoreFactory = preferenceDataStoreFactory,
            sessionId = sessionId,
        )
    }

    private fun createFailingLiveLocationStore(
        sessionId: io.element.android.libraries.matrix.api.core.SessionId = A_SESSION_ID,
    ): LiveLocationStore {
        val failingPreferenceDataStoreFactory = object : PreferenceDataStoreFactory {
            override fun create(name: String): DataStore<Preferences> = object : DataStore<Preferences> {
                override val data: Flow<Preferences> = flowOf(emptyPreferences())

                override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
                    throw IllegalStateException("boom")
                }
            }
        }
        return createLiveLocationStore(
            sessionId = sessionId,
            preferenceDataStoreFactory = failingPreferenceDataStoreFactory,
        )
    }

    private fun createInMemoryLiveLocationStore(
        sessionId: io.element.android.libraries.matrix.api.core.SessionId = A_SESSION_ID,
    ): LiveLocationStore {
        val preferenceDataStoreFactory = object : PreferenceDataStoreFactory {
            override fun create(name: String): DataStore<Preferences> {
                var preferences: Preferences = emptyPreferences()
                return object : DataStore<Preferences> {
                    override val data: Flow<Preferences>
                        get() = flowOf(preferences)

                    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
                        preferences = transform(preferences)
                        return preferences
                    }
                }
            }
        }
        return createLiveLocationStore(
            sessionId = sessionId,
            preferenceDataStoreFactory = preferenceDataStoreFactory,
        )
    }
}
