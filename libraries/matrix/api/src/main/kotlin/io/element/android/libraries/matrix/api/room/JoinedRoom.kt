/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.SendHandle
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.identity.IdentityStateChange
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.room.knock.KnockRequest
import io.element.android.libraries.matrix.api.room.location.LiveLocationShare
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevelsValues
import io.element.android.libraries.matrix.api.room.powerlevels.UserRoleChange
import io.element.android.libraries.matrix.api.room.threads.ThreadsListService
import io.element.android.libraries.matrix.api.roomdirectory.RoomVisibility
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.widget.MatrixWidgetDriver
import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * A room the current user has joined, adding to [BaseRoom] everything that requires membership: sending events, moderation and room settings.
 *
 * An instance owns SDK resources, in particular [liveTimeline], so it must be closed once no longer needed.
 */
interface JoinedRoom : BaseRoom {
    /**
     * A counter incremented every time a synced event is received on [liveTimeline], so callers can refresh data derived from the room.
     * It carries no meaning beyond "something changed", and is only updated while it has subscribers.
     */
    val syncUpdateFlow: StateFlow<Long>

    /** The members currently typing in the room, never including the current user; starts as an empty list. */
    val roomTypingMembersFlow: Flow<List<UserId>>

    /** Emits when the verification state of other members' identities changes, for instance after a member resets their identity. */
    val identityStateChangesFlow: Flow<List<IdentityStateChange>>

    /**
     * The notification settings that apply to this room.
     * Starts as [RoomNotificationSettingsState.Unknown]: [updateRoomNotificationSettings] must be called to populate it.
     */
    val roomNotificationSettingsStateFlow: StateFlow<RoomNotificationSettingsState>

    /**
     * The current knock requests in the room as a Flow.
     */
    val knockRequestsFlow: Flow<List<KnockRequest>>

    /**
     * The live timeline of the room. Must be used to send Event to a room.
     */
    val liveTimeline: Timeline

    /** Gives access to the paginated list of threads started in this room. */
    val threadsListService: ThreadsListService

    /**
     * Create a new timeline.
     * @param createTimelineParams contains parameters about how to filter the timeline. Will also configure the date separators.
     */
    suspend fun createTimeline(
        createTimelineParams: CreateTimelineParams,
    ): Result<Timeline>

    /**
     * Replaces the content of an event previously sent by the current user, by sending an edit of it.
     *
     * @param eventId the event to edit.
     * @param body the new content, as plain text.
     * @param htmlBody the new content as HTML, or `null` when the message has no formatted version.
     * @param intentionalMentions the users and rooms this new content deliberately mentions.
     */
    suspend fun editMessage(eventId: EventId, body: String, htmlBody: String?, intentionalMentions: List<IntentionalMention>): Result<Unit>

    /**
     * Send a typing notification.
     * @param isTyping True if the user is typing, false otherwise.
     */
    suspend fun typingNotice(isTyping: Boolean): Result<Unit>

    /**
     * Invites a user to this room.
     *
     * @param id the user to invite.
     */
    suspend fun inviteUserById(id: UserId): Result<Unit>

    /**
     * Uploads [data] as the new avatar of the room, not of the current user.
     *
     * @param mimeType the MIME type of the image, for instance `image/jpeg`.
     * @param data the raw bytes of the image.
     */
    suspend fun updateAvatar(mimeType: String, data: ByteArray): Result<Unit>

    /** Removes the avatar of the room. */
    suspend fun removeAvatar(): Result<Unit>

    /**
     * Refreshes [roomNotificationSettingsStateFlow], which moves to a pending state and then to a ready or error state.
     * The failure is reported through that flow as well as through the returned [Result].
     */
    suspend fun updateRoomNotificationSettings(): Result<Unit>

    /**
     * Update the canonical alias of the room.
     *
     * Note that publishing the alias in the room directory is done separately.
     */
    suspend fun updateCanonicalAlias(
        canonicalAlias: RoomAlias?,
        alternativeAliases: List<RoomAlias>
    ): Result<Unit>

    /**
     * Update the room's visibility in the room directory.
     *
     * @param roomVisibility whether the room should be listed publicly in the directory.
     */
    suspend fun updateRoomVisibility(roomVisibility: RoomVisibility): Result<Unit>

    /**
     * Update room history visibility for this room, i.e. how much of the past a new member is allowed to read.
     *
     * @param historyVisibility the new history visibility to apply.
     */
    suspend fun updateHistoryVisibility(historyVisibility: RoomHistoryVisibility): Result<Unit>

    /**
     * Publish a new room alias for this room in the room directory.
     *
     * Returns:
     * - `true` if the room alias didn't exist and it's now published.
     * - `false` if the room alias was already present so it couldn't be
     * published.
     *
     * @param roomAlias the alias to publish.
     */
    suspend fun publishRoomAliasInRoomDirectory(roomAlias: RoomAlias): Result<Boolean>

    /**
     * Remove an existing room alias for this room in the room directory.
     *
     * Returns:
     * - `true` if the room alias was present and it's now removed from the
     * room directory.
     * - `false` if the room alias didn't exist so it couldn't be removed.
     *
     * @param roomAlias the alias to remove.
     */
    suspend fun removeRoomAliasFromRoomDirectory(roomAlias: RoomAlias): Result<Boolean>

    /**
     * Enable End-to-end encryption in this room.
     */
    suspend fun enableEncryption(): Result<Unit>

    /**
     * Update the join rule for this room, i.e. who is allowed to join it.
     *
     * @param joinRule the new join rule to apply.
     */
    suspend fun updateJoinRule(joinRule: JoinRule): Result<Unit>

