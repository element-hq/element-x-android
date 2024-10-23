/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api

import io.element.android.libraries.matrix.api.core.DeviceId
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
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.room.PendingRoom
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias
import io.element.android.libraries.matrix.api.room.preview.RoomPreview
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryService
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.user.MatrixSearchUserResults
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.Closeable
import java.util.Optional

interface MatrixClient : Closeable {
    val sessionId: SessionId
    val deviceId: DeviceId
    val userProfile: StateFlow<MatrixUser>
    val roomListService: RoomListService
    val mediaLoader: MatrixMediaLoader
    val sessionCoroutineScope: CoroutineScope
    val ignoredUsersFlow: StateFlow<ImmutableList<UserId>>
    suspend fun getRoom(roomId: RoomId): MatrixRoom?
    suspend fun getPendingRoom(roomId: RoomId): PendingRoom?
    suspend fun findDM(userId: UserId): RoomId?
    suspend fun ignoreUser(userId: UserId): Result<Unit>
    suspend fun unignoreUser(userId: UserId): Result<Unit>
    suspend fun createRoom(createRoomParams: CreateRoomParameters): Result<RoomId>
    suspend fun createDM(userId: UserId): Result<RoomId>
    suspend fun getProfile(userId: UserId): Result<MatrixUser>
    suspend fun searchUsers(searchTerm: String, limit: Long): Result<MatrixSearchUserResults>
    suspend fun setDisplayName(displayName: String): Result<Unit>
    suspend fun uploadAvatar(mimeType: String, data: ByteArray): Result<Unit>
    suspend fun removeAvatar(): Result<Unit>
    suspend fun joinRoom(roomId: RoomId): Result<RoomSummary?>
    suspend fun joinRoomByIdOrAlias(roomIdOrAlias: RoomIdOrAlias, serverNames: List<String>): Result<RoomSummary?>
    suspend fun knockRoom(roomIdOrAlias: RoomIdOrAlias, message: String, serverNames: List<String>): Result<RoomSummary?>
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
     * Returns an optional URL. When the URL is there, it should be presented to the user after logout for
     * Relying Party (RP) initiated logout on their account page.
     * @param userInitiated if false, the logout came from the HS, no request will be made and the session entry will be kept in the store.
     * @param ignoreSdkError if true, the SDK will ignore any error and delete the session data anyway.
     */
    suspend fun logout(userInitiated: Boolean, ignoreSdkError: Boolean): String?

    /**
     * Retrieve the user profile, will also eventually emit a new value to [userProfile].
     */
    suspend fun getUserProfile(): Result<MatrixUser>
    suspend fun getAccountManagementUrl(action: AccountManagementAction?): Result<String?>
    suspend fun uploadMedia(mimeType: String, data: ByteArray, progressCallback: ProgressCallback?): Result<String>
    fun roomMembershipObserver(): RoomMembershipObserver

    /**
     * Get a room summary flow for a given room ID or alias.
     * The flow will emit a new value whenever the room summary is updated.
     * The flow will emit Optional.empty item if the room is not found.
     */
    fun getRoomSummaryFlow(roomIdOrAlias: RoomIdOrAlias): Flow<Optional<RoomSummary>>

    fun isMe(userId: UserId?) = userId == sessionId

    suspend fun trackRecentlyVisitedRoom(roomId: RoomId): Result<Unit>
    suspend fun getRecentlyVisitedRooms(): Result<List<RoomId>>
    suspend fun resolveRoomAlias(roomAlias: RoomAlias): Result<ResolvedRoomAlias>

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
    suspend fun getRoomPreview(roomIdOrAlias: RoomIdOrAlias, serverNames: List<String>): Result<RoomPreview>

    /** Returns `true` if the home server supports native sliding sync. */
    suspend fun isNativeSlidingSyncSupported(): Boolean

    /** Returns `true` if the home server supports sliding sync using a proxy. */
    suspend fun isSlidingSyncProxySupported(): Boolean

    /** Returns `true` if the current session is using native sliding sync, `false` if it's using a proxy. */
    fun isUsingNativeSlidingSync(): Boolean

    fun canDeactivateAccount(): Boolean
    suspend fun deactivateAccount(password: String, eraseData: Boolean): Result<Unit>
}

/**
 * Get a room info flow for a given room ID or alias.
 * The flow will emit a new value whenever the room info is updated.
 * The flow will emit Optional.empty item if the room is not found.
 */
fun MatrixClient.getRoomInfoFlow(roomIdOrAlias: RoomIdOrAlias): Flow<Optional<MatrixRoomInfo>> {
    return getRoomSummaryFlow(roomIdOrAlias)
        .map { roomSummary -> roomSummary.map { it.info } }
        .distinctUntilChanged()
}
