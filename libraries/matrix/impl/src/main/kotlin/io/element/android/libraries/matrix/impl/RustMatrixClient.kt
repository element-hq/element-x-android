/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.matrix.impl

import io.element.android.libraries.androidutils.file.getSizeOfFiles
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.coroutine.childScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.createroom.CreateRoomParameters
import io.element.android.libraries.matrix.api.createroom.RoomPreset
import io.element.android.libraries.matrix.api.createroom.RoomVisibility
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.notification.NotificationService
import io.element.android.libraries.matrix.api.pusher.PushersService
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.RoomSummaryDataSource
import io.element.android.libraries.matrix.api.room.awaitAllRoomsAreLoaded
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.matrix.api.user.MatrixSearchUserResults
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.impl.core.toProgressWatcher
import io.element.android.libraries.matrix.impl.media.RustMediaLoader
import io.element.android.libraries.matrix.impl.notification.RustNotificationService
import io.element.android.libraries.matrix.impl.pushers.RustPushersService
import io.element.android.libraries.matrix.impl.room.RoomContentForwarder
import io.element.android.libraries.matrix.impl.room.RustMatrixRoom
import io.element.android.libraries.matrix.impl.room.RustRoomSummaryDataSource
import io.element.android.libraries.matrix.impl.room.roomOrNull
import io.element.android.libraries.matrix.impl.room.stateFlow
import io.element.android.libraries.matrix.impl.sync.RustSyncService
import io.element.android.libraries.matrix.impl.usersearch.UserProfileMapper
import io.element.android.libraries.matrix.impl.usersearch.UserSearchResultMapper
import io.element.android.libraries.matrix.impl.verification.RustSessionVerificationService
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientDelegate
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.RoomListItem
import org.matrix.rustcomponents.sdk.use
import timber.log.Timber
import java.io.File
import org.matrix.rustcomponents.sdk.CreateRoomParameters as RustCreateRoomParameters
import org.matrix.rustcomponents.sdk.RoomPreset as RustRoomPreset
import org.matrix.rustcomponents.sdk.RoomVisibility as RustRoomVisibility
import org.matrix.rustcomponents.sdk.SyncService as ClientSyncService

