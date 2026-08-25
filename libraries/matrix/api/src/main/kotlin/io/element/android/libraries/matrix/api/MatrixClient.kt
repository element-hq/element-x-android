/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api

import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.matrix.api.analytics.SdkStoreSizes
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.MatrixPatterns
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.createroom.CreateRoomParameters
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.linknewdevice.LinkDesktopHandler
import io.element.android.libraries.matrix.api.linknewdevice.LinkMobileHandler
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.media.MediaPreviewService
import io.element.android.libraries.matrix.api.notification.NotificationService
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.oauth.AccountManagementAction
import io.element.android.libraries.matrix.api.paths.SessionPaths
import io.element.android.libraries.matrix.api.pusher.PushersService
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.NotJoinedRoom
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias
import io.element.android.libraries.matrix.api.room.location.BeaconInfoUpdate
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryService
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.scanner.ContentScanner
import io.element.android.libraries.matrix.api.search.MessageSearchService
import io.element.android.libraries.matrix.api.spaces.SpaceService
import io.element.android.libraries.matrix.api.sync.SlidingSyncVersion
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.user.MatrixSearchUserResults
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.user.UserStatus
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.Optional

/**
 * Entry point for everything scoped to a single logged in session: the sub-services, room access, and account level operations.
 *
 * One instance exists per session, created on login or session restore and torn down by [logout], [clearCache] or account deactivation.
 * Suspending members here report failures through a [Result] instead of throwing, and run their work off the main thread.
 */
interface MatrixClient : ClientUrlContentFetcher {
    /** Identifier of the logged in user; a [SessionId] and a [UserId] are the same value. */
    val sessionId: SessionId

    /** Identifier of this device, as assigned by the homeserver when the session was created. */
    val deviceId: DeviceId

    /** Directories holding this session's databases, caches and downloaded media. */
    val sessionPaths: SessionPaths

    /**
     * Display name and avatar of the logged in user.
     * Seeded from the local session store so a value is available before the first network fetch, then kept up to date by the SDK.
     */
    val userProfile: StateFlow<MatrixUser>

    /** Gives access to the list of rooms the user is a member of, or has been invited to. */
    val roomListService: RoomListService

    /** Gives access to the spaces the user belongs to and their hierarchy. */
    val spaceService: SpaceService

    /** Controls the sync loop of this session and exposes its state. */
    val syncService: SyncService

    /** Handles verifying this session against the user's other devices or their recovery key. */
    val sessionVerificationService: SessionVerificationService

    /** Manages the push gateways (pushers) registered on the homeserver for this session. */
    val pushersService: PushersService

    /** Resolves incoming push notifications into renderable events. */
    val notificationService: NotificationService

    /** Reads and writes the push notification rules of the account. */
    val notificationSettingsService: NotificationSettingsService

    /** Gives access to end-to-end encryption state, recovery and key backup. */
    val encryptionService: EncryptionService

    /** Gives access to the public room directory of the homeserver. */
    val roomDirectoryService: RoomDirectoryService

    /** Whether this live client was built with a message search index attached. */
    val isMessageSearchAvailable: Boolean

    /** Searches messages in the local index; only usable when [isMessageSearchAvailable] is `true`. */
    val messageSearchService: MessageSearchService

    /** Reads and writes the account settings controlling whether media previews and avatars are shown. */
    val mediaPreviewService: MediaPreviewService

    /** Downloads media content, thumbnails and files from the homeserver, caching them on disk. */
    val matrixMediaLoader: MatrixMediaLoader

    /**
     * Coroutine scope tied to the lifetime of this session, to be used for work that must stop when the session goes away.
     * It is cancelled when the session is logged out or otherwise destroyed, so never use it for work that must outlive the session.
     */
    val sessionCoroutineScope: CoroutineScope

    /** The users the logged in user has ignored; starts empty and is updated by the SDK, including with an initial value. */
    val ignoredUsersFlow: StateFlow<ImmutableList<UserId>>

    /** Notifies of membership changes the current user makes, so that callers can react to leaving or joining a room. */
    val roomMembershipObserver: RoomMembershipObserver

    /** Emits updates about the live location beacons published by the current user. */
    val ownBeaconInfoUpdates: Flow<BeaconInfoUpdate>

    /** Scans media content before it is displayed, or `null` when no content scanner is configured for this session. */
    val contentScanner: ContentScanner?

