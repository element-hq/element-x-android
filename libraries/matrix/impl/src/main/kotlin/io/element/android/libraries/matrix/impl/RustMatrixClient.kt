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
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.createroom.CreateRoomParameters
import io.element.android.libraries.matrix.api.createroom.RoomPreset
import io.element.android.libraries.matrix.api.createroom.RoomVisibility
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.notification.NotificationService
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.oidc.AccountManagementAction
import io.element.android.libraries.matrix.api.pusher.PushersService
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias
import io.element.android.libraries.matrix.api.room.preview.RoomPreview
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryService
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.matrix.api.user.MatrixSearchUserResults
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.impl.core.toProgressWatcher
import io.element.android.libraries.matrix.impl.encryption.RustEncryptionService
import io.element.android.libraries.matrix.impl.mapper.toSessionData
import io.element.android.libraries.matrix.impl.media.RustMediaLoader
import io.element.android.libraries.matrix.impl.notification.RustNotificationService
import io.element.android.libraries.matrix.impl.notificationsettings.RustNotificationSettingsService
import io.element.android.libraries.matrix.impl.oidc.toRustAction
import io.element.android.libraries.matrix.impl.pushers.RustPushersService
import io.element.android.libraries.matrix.impl.room.RoomContentForwarder
import io.element.android.libraries.matrix.impl.room.RustRoomFactory
import io.element.android.libraries.matrix.impl.room.preview.RoomPreviewMapper
import io.element.android.libraries.matrix.impl.roomdirectory.RustRoomDirectoryService
import io.element.android.libraries.matrix.impl.roomlist.RoomListFactory
import io.element.android.libraries.matrix.impl.roomlist.RustRoomListService
import io.element.android.libraries.matrix.impl.sync.RustSyncService
import io.element.android.libraries.matrix.impl.usersearch.UserProfileMapper
import io.element.android.libraries.matrix.impl.usersearch.UserSearchResultMapper
import io.element.android.libraries.matrix.impl.util.SessionDirectoryNameProvider
import io.element.android.libraries.matrix.impl.util.cancelAndDestroy
import io.element.android.libraries.matrix.impl.util.mxCallbackFlow
import io.element.android.libraries.matrix.impl.verification.RustSessionVerificationService
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.matrix.rustcomponents.sdk.BackupState
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientDelegate
import org.matrix.rustcomponents.sdk.IgnoredUsersListener
import org.matrix.rustcomponents.sdk.NotificationProcessSetup
import org.matrix.rustcomponents.sdk.PowerLevels
import org.matrix.rustcomponents.sdk.TaskHandle
import org.matrix.rustcomponents.sdk.use
import timber.log.Timber
import java.io.File
import java.util.Optional
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.INFINITE
import kotlin.time.Duration.Companion.seconds
import org.matrix.rustcomponents.sdk.CreateRoomParameters as RustCreateRoomParameters
import org.matrix.rustcomponents.sdk.RoomPreset as RustRoomPreset
import org.matrix.rustcomponents.sdk.RoomVisibility as RustRoomVisibility
import org.matrix.rustcomponents.sdk.SyncService as ClientSyncService