@OptIn(ExperimentalCoroutinesApi::class)
class RustMatrixClient constructor(
    private val client: Client,
    private val syncService: ClientSyncService,
    private val sessionStore: SessionStore,
    appCoroutineScope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
    private val baseDirectory: File,
    baseCacheDirectory: File,
    private val clock: SystemClock,
) : MatrixClient {

    override val sessionId: UserId = UserId(client.userId())
    private val roomListService = syncService.roomListService()
    private val sessionDispatcher = dispatchers.io.limitedParallelism(64)
    private val sessionCoroutineScope = appCoroutineScope.childScope(dispatchers.main, "Session-${sessionId}")
    private val verificationService = RustSessionVerificationService()
    private val rustSyncService = RustSyncService(syncService, roomListService.stateFlow(), sessionCoroutineScope)
    private val pushersService = RustPushersService(
        client = client,
        dispatchers = dispatchers,
    )
    private val notificationClient = client.notificationClient().use { builder ->
        builder.finish()
    }

    private val notificationService = RustNotificationService(notificationClient)

    private val clientDelegate = object : ClientDelegate {
        override fun didReceiveAuthError(isSoftLogout: Boolean) {
            //TODO handle this
            Timber.v("didReceiveAuthError(isSoftLogout=$isSoftLogout)")
        }
    }

    private val rustRoomSummaryDataSource: RustRoomSummaryDataSource =
        RustRoomSummaryDataSource(
            roomListService = roomListService,
            sessionCoroutineScope = sessionCoroutineScope,
            dispatcher = sessionDispatcher,
        )

    override val roomSummaryDataSource: RoomSummaryDataSource
        get() = rustRoomSummaryDataSource

    private val rustMediaLoader = RustMediaLoader(baseCacheDirectory, dispatchers, client)
    override val mediaLoader: MatrixMediaLoader
        get() = rustMediaLoader

    private val roomMembershipObserver = RoomMembershipObserver()

    private val roomContentForwarder = RoomContentForwarder(roomListService)

    init {
        client.setDelegate(clientDelegate)
        rustSyncService.syncState
            .onEach { syncState ->
                if (syncState == SyncState.Syncing) {
                    onSlidingSyncUpdate()
                }
            }.launchIn(sessionCoroutineScope)
    }

    override suspend fun getRoom(roomId: RoomId): MatrixRoom? {
        // Check if already in memory...
        var cachedPairOfRoom = pairOfRoom(roomId)
        if (cachedPairOfRoom == null) {
            //... otherwise, lets wait for the SS to load all rooms and check again.
            roomSummaryDataSource.awaitAllRoomsAreLoaded()
            cachedPairOfRoom = pairOfRoom(roomId)
        }
        if (cachedPairOfRoom == null) return null
        val (roomListItem, fullRoom) = cachedPairOfRoom
        return RustMatrixRoom(
            sessionId = sessionId,
            roomListItem = roomListItem,
            innerRoom = fullRoom,
            sessionCoroutineScope = sessionCoroutineScope,
            coroutineDispatchers = dispatchers,
            systemClock = clock,
            roomContentForwarder = roomContentForwarder,
            sessionData = sessionStore.getSession(sessionId.value)!!,
        )
    }

    private suspend fun pairOfRoom(roomId: RoomId): Pair<RoomListItem, Room>? = withContext(sessionDispatcher) {
        val cachedRoomListItem = roomListService.roomOrNull(roomId.value)
        val fullRoom = cachedRoomListItem?.fullRoom()
        if (cachedRoomListItem == null || fullRoom == null) {
            Timber.d("No room cached for $roomId")
            null
        } else {
            Timber.d("Found room cached for $roomId")
            Pair(cachedRoomListItem, fullRoom)
        }
    }

    override suspend fun findDM(userId: UserId): MatrixRoom? {
        val roomId = client.getDmRoom(userId.value)?.use { RoomId(it.id()) }
        return roomId?.let { getRoom(it) }
    }

    override suspend fun ignoreUser(userId: UserId): Result<Unit> = withContext(sessionDispatcher) {
        runCatching {
            client.ignoreUser(userId.value)
        }
    }

    override suspend fun unignoreUser(userId: UserId): Result<Unit> = withContext(sessionDispatcher) {
        runCatching {
            client.unignoreUser(userId.value)
        }
    }

    override suspend fun createRoom(createRoomParams: CreateRoomParameters): Result<RoomId> = withContext(sessionDispatcher) {
        runCatching {
            val rustParams = RustCreateRoomParameters(
                name = createRoomParams.name,
                topic = createRoomParams.topic,
                isEncrypted = createRoomParams.isEncrypted,
                isDirect = createRoomParams.isDirect,
                visibility = when (createRoomParams.visibility) {
                    RoomVisibility.PUBLIC -> RustRoomVisibility.PUBLIC
                    RoomVisibility.PRIVATE -> RustRoomVisibility.PRIVATE
                },
                preset = when (createRoomParams.preset) {
                    RoomPreset.PRIVATE_CHAT -> RustRoomPreset.PRIVATE_CHAT
                    RoomPreset.PUBLIC_CHAT -> RustRoomPreset.PUBLIC_CHAT
                    RoomPreset.TRUSTED_PRIVATE_CHAT -> RustRoomPreset.TRUSTED_PRIVATE_CHAT
                },
                invite = createRoomParams.invite?.map { it.value },
                avatar = createRoomParams.avatar,
            )
            val roomId = RoomId(client.createRoom(rustParams))

            // Wait to receive the room back from the sync
            withTimeout(30_000L) {
                roomSummaryDataSource.allRooms()
                    .filter { roomSummaries ->
                        roomSummaries.map { it.identifier() }.contains(roomId.value)
                    }.first()
            }
            roomId
        }
    }

    override suspend fun createDM(userId: UserId): Result<RoomId> {
        val createRoomParams = CreateRoomParameters(
            name = null,
            isEncrypted = true,
            isDirect = true,
            visibility = RoomVisibility.PRIVATE,
            preset = RoomPreset.TRUSTED_PRIVATE_CHAT,
            invite = listOf(userId)
        )
        return createRoom(createRoomParams)
    }

    override suspend fun getProfile(userId: UserId): Result<MatrixUser> = withContext(sessionDispatcher) {
        runCatching {
            client.getProfile(userId.value).let(UserProfileMapper::map)
        }
    }

    override suspend fun searchUsers(searchTerm: String, limit: Long): Result<MatrixSearchUserResults> =
        withContext(sessionDispatcher) {
            runCatching {
                client.searchUsers(searchTerm, limit.toULong()).let(UserSearchResultMapper::map)
            }
        }

    override fun syncService(): SyncService = rustSyncService

    override fun sessionVerificationService(): SessionVerificationService = verificationService

    override fun pushersService(): PushersService = pushersService

    override fun notificationService(): NotificationService = notificationService

    override fun close() {
        sessionCoroutineScope.cancel()
        client.setDelegate(null)
        verificationService.destroy()
        syncService.destroy()
        roomListService.destroy()
        notificationClient.destroy()
        client.destroy()
    }

    override suspend fun getCacheSize(): Long {
        // Do not use client.userId since it can throw if client has been closed (during clear cache)
        return baseDirectory.getCacheSize(userID = sessionId.value)
    }

    override suspend fun clearCache() {
        close()
        baseDirectory.deleteSessionDirectory(userID = sessionId.value, deleteCryptoDb = false)
    }

    override suspend fun logout() = withContext(sessionDispatcher) {
        try {
            client.logout()
        } catch (failure: Throwable) {
            Timber.e(failure, "Fail to call logout on HS. Still delete local files.")
        }
        close()
        baseDirectory.deleteSessionDirectory(userID = sessionId.value, deleteCryptoDb = true)
        sessionStore.removeSession(sessionId.value)
    }

    override suspend fun loadUserDisplayName(): Result<String> = withContext(sessionDispatcher) {
        runCatching {
            client.displayName()
        }
    }

    override suspend fun loadUserAvatarURLString(): Result<String?> = withContext(sessionDispatcher) {
        runCatching {
            client.avatarUrl()
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun uploadMedia(mimeType: String, data: ByteArray, progressCallback: ProgressCallback?): Result<String> = withContext(sessionDispatcher) {
        runCatching {
            client.uploadMedia(mimeType, data.toUByteArray().toList(), progressCallback?.toProgressWatcher())
        }
    }

    private fun onSlidingSyncUpdate() {
        if (!verificationService.isReady.value) {
            try {
                verificationService.verificationController = client.getSessionVerificationController()
            } catch (e: Throwable) {
                Timber.e(e, "Could not start verification service. Will try again on the next sliding sync update.")
            }
        }
    }

    override fun roomMembershipObserver(): RoomMembershipObserver = roomMembershipObserver

    private suspend fun File.getCacheSize(
        userID: String,
        includeCryptoDb: Boolean = false,
    ): Long = withContext(sessionDispatcher) {
        // Rust sanitises the user ID replacing invalid characters with an _
        val sanitisedUserID = userID.replace(":", "_")
        val sessionDirectory = File(this@getCacheSize, sanitisedUserID)
        if (includeCryptoDb) {
            sessionDirectory.getSizeOfFiles()
        } else {
            listOf(
                "matrix-sdk-state.sqlite3",
                "matrix-sdk-state.sqlite3-shm",
                "matrix-sdk-state.sqlite3-wal",
            ).map { fileName ->
                File(sessionDirectory, fileName)
            }.sumOf { file ->
                file.length()
            }
        }
    }

    private suspend fun File.deleteSessionDirectory(
        userID: String,
        deleteCryptoDb: Boolean = false,
    ): Boolean = withContext(sessionDispatcher) {
        // Rust sanitises the user ID replacing invalid characters with an _
        val sanitisedUserID = userID.replace(":", "_")
        val sessionDirectory = File(this@deleteSessionDirectory, sanitisedUserID)
        if (deleteCryptoDb) {
            // Delete the folder and all its content
            sessionDirectory.deleteRecursively()
        } else {
            // Delete only the state.db file
            sessionDirectory.listFiles().orEmpty()
                .filter { it.name.contains("matrix-sdk-state") }
                .forEach { file ->
                    Timber.w("Deleting file ${file.name}...")
                    file.safeDelete()
                }
            true
        }
    }
}