    /** Whether the session is in the process of shutting down, either because it is being logged out or its cache is being cleared. */
    val isShuttingDown: Boolean

    /**
     * Returns the room with the given id if the user has joined it, along with its live [Timeline].
     * A new instance is created on each call and the caller owns it, so it must be closed once no longer needed.
     *
     * @param roomId the id of the room to open.
     * @return the room, or `null` if it is unknown to the client or the user is not joined.
     */
    suspend fun getJoinedRoom(roomId: RoomId): JoinedRoom?

    /**
     * Returns the room with the given id whatever the current membership is, without building a timeline.
     * As with [getJoinedRoom], a new instance is created on each call and must be closed by the caller.
     *
     * @param roomId the id of the room to open.
     * @return the room, or `null` if it is unknown to the client.
     */
    suspend fun getRoom(roomId: RoomId): BaseRoom?

    /**
     * Looks for an existing direct message room shared with [userId].
     *
     * @param userId the other member of the direct message room.
     * @return the room id, or `null` if no direct message room exists with this user.
     */
    suspend fun findDM(userId: UserId): Result<RoomId?>

    /**
     * Returns the ids of every room the user has joined, as currently known by the client.
     * Invited, knocked and left rooms are excluded.
     */
    suspend fun getJoinedRoomIds(): Result<Set<RoomId>>

    /**
     * Adds [userId] to the ignore list of the account, which hides their events and makes [ignoredUsersFlow] emit.
     *
     * @param userId the user to ignore.
     */
    suspend fun ignoreUser(userId: UserId): Result<Unit>

    /**
     * Removes [userId] from the ignore list of the account, which makes [ignoredUsersFlow] emit.
     *
     * @param userId the user to stop ignoring.
     */
    suspend fun unignoreUser(userId: UserId): Result<Unit>

    /**
     * Creates a room, applying the app's default power levels on top of [createRoomParams].
     * Waits a short while for the new room to come back through sync, but a timeout there is only logged: the result is still a success.
     *
     * @param createRoomParams the name, topic, preset, invitees and other settings of the room to create.
     */
    suspend fun createRoom(createRoomParams: CreateRoomParameters): Result<RoomId>

    /**
     * Creates a direct message room with [userId], as a private, invite-only room with the user already invited.
     * This does not check whether such a room already exists; use [findDM] first if you want to reuse one.
     *
     * @param userId the user to start the direct message with.
     * @param isEncrypted whether the room should have end-to-end encryption enabled.
     */
    suspend fun createDM(userId: UserId, isEncrypted: Boolean): Result<RoomId>

    /**
     * Fetches the current profile of any user from the homeserver, bypassing the local cache.
     *
     * @param userId the user whose profile is requested.
     */
    suspend fun getProfile(userId: UserId): Result<MatrixUser>

    /**
     * Searches the homeserver's user directory.
     *
     * @param searchTerm the text to look for in user ids and display names.
     * @param limit the maximum number of results to return.
     */
    suspend fun searchUsers(searchTerm: String, limit: Long): Result<MatrixSearchUserResults>

    /**
     * Changes the display name of the logged in user on the homeserver.
     *
     * @param displayName the new display name.
     */
    suspend fun setDisplayName(displayName: String): Result<Unit>

    /**
     * Uploads [data] as the new avatar of the logged in user.
     *
     * @param mimeType the MIME type of the image, for instance `image/jpeg`.
     * @param data the raw bytes of the image.
     */
    suspend fun uploadAvatar(mimeType: String, data: ByteArray): Result<Unit>

    /** Removes the avatar of the logged in user. */
    suspend fun removeAvatar(): Result<Unit>

    /**
     * Sets the `m.status` profile field of the logged in user (MSC4426), refreshing [userProfile] if the server does not push the change itself.
     *
     * @param status the status to publish.
     */
    suspend fun setUserStatus(status: UserStatus): Result<Unit>

    /** Clears both m.status and m.call profile fields (maps to DELETE on the profile endpoint per MSC4426). */
    suspend fun clearUserStatus(): Result<Unit>

    /** Whether the homeserver advertises support for user status (MSC4426). */
    suspend fun isUserStatusSupported(): Result<Boolean>

    /** Whether the homeserver advertises support for the Profiles sliding sync extension (MSC4262). */
    suspend fun isProfilesSlidingSyncExtensionSupported(): Result<Boolean>