    /**
     * Changes the power level of individual members, which is how roles such as moderator or administrator are granted.
     *
     * @param changes the new power level to apply to each user.
     */
    suspend fun updateUsersRoles(changes: List<UserRoleChange>): Result<Unit>

    /**
     * Changes the power level required for each action in the room, such as inviting, kicking or changing the room name.
     *
     * @param roomPowerLevelsValues the new threshold for every action.
     */
    suspend fun updatePowerLevels(roomPowerLevelsValues: RoomPowerLevelsValues): Result<Unit>

    /** Restores the power levels required for each action to the defaults the room was created with. */
    suspend fun resetPowerLevels(): Result<Unit>

    /**
     * Changes the name of the room.
     *
     * @param name the new room name.
     */
    suspend fun setName(name: String): Result<Unit>

    /**
     * Changes the topic of the room.
     *
     * @param topic the new room topic.
     */
    suspend fun setTopic(topic: String): Result<Unit>

    /**
     * Reports an event to the homeserver moderators, optionally ignoring its sender at the same time.
     *
     * @param eventId the event being reported.
     * @param reason the explanation sent to the moderators.
     * @param blockUserId the sender to also add to the ignore list, or `null` to only report the event.
     */
    suspend fun reportContent(eventId: EventId, reason: String, blockUserId: UserId?): Result<Unit>

    /**
     * Removes a member from the room; they are able to join again unless they are also banned.
     *
     * @param userId the member to remove.
     * @param reason the reason recorded in the membership event, or `null` for none.
     */
    suspend fun kickUser(userId: UserId, reason: String? = null): Result<Unit>

    /**
     * Bans a user from the room, removing them if they are a member and preventing them from joining again.
     *
     * @param userId the user to ban.
     * @param reason the reason recorded in the membership event, or `null` for none.
     */
    suspend fun banUser(userId: UserId, reason: String? = null): Result<Unit>

    /**
     * Lifts the ban on a user, allowing them to join the room again; it does not invite them back.
     *
     * @param userId the user to unban.
     * @param reason the reason recorded in the membership event, or `null` for none.
     */
    suspend fun unbanUser(userId: UserId, reason: String? = null): Result<Unit>

    /**
     * Generates a Widget url to display in a [android.webkit.WebView] given the provided parameters.
     * @param widgetSettings The widget settings to use.
     * @param clientId The client id to use. It should be unique per app install.
     * @param languageTag The language tag to use. If null, the default language will be used.
     * @param theme The theme to use. If null, the default theme will be used.
     * @return The resulting url, or a failure.
     */
    suspend fun generateWidgetWebViewUrl(
        widgetSettings: MatrixWidgetSettings,
        clientId: String,
        languageTag: String?,
        theme: String?,
    ): Result<String>

    /**
     * Get a [MatrixWidgetDriver] for the provided [widgetSettings].
     * @param widgetSettings The widget settings to use.
     * @return The resulting [MatrixWidgetDriver], or a failure.
     */
    fun getWidgetDriver(widgetSettings: MatrixWidgetSettings): Result<MatrixWidgetDriver>

    /**
     * Enables or disables the send queue of this room only; see [io.element.android.libraries.matrix.api.MatrixClient.setAllSendQueuesEnabled]
     * for the session-wide equivalent and for why a queue disables itself.
     *
     * @param enabled whether the send queue of this room should be enabled.
     */
    suspend fun setSendQueueEnabled(enabled: Boolean)

    /**
     * Ignore the local trust for the given devices and resend messages that failed to send because said devices are unverified.
     *
     * @param devices The map of users identifiers to device identifiers received in the error
     * @param sendHandle The send queue handle of the local echo the send error applies to. It can be used to retry the upload.
     *
     */
    suspend fun ignoreDeviceTrustAndResend(devices: Map<UserId, List<DeviceId>>, sendHandle: SendHandle): Result<Unit>

    /**
     * Remove verification requirements for the given users and
     * resend messages that failed to send because their identities were no longer verified.
     *
     * @param userIds The list of users identifiers received in the error.
     * @param sendHandle The send queue handle of the local echo the send error applies to. It can be used to retry the upload.
     *
     */
    suspend fun withdrawVerificationAndResend(userIds: List<UserId>, sendHandle: SendHandle): Result<Unit>

    /**
     * Subscribe to a [Flow] of [SendQueueUpdate] related to this room.
     */
    fun subscribeToSendQueueUpdates(): Flow<SendQueueUpdate>

    /**
     * Subscribe to live location shares in this room.
     * @return Flow of list of active live location shares.
     */
    fun subscribeToLiveLocationShares(): Flow<List<LiveLocationShare>>

    /**
     * Start sharing live location in this room.
     * @param durationMillis How long to share location (in milliseconds).
     * @return Result containing the [EventId] of the beacon state event on success or an error on failure.
     */
    suspend fun startLiveLocationShare(durationMillis: Long): Result<EventId>

    /**
     * Stop sharing live location in this room.
     * @return Result indicating success or failure.
     */
    suspend fun stopLiveLocationShare(): Result<Unit>

    /**
     * Send a live location update while a live location share is active.
     * @param geoUri The geo URI (e.g., "geo:51.5074,-0.1278").
     * @return Result indicating success or failure.
     */
    suspend fun sendLiveLocation(geoUri: String): Result<Unit>

    /**
     * Sets the display name of the current user within this room.
     * This is different from the global setDisplayName which updates
     * the user's display name across all of their rooms.
     */
    suspend fun setOwnMemberDisplayName(displayName: String): Result<Unit>
}
