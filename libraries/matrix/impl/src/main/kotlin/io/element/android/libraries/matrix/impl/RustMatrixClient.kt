/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import io.element.android.libraries.androidutils.file.getSizeOfFiles
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.coroutine.childScope
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.core.extensions.mapFailure
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.createroom.CreateRoomParameters
import io.element.android.libraries.matrix.api.createroom.RoomPreset
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.notification.NotificationService
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.oidc.AccountManagementAction
import io.element.android.libraries.matrix.api.pusher.PushersService
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.NotJoinedRoom
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryService
import io.element.android.libraries.matrix.api.roomdirectory.RoomVisibility
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.sync.SlidingSyncVersion
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.matrix.api.user.MatrixSearchUserResults
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.impl.core.toProgressWatcher
import io.element.android.libraries.matrix.impl.encryption.RustEncryptionService
import io.element.android.libraries.matrix.impl.exception.mapClientException
import io.element.android.libraries.matrix.impl.media.RustMediaLoader
import io.element.android.libraries.matrix.impl.notification.RustNotificationService
import io.element.android.libraries.matrix.impl.notificationsettings.RustNotificationSettingsService
import io.element.android.libraries.matrix.impl.oidc.toRustAction
import io.element.android.libraries.matrix.impl.pushers.RustPushersService
import io.element.android.libraries.matrix.impl.room.GetRoomResult
import io.element.android.libraries.matrix.impl.room.NotJoinedRustRoom
import io.element.android.libraries.matrix.impl.room.RoomContentForwarder
import io.element.android.libraries.matrix.impl.room.RoomInfoMapper
import io.element.android.libraries.matrix.impl.room.RoomSyncSubscriber
import io.element.android.libraries.matrix.impl.room.RustRoomFactory
import io.element.android.libraries.matrix.impl.room.TimelineEventTypeFilterFactory
import io.element.android.libraries.matrix.impl.room.history.map
import io.element.android.libraries.matrix.impl.room.join.map
import io.element.android.libraries.matrix.impl.room.preview.RoomPreviewInfoMapper
import io.element.android.libraries.matrix.impl.roomdirectory.RustRoomDirectoryService
import io.element.android.libraries.matrix.impl.roomdirectory.map
import io.element.android.libraries.matrix.impl.roomlist.RoomListFactory
import io.element.android.libraries.matrix.impl.roomlist.RustRoomListService
import io.element.android.libraries.matrix.impl.roomlist.roomOrNull
import io.element.android.libraries.matrix.impl.sync.RustSyncService
import io.element.android.libraries.matrix.impl.sync.map
import io.element.android.libraries.matrix.impl.usersearch.UserProfileMapper
import io.element.android.libraries.matrix.impl.usersearch.UserSearchResultMapper
import io.element.android.libraries.matrix.impl.util.SessionPathsProvider
import io.element.android.libraries.matrix.impl.util.cancelAndDestroy
import io.element.android.libraries.matrix.impl.util.mxCallbackFlow
import io.element.android.libraries.matrix.impl.verification.RustSessionVerificationService
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.matrix.rustcomponents.sdk.AuthData
import org.matrix.rustcomponents.sdk.AuthDataPasswordDetails
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientException
import org.matrix.rustcomponents.sdk.IgnoredUsersListener
import org.matrix.rustcomponents.sdk.NotificationProcessSetup
import org.matrix.rustcomponents.sdk.PowerLevels
import org.matrix.rustcomponents.sdk.RoomInfoListener
import org.matrix.rustcomponents.sdk.SendQueueRoomErrorListener
import org.matrix.rustcomponents.sdk.TaskHandle
import org.matrix.rustcomponents.sdk.use
import timber.log.Timber
import java.io.File
import java.util.Optional
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import org.matrix.rustcomponents.sdk.CreateRoomParameters as RustCreateRoomParameters
import org.matrix.rustcomponents.sdk.RoomPreset as RustRoomPreset
import org.matrix.rustcomponents.sdk.SyncService as ClientSyncService