    /**
     * Enable or disable automatically setting the user's status to "in a call" (m.call) while in a call.
     *
     * @param enabled whether the status should be maintained automatically.
     */
    fun enableAutomaticCallStatus(enabled: Boolean)

    /**
     * Joins a room the client already knows about, then waits briefly for the new membership to arrive through sync.
     * The returned [RoomInfo] is `null` when that wait times out, which is not treated as a failure: the join itself succeeded.
     *
     * @param roomId the id of the room to join.
     */
    suspend fun joinRoom(roomId: RoomId): Result<RoomInfo?>

    /**
     * Joins a room by id or by alias, resolving it through [serverNames] when the client does not know it yet.
     * As with [joinRoom], the returned [RoomInfo] is `null` if the membership did not arrive through sync in time.
     *
     * @param roomIdOrAlias the id or the alias of the room to join.
     * @param serverNames servers to ask about the room, used when it cannot be resolved locally.
     */
    suspend fun joinRoomByIdOrAlias(roomIdOrAlias: RoomIdOrAlias, serverNames: List<String>): Result<RoomInfo?>

    /**
     * Sends a request to join a room whose join rule is knock, then waits briefly for the knocked membership to arrive through sync.
     * As with [joinRoom], the returned [RoomInfo] is `null` if that wait times out.
     *
     * @param roomIdOrAlias the id or the alias of the room to knock on.
     * @param message the reason shown to the room moderators; may be empty.
     * @param serverNames servers to ask about the room, used when it cannot be resolved locally.
     */
    suspend fun knockRoom(roomIdOrAlias: RoomIdOrAlias, message: String, serverNames: List<String>): Result<RoomInfo?>

    /**
     * Returns the number of bytes this session occupies on disk that [clearCache] would be able to reclaim.
     * This covers the cache directory, the state database and the message search index, but not the crypto database.
     */
    suspend fun getCacheSize(): Long

    /** Returns the size of each individual store the SDK maintains for this session, as reported by the SDK. */
    suspend fun getDatabaseSizes(): Result<SdkStoreSizes>

    /**
     * Deletes the cached data of this session and destroys the client.
     * The instance is unusable afterwards, so callers are expected to restart the session, unlike with [logout] the session itself is kept.
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
     * Retrieve the profile of the logged in user, emitting it to [userProfile] and persisting it in the local session store on success.
     */
    suspend fun getUserProfile(): Result<MatrixUser>

    /**
     * Returns the web URL where the user can manage their account on the authentication provider, or `null` if the homeserver exposes none.
     *
     * @param action an optional section of the account management page to open directly, such as changing the password.
     */
    suspend fun getAccountManagementUrl(action: AccountManagementAction?): Result<String?>

    /**
     * Uploads arbitrary content to the homeserver's media repository.
     *
     * @param mimeType the MIME type of the content being uploaded.
     * @param data the raw bytes to upload.
     * @return the `mxc://` URI the content is now available at.
     */
    suspend fun uploadMedia(mimeType: String, data: ByteArray): Result<String>

    /**
     * Get a room info flow for a given room ID.
     * The flow will emit a new value whenever the room info is updated, skipping duplicates, and an empty [Optional] if the room is not found.
     *
     * @param roomId the id of the room to observe.
     */
    fun getRoomInfoFlow(roomId: RoomId): Flow<Optional<RoomInfo>>

    /**
     * Whether [userId] is the logged in user, `false` for a `null` value.
     *
     * @param userId the user id to compare against this session's user.
     */
    fun isMe(userId: UserId?) = userId == sessionId

    /**
     * Adds a room to the account's list of recently visited rooms, which backs the room suggestions when sharing or forwarding.
     *
     * @param roomId the id of the room that was just visited.
     */
    suspend fun trackRecentlyVisitedRoom(roomId: RoomId): Result<Unit>

    /** Returns the recently visited rooms of the account, most recent first. */
    suspend fun getRecentlyVisitedRooms(): Result<List<RoomId>>

    /**
     * Resolves the given room alias to a roomID (and a list of servers), if possible.
     *
     * @param roomAlias the room alias to resolve.
     * @return the resolved room alias if any, an empty result if not found, or an error if the resolution failed.
     */
    suspend fun resolveRoomAlias(roomAlias: RoomAlias): Result<Optional<ResolvedRoomAlias>>

