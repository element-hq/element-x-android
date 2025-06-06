/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api

import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.MatrixPatterns
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.createroom.CreateRoomParameters
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.notification.NotificationService
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.oidc.AccountManagementAction
import io.element.android.libraries.matrix.api.pusher.PushersService
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.NotJoinedRoom
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryService
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.sync.SlidingSyncVersion
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.user.MatrixSearchUserResults
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.Optional

interface MatrixClient {
    val sessionId: SessionId
    val deviceId: DeviceId
    val userProfile: StateFlow<MatrixUser>
    val roomListService: RoomListService
    val mediaLoader: MatrixMediaLoader
    val sessionCoroutineScope: CoroutineScope
    val ignoredUsersFlow: StateFlow<ImmutableList<UserId>>
    suspend fun getJoinedRoom(roomId: RoomId): JoinedRoom?
    suspend fun getRoom(roomId: RoomId): BaseRoom?
    suspend fun findDM(userId: UserId): Result<RoomId?>
    suspend fun ignoreUser(userId: UserId): Result<Unit>
    suspend fun unignoreUser(userId: UserId): Result<Unit>
    suspend fun createRoom(createRoomParams: CreateRoomParameters): Result<RoomId>
    suspend fun createDM(userId: UserId): Result<RoomId>
    suspend fun getProfile(userId: UserId): Result<MatrixUser>
    suspend fun searchUsers(searchTerm: String, limit: Long): Result<MatrixSearchUserResults>
    suspend fun setDisplayName(displayName: String): Result<Unit>
    suspend fun uploadAvatar(mimeType: String, data: ByteArray): Result<Unit>
    suspend fun removeAvatar(): Result<Unit>
    suspend fun joinRoom(roomId: RoomId): Result<RoomInfo?>
    suspend fun joinRoomByIdOrAlias(roomIdOrAlias: RoomIdOrAlias, serverNames: List<String>): Result<RoomInfo?>
    suspend fun knockRoom(roomIdOrAlias: RoomIdOrAlias, message: String, serverNames: List<String>): Result<RoomInfo?>
    fun syncService(): SyncService
    fun sessionVerificationService(): SessionVerificationService
    fun pushersService(): PushersService
    fun notificationService(): NotificationService
    fun notificationSettingsService(): NotificationSettingsService
    fun encryptionService(): EncryptionService
    fun roomDirectoryService(): RoomDirectoryService
    suspend fun getCacheSize(): Long

    /**
     * Will close the client and delete the cache data.
     */
    suspend fun clearCache()

    /**
     * Logout the user.
     *
     * @param userInitiated if false, the logout came from the HS, no request will be made and the session entry will be kept in the store.
     * @param ignoreSdkError if true, the SDK will ignore any error and delete the session data anyway.
     */
    suspend fun logout(userInitiated: Boolean, ignoreSdkError: Boolean)

    /**
     * Retrieve the user profile, will also eventually emit a new value to [userProfile].
     */
    suspend fun getUserProfile(): Result<MatrixUser>
    suspend fun getAccountManagementUrl(action: AccountManagementAction?): Result<String?>
    suspend fun uploadMedia(mimeType: String, data: ByteArray, progressCallback: ProgressCallback?): Result<String>
    fun roomMembershipObserver(): RoomMembershipObserver

    /**
     * Get a room info flow for a given room ID.
     * The flow will emit a new value whenever the room info is updated.
     * The flow will emit Optional.empty item if the room is not found.
     */
    fun getRoomInfoFlow(roomId: RoomId): Flow<Optional<RoomInfo>>

    fun isMe(userId: UserId?) = userId == sessionId

    suspend fun trackRecentlyVisitedRoom(roomId: RoomId): Result<Unit>
    suspend fun getRecentlyVisitedRooms(): Result<List<RoomId>>

    /**
     * Resolves the given room alias to a roomID (and a list of servers), if possible.
     * @param roomAlias the room alias to resolve
     * @return the resolved room alias if any, an empty result if not found,or an error if the resolution failed.
     *
     */
    suspend fun resolveRoomAlias(roomAlias: RoomAlias): Result<Optional<ResolvedRoomAlias>>

    /**
     * Enables or disables the sending queue, according to the given parameter.
     *
     * The sending queue automatically disables itself whenever sending an
     * event with it failed (e.g. sending an event via the Timeline),
     * so it's required to manually re-enable it as soon as
     * connectivity is back on the device.
     */
    suspend fun setAllSendQueuesEnabled(enabled: Boolean)

    /**
     * Returns a flow of room IDs that have send queue being disabled.
     * This flow will emit a new value whenever the send queue is disabled for a room.
     */
    fun sendQueueDisabledFlow(): Flow<RoomId>

    /**
     * Return the server name part of the current user ID, using the SDK, and if a failure occurs,
     * compute it manually.
     */
    fun userIdServerName(): String

    /**
     * Execute generic GET requests through the SDKs internal HTTP client.
     */
    suspend fun getUrl(url: String): Result<String>

    /**
     * Get a room preview for a given room ID or alias. This is especially useful for rooms that the user is not a member of, or hasn't joined yet.
     */
    suspend fun getRoomPreview(roomIdOrAlias: RoomIdOrAlias, serverNames: List<String>): Result<NotJoinedRoom>

    /**
     * Returns the currently used sliding sync version.
     */
    suspend fun currentSlidingSyncVersion(): Result<SlidingSyncVersion>

    /**
     * Returns the available sliding sync versions for the current user.
     */
    suspend fun availableSlidingSyncVersions(): Result<List<SlidingSyncVersion>>

    fun canDeactivateAccount(): Boolean
    suspend fun deactivateAccount(password: String, eraseData: Boolean): Result<Unit>

    /**
     * Check if the user can report a room.
     */
    suspend fun canReportRoom(): Boolean
}

/**
 * Returns a room alias from a room alias name, or null if the name is not valid.
 * @param name the room alias name ie. the local part of the room alias.
 */
fun MatrixClient.roomAliasFromName(name: String): RoomAlias? {
    return name.takeIf { it.isNotEmpty() }
        ?.let { "#$it:${userIdServerName()}" }
        ?.takeIf { MatrixPatterns.isRoomAlias(it) }
        ?.let { tryOrNull { RoomAlias(it) } }
}