@OptIn(ExperimentalCoroutinesApi::class)
class RustMatrixClient(
    private val client: Client,
    private val syncService: ClientSyncService,
    private val sessionStore: SessionStore,
    private val appCoroutineScope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
    private val baseDirectory: File,
    baseCacheDirectory: File,
    private val clock: SystemClock,
) : MatrixClient {
    override val sessionId: UserId = UserId(client.userId())
    override val deviceId: String = client.deviceId()
    override val sessionCoroutineScope = appCoroutineScope.childScope(dispatchers.main, "Session-$sessionId")

    private val innerRoomListService = syncService.roomListService()
    private val sessionDispatcher = dispatchers.io.limitedParallelism(64)
    private val rustSyncService = RustSyncService(syncService, sessionCoroutineScope)
    private val pushersService = RustPushersService(
        client = client,
        dispatchers = dispatchers,
    )
    private val notificationProcessSetup = NotificationProcessSetup.SingleProcess(syncService)
    private val notificationClient = runBlocking { client.notificationClient(notificationProcessSetup) }
    private val notificationService = RustNotificationService(sessionId, notificationClient, dispatchers, clock)
    private val notificationSettingsService = RustNotificationSettingsService(client, dispatchers)
        .apply { start() }
    private val encryptionService = RustEncryptionService(
        client = client,
        syncService = rustSyncService,
        sessionCoroutineScope = sessionCoroutineScope,
        dispatchers = dispatchers,
    )

    private val roomDirectoryService = RustRoomDirectoryService(
        client = client,
        sessionDispatcher = sessionDispatcher,
    )

    private val sessionDirectoryNameProvider = SessionDirectoryNameProvider()

    private val isLoggingOut = AtomicBoolean(false)

    private val clientDelegate = object : ClientDelegate {
        override fun didReceiveAuthError(isSoftLogout: Boolean) {
            Timber.w("didReceiveAuthError(isSoftLogout=$isSoftLogout)")
            if (isLoggingOut.getAndSet(true).not()) {
                Timber.v("didReceiveAuthError -> do the cleanup")
                // TODO handle isSoftLogout parameter.
                appCoroutineScope.launch {
                    val existingData = sessionStore.getSession(client.userId())
                    val anonymizedToken = existingData?.accessToken?.takeLast(4)
                    Timber.d("Removing session data with token: '...$anonymizedToken'.")
                    if (existingData != null) {
                        // Set isTokenValid to false
                        val newData = client.session().toSessionData(
                            isTokenValid = false,
                            loginType = existingData.loginType,
                            passphrase = existingData.passphrase,
                        )
                        sessionStore.updateData(newData)
                        Timber.d("Removed session data with token: '...$anonymizedToken'.")
                    } else {
                        Timber.d("No session data found.")
                    }
                    doLogout(doRequest = false, removeSession = false, ignoreSdkError = false)
                }.invokeOnCompletion {
                    if (it != null) {
                        Timber.e(it, "Failed to remove session data.")
                    }
                }
            } else {
                Timber.v("didReceiveAuthError -> already cleaning up")
            }
        }

        override fun didRefreshTokens() {
            Timber.w("didRefreshTokens()")
            appCoroutineScope.launch {
                val existingData = sessionStore.getSession(client.userId()) ?: return@launch
                val anonymizedToken = client.session().accessToken.takeLast(4)
                Timber.d("Saving new session data with token: '...$anonymizedToken'. Was token valid: ${existingData.isTokenValid}")
                val newData = client.session().toSessionData(
                    isTokenValid = true,
                    loginType = existingData.loginType,
                    passphrase = existingData.passphrase,
                )
                sessionStore.updateData(newData)
                Timber.d("Saved new session data with token: '...$anonymizedToken'.")
            }.invokeOnCompletion {
                if (it != null) {
                    Timber.e(it, "Failed to save new session data.")
                }
            }
        }
    }

    override val roomListService: RoomListService = RustRoomListService(
        innerRoomListService = innerRoomListService,
        sessionCoroutineScope = sessionCoroutineScope,
        sessionDispatcher = sessionDispatcher,
        roomListFactory = RoomListFactory(
            innerRoomListService = innerRoomListService,
            sessionCoroutineScope = sessionCoroutineScope,
        ),
    )

    private val verificationService = RustSessionVerificationService(
        client = client,
        isSyncServiceReady = rustSyncService.syncState.map { it == SyncState.Running },
        sessionCoroutineScope = sessionCoroutineScope,
    )

    private val roomFactory = RustRoomFactory(
        roomListService = roomListService,
        innerRoomListService = innerRoomListService,
        sessionId = sessionId,
        notificationSettingsService = notificationSettingsService,
        sessionCoroutineScope = sessionCoroutineScope,
        dispatchers = dispatchers,
        systemClock = clock,
        roomContentForwarder = RoomContentForwarder(innerRoomListService),
        isKeyBackupEnabled = { client.encryption().use { it.backupState() == BackupState.ENABLED } },
        getSessionData = { sessionStore.getSession(sessionId.value)!! },
    )

    override val mediaLoader: MatrixMediaLoader = RustMediaLoader(
        baseCacheDirectory = baseCacheDirectory,
        dispatchers = dispatchers,
        innerClient = client,
    )

    private val roomMembershipObserver = RoomMembershipObserver()

    private val clientDelegateTaskHandle: TaskHandle? = client.setDelegate(clientDelegate)

    private val _userProfile: MutableStateFlow<MatrixUser> = MutableStateFlow(
        MatrixUser(
            userId = sessionId,
            // TODO cache for displayName?
            displayName = null,
            avatarUrl = client.cachedAvatarUrl(),
        )
    )

    override val userProfile: StateFlow<MatrixUser> = _userProfile

    override val ignoredUsersFlow = mxCallbackFlow<ImmutableList<UserId>> {
        client.subscribeToIgnoredUsers(object : IgnoredUsersListener {
            override fun call(ignoredUserIds: List<String>) {
                channel.trySend(ignoredUserIds.map(::UserId).toPersistentList())
            }
        })
    }
        .buffer(Channel.UNLIMITED)
        .stateIn(sessionCoroutineScope, started = SharingStarted.Eagerly, initialValue = persistentListOf())

    init {
        sessionCoroutineScope.launch {
            // Force a refresh of the profile
            getUserProfile()
        }
    }

    override suspend fun getRoom(roomId: RoomId): MatrixRoom? {
        return roomFactory.create(roomId)
    }

    /**
     * Wait for the room to be available in the room list.
     * @param roomId the room id to wait for
     * @param timeout the timeout to wait for the room to be available
     * @throws TimeoutCancellationException if the room is not available after the timeout
     */
    private suspend fun awaitRoom(roomId: RoomId, timeout: Duration) {
        withTimeout(timeout) {
            roomListService.allRooms.summaries
                .filter { roomSummaries ->
                    roomSummaries.map { it.identifier() }.contains(roomId.value)
                }
                .first()
        }
    }

    override suspend fun findDM(userId: UserId): RoomId? {
        return client.getDmRoom(userId.value)?.use { RoomId(it.id()) }
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
                powerLevelContentOverride = defaultRoomCreationPowerLevels,
            )
            val roomId = RoomId(client.createRoom(rustParams))
            // Wait to receive the room back from the sync but do not returns failure if it fails.
            try {
                awaitRoom(roomId, 30.seconds)
            } catch (e: Exception) {
                Timber.e(e, "Timeout waiting for the room to be available in the room list")
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
            invite = listOf(userId),
        )
        return createRoom(createRoomParams)
    }

    override suspend fun getProfile(userId: UserId): Result<MatrixUser> = withContext(sessionDispatcher) {
        runCatching {
            client.getProfile(userId.value).let(UserProfileMapper::map)
        }
    }

    override suspend fun getUserProfile(): Result<MatrixUser> = getProfile(sessionId)
        .onSuccess { _userProfile.tryEmit(it) }

    override suspend fun searchUsers(searchTerm: String, limit: Long): Result<MatrixSearchUserResults> =
        withContext(sessionDispatcher) {
            runCatching {
                client.searchUsers(searchTerm, limit.toULong()).let(UserSearchResultMapper::map)
            }
        }

    override suspend fun setDisplayName(displayName: String): Result<Unit> =
        withContext(sessionDispatcher) {
            runCatching { client.setDisplayName(displayName) }
        }

    override suspend fun uploadAvatar(mimeType: String, data: ByteArray): Result<Unit> =
        withContext(sessionDispatcher) {
            runCatching { client.uploadAvatar(mimeType, data) }
        }

    override suspend fun removeAvatar(): Result<Unit> =
        withContext(sessionDispatcher) {
            runCatching { client.removeAvatar() }
        }

    override suspend fun joinRoom(roomId: RoomId): Result<Unit> = withContext(sessionDispatcher) {
        runCatching {
            client.joinRoomById(roomId.value).destroy()
            try {
                awaitRoom(roomId, 10.seconds)
            } catch (e: Exception) {
                Timber.e(e, "Timeout waiting for the room to be available in the room list")
            }
        }
    }

    override suspend fun joinRoomByIdOrAlias(
        roomId: RoomId,
        serverNames: List<String>,
    ): Result<Unit> = withContext(sessionDispatcher) {
        runCatching {
            client.joinRoomByIdOrAlias(
                roomIdOrAlias = roomId.value,
                serverNames = serverNames,
            ).destroy()
            try {
                awaitRoom(roomId, 10.seconds)
            } catch (e: Exception) {
                Timber.e(e, "Timeout waiting for the room to be available in the room list")
            }
        }
    }

    override suspend fun knockRoom(roomId: RoomId): Result<Unit> {
        return Result.failure(NotImplementedError("Not yet implemented"))
    }

    override suspend fun trackRecentlyVisitedRoom(roomId: RoomId): Result<Unit> = withContext(sessionDispatcher) {
        runCatching {
            client.trackRecentlyVisitedRoom(roomId.value)
        }
    }

    override suspend fun getRecentlyVisitedRooms(): Result<List<RoomId>> = withContext(sessionDispatcher) {
        runCatching {
            client.getRecentlyVisitedRooms().map(::RoomId)
        }
    }

    override suspend fun resolveRoomAlias(roomAlias: RoomAlias): Result<ResolvedRoomAlias> = withContext(sessionDispatcher) {
        runCatching {
            val result = client.resolveRoomAlias(roomAlias.value)
            ResolvedRoomAlias(
                roomId = RoomId(result.roomId),
                servers = result.servers,
            )
        }
    }

    override suspend fun getRoomPreviewFromRoomId(roomId: RoomId, serverNames: List<String>): Result<RoomPreview> = withContext(sessionDispatcher) {
        runCatching {
            client.getRoomPreviewFromRoomId(
                roomId = roomId.value,
                viaServers = serverNames,
            ).let(RoomPreviewMapper::map)
        }
    }

    override fun syncService(): SyncService = rustSyncService

    override fun sessionVerificationService(): SessionVerificationService = verificationService

    override fun pushersService(): PushersService = pushersService

    override fun notificationService(): NotificationService = notificationService

    override fun encryptionService(): EncryptionService = encryptionService

    override fun notificationSettingsService(): NotificationSettingsService = notificationSettingsService

    override fun roomDirectoryService(): RoomDirectoryService = roomDirectoryService

    override fun close() {
        sessionCoroutineScope.cancel()
        clientDelegateTaskHandle?.cancelAndDestroy()
        notificationSettingsService.destroy()
        verificationService.destroy()
        syncService.destroy()
        innerRoomListService.destroy()
        notificationClient.destroy()
        notificationProcessSetup.destroy()
        encryptionService.destroy()
        client.destroy()
    }

    override suspend fun getCacheSize(): Long {
        return baseDirectory.getCacheSize()
    }

    override suspend fun clearCache() {
        close()
        baseDirectory.deleteSessionDirectory(deleteCryptoDb = false)
    }

    override suspend fun logout(ignoreSdkError: Boolean): String? = doLogout(
        doRequest = true,
        removeSession = true,
        ignoreSdkError = ignoreSdkError,
    )

    private suspend fun doLogout(
        doRequest: Boolean,
        removeSession: Boolean,
        ignoreSdkError: Boolean,
    ): String? {
        var result: String? = null
        syncService.stop()
        withContext(sessionDispatcher) {
            if (doRequest) {
                try {
                    result = client.logout()
                } catch (failure: Throwable) {
                    if (ignoreSdkError) {
                        Timber.e(failure, "Fail to call logout on HS. Still delete local files.")
                    } else {
                        Timber.e(failure, "Fail to call logout on HS.")
                        throw failure
                    }
                }
            }
            close()
            baseDirectory.deleteSessionDirectory(deleteCryptoDb = true)
            if (removeSession) {
                sessionStore.removeSession(sessionId.value)
            }
        }
        return result
    }

    override suspend fun getAccountManagementUrl(action: AccountManagementAction?): Result<String?> = withContext(sessionDispatcher) {
        val rustAction = action?.toRustAction()
        runCatching {
            client.accountUrl(rustAction)
        }
    }

    override suspend fun uploadMedia(mimeType: String, data: ByteArray, progressCallback: ProgressCallback?): Result<String> = withContext(sessionDispatcher) {
        runCatching {
            client.uploadMedia(mimeType, data, progressCallback?.toProgressWatcher())
        }
    }

    override fun roomMembershipObserver(): RoomMembershipObserver = roomMembershipObserver

    override fun getRoomInfoFlow(roomId: RoomId): Flow<Optional<MatrixRoomInfo>> {
        return flow {
            var room = getRoom(roomId)
            if (room == null) {
                emit(Optional.empty())
                awaitRoom(roomId, INFINITE)
                room = getRoom(roomId)
            }
            room?.use {
                room.roomInfoFlow
                    .map { roomInfo -> Optional.of(roomInfo) }
                    .collect(this)
            }
        }.distinctUntilChanged()
    }

    private suspend fun File.getCacheSize(
        includeCryptoDb: Boolean = false,
    ): Long = withContext(sessionDispatcher) {
        val sessionDirectoryName = sessionDirectoryNameProvider.provides(sessionId)
        val sessionDirectory = File(this@getCacheSize, sessionDirectoryName)
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
        deleteCryptoDb: Boolean = false,
    ): Boolean = withContext(sessionDispatcher) {
        val sessionDirectoryName = sessionDirectoryNameProvider.provides(sessionId)
        val sessionDirectory = File(this@deleteSessionDirectory, sessionDirectoryName)
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

private val defaultRoomCreationPowerLevels = PowerLevels(
    usersDefault = null,
    eventsDefault = null,
    stateDefault = null,
    ban = null,
    kick = null,
    redact = null,
    invite = null,
    notifications = null,
    users = mapOf(),
    events = mapOf(
        "m.call.member" to 0,
        "org.matrix.msc3401.call.member" to 0,
    )
)