    /**
     * Enables or disables the sending queue, according to the given parameter.
     *
     * The sending queue automatically disables itself whenever sending an
     * event with it failed (e.g. sending an event via the Timeline),
     * so it's required to manually re-enable it as soon as
     * connectivity is back on the device.
     *
     * @param enabled whether the send queues of every room should be enabled.
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
     * Get a room preview for a given room ID or alias. This is especially useful for rooms that the user is not a member of, or hasn't joined yet.
     *
     * @param roomIdOrAlias the id or the alias of the room to preview.
     * @param serverNames servers to ask about the room, used when it cannot be resolved locally.
     */
    suspend fun getRoomPreview(roomIdOrAlias: RoomIdOrAlias, serverNames: List<String>): Result<NotJoinedRoom>

    /**
     * Returns the currently used sliding sync version.
     */
    suspend fun currentSlidingSyncVersion(): Result<SlidingSyncVersion>

    /** Whether this account can be deactivated from the app, which depends on the authentication method used by the homeserver. */
    fun canDeactivateAccount(): Boolean

    /**
     * Deactivates the account on the homeserver, then destroys this client and deletes every local trace of the session.
     * The password is only sent if the homeserver asks for re-authentication, and on failure the session is left untouched.
     *
     * @param password the account password, required to confirm the deactivation.
     * @param eraseData whether the homeserver should also erase the messages the user sent.
     */
    suspend fun deactivateAccount(password: String, eraseData: Boolean): Result<Unit>

    /**
     * Whether the homeserver supports reporting a whole room, rather than individual events.
     * Returns `false` if the capability cannot be determined.
     */
    suspend fun canReportRoom(): Boolean

    /**
     * Return true if Livekit Rtc is supported, i.e. if Element Call is available.
     */
    suspend fun isLivekitRtcSupported(): Boolean

    /**
     * Returns the maximum file upload size allowed by the Matrix server.
     */
    suspend fun getMaxFileUploadSize(): Result<Long>

    /**
     * Returns the list of shared recent emoji reactions for this account.
     */
    suspend fun getRecentEmojis(): Result<List<String>>

    /**
     * Adds an emoji to the list of recent emoji reactions for this account.
     *
     * @param emoji the emoji that was just used.
     */
    suspend fun addRecentEmoji(emoji: String): Result<Unit>

    /**
     * Returns the raw JSON content of the global account data event of type [eventType], or null if it is not set.
     *
     * @param eventType the Matrix event type of the account data to read, for instance `m.direct`.
     */
    suspend fun getAccountData(eventType: String): Result<String?>

    /**
     * Sets the global account data event of type [eventType] to the raw JSON [content].
     *
     * @param eventType the Matrix event type of the account data to write.
     * @param content the new content, as a raw JSON object.
     */
    suspend fun setAccountData(eventType: String, content: String): Result<Unit>

    /**
     * Marks the room with the provided [roomId] as read, sending a fully read receipt for [eventId].
     *
     * This method should be used with caution as providing the [eventId] ourselves can result in incorrect read receipts.
     * Use [Timeline.markAsRead] instead when possible.
     *
     * @param roomId the id of the room to mark as read.
     * @param eventId the event up to which the room is considered read.
     */
    suspend fun markRoomAsFullyRead(roomId: RoomId, eventId: EventId): Result<Unit>

    /**
     * Mark all joined rooms as read by sending public, private and fully-read receipts
     * on each room's latest event. Per-room errors are logged and skipped by the SDK.
     */
    suspend fun markAllRoomsAsRead(): Result<Unit>

    /**
     * Check if linking a new device using QrCode is supported by the server.
     */
    suspend fun canLinkNewDevice(): Result<Boolean>

    /**
     * Create a handler to link a new mobile device, i.e. a device capable of scanning QrCodes.
     */
    fun createLinkMobileHandler(): Result<LinkMobileHandler>

    /**
     * Create a handler to link a new desktop device, i.e. a device not capable of scanning QrCodes.
     */
    fun createLinkDesktopHandler(): Result<LinkDesktopHandler>

    /**
     * Performs a database optimization that should flush cached data and improve performance.
     * A periodic background job already calls this for every session, so callers rarely need to trigger it by hand.
     */
    suspend fun performDatabaseVacuum(): Result<Unit>

    /**
     * Resets the cached client `well-known` config by the SDK.
     */
    suspend fun resetWellKnownConfig(): Result<Unit>

    /** Returns a provider for the capabilities the homeserver advertises, such as whether the display name can be changed. */
    fun homeserverCapabilities(): HomeserverCapabilitiesProvider
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