class RustMatrixClient(
    private val innerClient: Client,
    private val baseDirectory: File,
    private val sessionStore: SessionStore,
    private val appCoroutineScope: CoroutineScope,
    private val sessionDelegate: RustClientSessionDelegate,
    innerSyncService: ClientSyncService,
    dispatchers: CoroutineDispatchers,
    baseCacheDirectory: File,
    clock: SystemClock,
    timelineEventTypeFilterFactory: TimelineEventTypeFilterFactory,
    featureFlagService: FeatureFlagService,
) : MatrixClient {
    override val sessionId: UserId = UserId(innerClient.userId())
    override val deviceId: DeviceId = DeviceId(innerClient.deviceId())
    override val sessionCoroutineScope = appCoroutineScope.childScope(dispatchers.main, "Session-$sessionId")
    private val sessionDispatcher = dispatchers.io.limitedParallelism(64)

    private val innerRoomListService = innerSyncService.roomListService()

    private val rustSyncService = RustSyncService(
        inner = innerSyncService,
        dispatcher = sessionDispatcher,
        sessionCoroutineScope = sessionCoroutineScope
    )
    private val pushersService = RustPushersService(
        client = innerClient,
        dispatchers = dispatchers,
    )
    private val notificationProcessSetup = NotificationProcessSetup.SingleProcess(innerSyncService)
    private val innerNotificationClient = runBlocking { innerClient.notificationClient(notificationProcessSetup) }
    private val notificationService = RustNotificationService(sessionId, innerNotificationClient, dispatchers, clock)
    private val notificationSettingsService = RustNotificationSettingsService(innerClient, sessionCoroutineScope, dispatchers)
    private val encryptionService = RustEncryptionService(
        client = innerClient,
        syncService = rustSyncService,
        sessionCoroutineScope = sessionCoroutineScope,
        dispatchers = dispatchers,
    )

    private val roomDirectoryService = RustRoomDirectoryService(
        client = innerClient,
        sessionDispatcher = sessionDispatcher,
    )

    private val sessionPathsProvider = SessionPathsProvider(sessionStore)

    private val roomSyncSubscriber: RoomSyncSubscriber = RoomSyncSubscriber(innerRoomListService, dispatchers)

    override val roomListService: RoomListService = RustRoomListService(
        innerRoomListService = innerRoomListService,
        sessionCoroutineScope = sessionCoroutineScope,
        sessionDispatcher = sessionDispatcher,
        roomListFactory = RoomListFactory(
            innerRoomListService = innerRoomListService,
            sessionCoroutineScope = sessionCoroutineScope,
        ),
        roomSyncSubscriber = roomSyncSubscriber,
    )

    private val verificationService = RustSessionVerificationService(
        client = innerClient,
        isSyncServiceReady = rustSyncService.syncState.map { it == SyncState.Running },
        sessionCoroutineScope = sessionCoroutineScope,
    )

    private val roomInfoMapper = RoomInfoMapper()
    private val roomMembershipObserver = RoomMembershipObserver()
    private val roomFactory = RustRoomFactory(
        roomListService = roomListService,
        innerRoomListService = innerRoomListService,
        sessionId = sessionId,
        deviceId = deviceId,
        notificationSettingsService = notificationSettingsService,
        sessionCoroutineScope = sessionCoroutineScope,
        dispatchers = dispatchers,
        systemClock = clock,
        roomContentForwarder = RoomContentForwarder(innerRoomListService),
        roomSyncSubscriber = roomSyncSubscriber,
        timelineEventTypeFilterFactory = timelineEventTypeFilterFactory,
        featureFlagService = featureFlagService,
        roomMembershipObserver = roomMembershipObserver,
        roomInfoMapper = roomInfoMapper,
    )

    override val mediaLoader: MatrixMediaLoader = RustMediaLoader(
        baseCacheDirectory = baseCacheDirectory,
        dispatchers = dispatchers,
        innerClient = innerClient,
    )

    private var clientDelegateTaskHandle: TaskHandle? = innerClient.setDelegate(sessionDelegate)

    private val _userProfile: MutableStateFlow<MatrixUser> = MutableStateFlow(
        MatrixUser(
            userId = sessionId,
            // TODO cache for displayName?
            displayName = null,
            avatarUrl = null,
        )
    )

    override val userProfile: StateFlow<MatrixUser> = _userProfile

    override val ignoredUsersFlow = mxCallbackFlow<ImmutableList<UserId>> {
        // Fetch the initial value manually, the SDK won't return it automatically
        channel.trySend(innerClient.ignoredUsers().map(::UserId).toPersistentList())

        innerClient.subscribeToIgnoredUsers(object : IgnoredUsersListener {
            override fun call(ignoredUserIds: List<String>) {
                channel.trySend(ignoredUserIds.map(::UserId).toPersistentList())
            }
        })
    }
        .buffer(Channel.UNLIMITED)
        .stateIn(sessionCoroutineScope, started = SharingStarted.Eagerly, initialValue = persistentListOf())

    init {
        // Make sure the session delegate has a reference to the client to be able to logout on auth error
        sessionDelegate.bindClient(this)

        sessionCoroutineScope.launch {
            // Start notification settings
            notificationSettingsService.start()

            // Force a refresh of the profile
            getUserProfile()
        }
    }

    override fun userIdServerName(): String {
        return runCatchingExceptions {
            innerClient.userIdServerName()
        }
            .onFailure {
                Timber.w(it, "Failed to get userIdServerName")
            }
            .getOrNull()
            ?: sessionId.value.substringAfter(":")
    }

    override suspend fun getUrl(url: String): Result<String> = withContext(sessionDispatcher) {
        runCatchingExceptions {
            innerClient.getUrl(url)
        }
    }

    override suspend fun getRoom(roomId: RoomId): BaseRoom? = withContext(sessionDispatcher) {
        roomFactory.getBaseRoom(roomId)
    }

    override suspend fun getJoinedRoom(roomId: RoomId): JoinedRoom? = withContext(sessionDispatcher) {
        (roomFactory.getJoinedRoomOrPreview(roomId) as? GetRoomResult.Joined)?.joinedRoom
    }

    /**
     * Wait for the room to be available in the client with the correct membership for the current user.
     * @param roomId the room id to wait for
     * @param timeout the timeout to wait for the room to be available
     * @param currentUserMembership the membership to wait for
     * @throws TimeoutCancellationException if the room is not available after the timeout
     */
    private suspend fun awaitRoom(
        roomId: RoomId,
        timeout: Duration,
        currentUserMembership: CurrentUserMembership,
    ): RoomInfo {
        return withTimeout(timeout) {
            getRoomInfoFlow(roomId)
                .mapNotNull { roomInfo -> roomInfo.getOrNull() }
                .first { info -> info.currentUserMembership == currentUserMembership }
                // Ensure that the room is ready
                .also { innerClient.awaitRoomRemoteEcho(roomId.value).destroy() }
        }
    }

    override suspend fun findDM(userId: UserId): Result<RoomId?> = withContext(sessionDispatcher) {
        runCatchingExceptions {
            innerClient.getDmRoom(userId.value)?.use { RoomId(it.id()) }
        }
    }

    override suspend fun ignoreUser(userId: UserId): Result<Unit> = withContext(sessionDispatcher) {
        runCatchingExceptions {
            innerClient.ignoreUser(userId.value)
        }
    }

    override suspend fun unignoreUser(userId: UserId): Result<Unit> = withContext(sessionDispatcher) {
        runCatchingExceptions {
            innerClient.unignoreUser(userId.value)
        }
    }

    override suspend fun createRoom(createRoomParams: CreateRoomParameters): Result<RoomId> = withContext(sessionDispatcher) {
        runCatchingExceptions {
            val rustParams = RustCreateRoomParameters(
                name = createRoomParams.name,
                topic = createRoomParams.topic,
                isEncrypted = createRoomParams.isEncrypted,
                isDirect = createRoomParams.isDirect,
                visibility = createRoomParams.visibility.map(),
                preset = when (createRoomParams.preset) {
                    RoomPreset.PRIVATE_CHAT -> RustRoomPreset.PRIVATE_CHAT
                    RoomPreset.TRUSTED_PRIVATE_CHAT -> RustRoomPreset.TRUSTED_PRIVATE_CHAT
                    RoomPreset.PUBLIC_CHAT -> RustRoomPreset.PUBLIC_CHAT
                },
                invite = createRoomParams.invite?.map { it.value },
                avatar = createRoomParams.avatar,
                powerLevelContentOverride = defaultRoomCreationPowerLevels.copy(
                    invite = if (createRoomParams.joinRuleOverride == JoinRule.Knock) {
                        // override the invite power level so it's the same as kick.
                        RoomMember.Role.MODERATOR.powerLevel.toInt()
                    } else {
                        null
                    }
                ),
                joinRuleOverride = createRoomParams.joinRuleOverride?.map(),
                historyVisibilityOverride = createRoomParams.historyVisibilityOverride?.map(),
                canonicalAlias = createRoomParams.roomAliasName.getOrNull(),
            )
            val roomId = RoomId(innerClient.createRoom(rustParams))
            // Wait to receive the room back from the sync but do not returns failure if it fails.
            try {
                awaitRoom(roomId, 30.seconds, CurrentUserMembership.JOINED)
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
            visibility = RoomVisibility.Private,
            preset = RoomPreset.TRUSTED_PRIVATE_CHAT,
            invite = listOf(userId),
        )
        return createRoom(createRoomParams)
    }

    override suspend fun getProfile(userId: UserId): Result<MatrixUser> = withContext(sessionDispatcher) {
        runCatchingExceptions {
            innerClient.getProfile(userId.value).let(UserProfileMapper::map)
        }
    }

    override suspend fun getUserProfile(): Result<MatrixUser> = getProfile(sessionId)
        .onSuccess { _userProfile.tryEmit(it) }

    override suspend fun searchUsers(searchTerm: String, limit: Long): Result<MatrixSearchUserResults> =
        withContext(sessionDispatcher) {
            runCatchingExceptions {
                innerClient.searchUsers(searchTerm, limit.toULong()).let(UserSearchResultMapper::map)
            }
        }

    override suspend fun setDisplayName(displayName: String): Result<Unit> =
        withContext(sessionDispatcher) {
            runCatchingExceptions { innerClient.setDisplayName(displayName) }
        }

    override suspend fun uploadAvatar(mimeType: String, data: ByteArray): Result<Unit> =
        withContext(sessionDispatcher) {
            runCatchingExceptions { innerClient.uploadAvatar(mimeType, data) }
        }

    override suspend fun removeAvatar(): Result<Unit> =
        withContext(sessionDispatcher) {
            runCatchingExceptions { innerClient.removeAvatar() }
        }

    override suspend fun joinRoom(roomId: RoomId): Result<RoomInfo?> = withContext(sessionDispatcher) {
        runCatchingExceptions {
            innerClient.joinRoomById(roomId.value).destroy()
            try {
                awaitRoom(roomId, 10.seconds, CurrentUserMembership.JOINED)
            } catch (e: Exception) {
                Timber.e(e, "Timeout waiting for the room to be available in the room list")
                null
            }
        }
    }.mapFailure { it.mapClientException() }

    override suspend fun joinRoomByIdOrAlias(roomIdOrAlias: RoomIdOrAlias, serverNames: List<String>): Result<RoomInfo?> = withContext(sessionDispatcher) {
        runCatchingExceptions {
            val roomId = innerClient.joinRoomByIdOrAlias(
                roomIdOrAlias = roomIdOrAlias.identifier,
                serverNames = serverNames,
            ).use {
                RoomId(it.id())
            }
            try {
                awaitRoom(roomId, 10.seconds, CurrentUserMembership.JOINED)
            } catch (e: Exception) {
                Timber.e(e, "Timeout waiting for the room to be available in the room list")
                null
            }
        }.mapFailure { it.mapClientException() }
    }

    override suspend fun knockRoom(roomIdOrAlias: RoomIdOrAlias, message: String, serverNames: List<String>): Result<RoomInfo?> = withContext(
        sessionDispatcher
    ) {
        runCatchingExceptions {
            val roomId = innerClient.knock(roomIdOrAlias.identifier, message, serverNames).use {
                RoomId(it.id())
            }
            try {
                awaitRoom(roomId, 10.seconds, CurrentUserMembership.KNOCKED)
            } catch (e: Exception) {
                Timber.e(e, "Timeout waiting for the room to be available in the room list")
                null
            }
        }.mapFailure { it.mapClientException() }
    }

    override suspend fun trackRecentlyVisitedRoom(roomId: RoomId): Result<Unit> = withContext(sessionDispatcher) {
        runCatchingExceptions {
            innerClient.trackRecentlyVisitedRoom(roomId.value)
        }
    }

    override suspend fun getRecentlyVisitedRooms(): Result<List<RoomId>> = withContext(sessionDispatcher) {
        runCatchingExceptions {
            innerClient.getRecentlyVisitedRooms().map(::RoomId)
        }
    }

    override suspend fun resolveRoomAlias(roomAlias: RoomAlias): Result<Optional<ResolvedRoomAlias>> = withContext(sessionDispatcher) {
        runCatchingExceptions {
            val result = innerClient.resolveRoomAlias(roomAlias.value)?.let {
                ResolvedRoomAlias(
                    roomId = RoomId(it.roomId),
                    servers = it.servers,
                )
            }
            Optional.ofNullable(result)
        }
    }

    override suspend fun getRoomPreview(roomIdOrAlias: RoomIdOrAlias, serverNames: List<String>): Result<NotJoinedRoom> = withContext(sessionDispatcher) {
        runCatchingExceptions {
            when (roomIdOrAlias) {
                is RoomIdOrAlias.Alias -> {
                    val roomId = innerClient.resolveRoomAlias(roomIdOrAlias.roomAlias.value)?.roomId?.let { RoomId(it) }

                    var room = (roomId?.let { roomFactory.getJoinedRoomOrPreview(it) } as? GetRoomResult.NotJoined)?.notJoinedRoom
                    if (room == null) {
                        val preview = innerClient.getRoomPreviewFromRoomAlias(roomIdOrAlias.roomAlias.value)
                        room = NotJoinedRustRoom(sessionId, null, RoomPreviewInfoMapper.map(preview.info()))
                    }
                    room
                }
                is RoomIdOrAlias.Id -> {
                    var room = (roomFactory.getJoinedRoomOrPreview(roomIdOrAlias.roomId) as? GetRoomResult.NotJoined)?.notJoinedRoom

                    if (room == null) {
                        val preview = innerClient.getRoomPreviewFromRoomId(roomIdOrAlias.roomId.value, serverNames)
                        room = NotJoinedRustRoom(sessionId, null, RoomPreviewInfoMapper.map(preview.info()))
                    }
                    room
                }
            }
        }.mapFailure { it.mapClientException() }
    }

    override fun syncService(): SyncService = rustSyncService

    override fun sessionVerificationService(): SessionVerificationService = verificationService

    override fun pushersService(): PushersService = pushersService

    override fun notificationService(): NotificationService = notificationService

    override fun encryptionService(): EncryptionService = encryptionService

    override fun notificationSettingsService(): NotificationSettingsService = notificationSettingsService

    override fun roomDirectoryService(): RoomDirectoryService = roomDirectoryService

    internal suspend fun destroy() {
        innerNotificationClient.close()

        roomFactory.destroy()
        rustSyncService.destroy()
        notificationSettingsService.destroy()
        notificationProcessSetup.destroy()

        sessionCoroutineScope.cancel()
        clientDelegateTaskHandle?.cancelAndDestroy()
        verificationService.destroy()

        sessionDelegate.clearCurrentClient()
        innerRoomListService.close()
        notificationService.close()
        encryptionService.close()
        innerClient.close()
    }

    override suspend fun getCacheSize(): Long {
        return baseDirectory.getCacheSize()
    }

    override suspend fun clearCache() {
        innerClient.clearCaches()
        destroy()
    }

    override suspend fun logout(userInitiated: Boolean, ignoreSdkError: Boolean) {
        sessionCoroutineScope.cancel()
        // Remove current delegate so we don't receive an auth error
        clientDelegateTaskHandle?.cancelAndDestroy()
        clientDelegateTaskHandle = null
        withContext(sessionDispatcher) {
            if (userInitiated) {
                try {
                    innerClient.logout()
                } catch (failure: Throwable) {
                    if (ignoreSdkError) {
                        Timber.e(failure, "Fail to call logout on HS. Still delete local files.")
                    } else {
                        // If the logout failed we need to restore the delegate
                        clientDelegateTaskHandle = innerClient.setDelegate(sessionDelegate)
                        Timber.e(failure, "Fail to call logout on HS.")
                        throw failure
                    }
                }
            }
            destroy()

            deleteSessionDirectory()
            if (userInitiated) {
                sessionStore.removeSession(sessionId.value)
            }
        }
    }

    override fun canDeactivateAccount(): Boolean {
        return runCatchingExceptions {
            innerClient.canDeactivateAccount()
        }
            .getOrNull()
            .orFalse()
    }

    override suspend fun deactivateAccount(password: String, eraseData: Boolean): Result<Unit> = withContext(sessionDispatcher) {
        Timber.w("Deactivating account")
        // Remove current delegate so we don't receive an auth error
        clientDelegateTaskHandle?.cancelAndDestroy()
        clientDelegateTaskHandle = null
        runCatchingExceptions {
            // First call without AuthData, should fail
            val firstAttempt = runCatchingExceptions {
                innerClient.deactivateAccount(
                    authData = null,
                    eraseData = eraseData,
                )
            }
            if (firstAttempt.isFailure) {
                Timber.w(firstAttempt.exceptionOrNull(), "Expected failure, try again")
                // This is expected, try again with the password
                runCatchingExceptions {
                    innerClient.deactivateAccount(
                        authData = AuthData.Password(
                            passwordDetails = AuthDataPasswordDetails(
                                identifier = sessionId.value,
                                password = password,
                            ),
                        ),
                        eraseData = eraseData,
                    )
                }.onFailure {
                    Timber.e(it, "Failed to deactivate account")
                    // If the deactivation failed we need to restore the delegate
                    clientDelegateTaskHandle = innerClient.setDelegate(sessionDelegate)
                    throw it
                }
            }
            destroy()
            deleteSessionDirectory()
            sessionStore.removeSession(sessionId.value)
        }.onFailure {
            Timber.e(it, "Failed to deactivate account")
        }
    }

    override suspend fun getAccountManagementUrl(action: AccountManagementAction?): Result<String?> = withContext(sessionDispatcher) {
        val rustAction = action?.toRustAction()
        runCatchingExceptions {
            innerClient.accountUrl(rustAction)
        }
    }

    override suspend fun uploadMedia(mimeType: String, data: ByteArray, progressCallback: ProgressCallback?): Result<String> = withContext(sessionDispatcher) {
        runCatchingExceptions {
            innerClient.uploadMedia(mimeType, data, progressCallback?.toProgressWatcher())
        }
    }

    override fun roomMembershipObserver(): RoomMembershipObserver = roomMembershipObserver

    override fun getRoomInfoFlow(roomId: RoomId): Flow<Optional<RoomInfo>> {
        return mxCallbackFlow {
            val roomNotFound = innerRoomListService.roomOrNull(roomId.value).use { it == null }
            if (roomNotFound) {
                channel.send(Optional.empty())
            }
            innerClient.subscribeToRoomInfo(roomId.value, object : RoomInfoListener {
                override fun call(roomInfo: org.matrix.rustcomponents.sdk.RoomInfo) {
                    val mappedRoomInfo = roomInfoMapper.map(roomInfo)
                    channel.trySend(Optional.of(mappedRoomInfo))
                }
            })
        }.distinctUntilChanged()
    }

    override suspend fun setAllSendQueuesEnabled(enabled: Boolean) {
        withContext(sessionDispatcher) {
            Timber.i("setAllSendQueuesEnabled($enabled)")
            tryOrNull {
                innerClient.enableAllSendQueues(enabled)
            }
        }
    }

    override fun sendQueueDisabledFlow(): Flow<RoomId> = mxCallbackFlow {
        innerClient.subscribeToSendQueueStatus(object : SendQueueRoomErrorListener {
            override fun onError(roomId: String, error: ClientException) {
                trySend(RoomId(roomId))
            }
        })
    }.buffer(Channel.UNLIMITED)

    override suspend fun availableSlidingSyncVersions(): Result<List<SlidingSyncVersion>> = withContext(sessionDispatcher) {
        runCatchingExceptions {
            innerClient.availableSlidingSyncVersions().map { it.map() }
        }
    }

    override suspend fun currentSlidingSyncVersion(): Result<SlidingSyncVersion> = withContext(sessionDispatcher) {
        runCatchingExceptions {
            innerClient.session().slidingSyncVersion.map()
        }
    }

    override suspend fun canReportRoom(): Boolean = withContext(sessionDispatcher) {
        runCatchingExceptions {
            innerClient.isReportRoomApiSupported()
        }.getOrDefault(false)
    }

    private suspend fun File.getCacheSize(
        includeCryptoDb: Boolean = false,
    ): Long = withContext(sessionDispatcher) {
        val sessionDirectory = sessionPathsProvider.provides(sessionId) ?: return@withContext 0L
        val cacheSize = sessionDirectory.cacheDirectory.getSizeOfFiles()
        if (includeCryptoDb) {
            cacheSize + sessionDirectory.fileDirectory.getSizeOfFiles()
        } else {
            cacheSize + listOf(
                "matrix-sdk-state.sqlite3",
                "matrix-sdk-state.sqlite3-shm",
                "matrix-sdk-state.sqlite3-wal",
            ).map { fileName ->
                File(sessionDirectory.fileDirectory, fileName)
            }.sumOf { file ->
                file.length()
            }
        }
    }

    private suspend fun deleteSessionDirectory() = withContext(sessionDispatcher) {
        // Delete all the files for this session
        sessionPathsProvider.provides(sessionId)?.deleteRecursively()
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
