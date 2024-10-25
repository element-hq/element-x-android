/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.identity.IdentityStateChange
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.MediaUploadHandler
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.room.draft.ComposerDraft
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.room.powerlevels.MatrixRoomPowerLevels
import io.element.android.libraries.matrix.api.room.powerlevels.UserRoleChange
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.widget.MatrixWidgetDriver
import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.io.Closeable
import java.io.File

interface MatrixRoom : Closeable {
    val sessionId: SessionId
    val roomId: RoomId
    val displayName: String
    val alias: RoomAlias?
    val alternativeAliases: List<RoomAlias>
    val topic: String?
    val avatarUrl: String?
    val isEncrypted: Boolean
    val isSpace: Boolean
    val isDirect: Boolean
    val isPublic: Boolean
    val activeMemberCount: Long
    val joinedMemberCount: Long

    val roomInfoFlow: Flow<MatrixRoomInfo>
    val roomTypingMembersFlow: Flow<List<UserId>>
    val identityStateChangesFlow: Flow<List<IdentityStateChange>>

    /**
     * A one-to-one is a room with exactly 2 members.
     * See [the Matrix spec](https://spec.matrix.org/latest/client-server-api/#default-underride-rules).
     */
    val isOneToOne: Boolean get() = activeMemberCount == 2L

    /**
     * The current loaded members as a StateFlow.
     * Initial value is [MatrixRoomMembersState.Unknown].
     * To update them you should call [updateMembers].
     */
    val membersStateFlow: StateFlow<MatrixRoomMembersState>

    val roomNotificationSettingsStateFlow: StateFlow<MatrixRoomNotificationSettingsState>

    /**
     * Try to load the room members and update the membersFlow.
     */
    suspend fun updateMembers()

    /**
     * Get the members of the room. Note: generally this should not be used, please use
     * [membersStateFlow] and [updateMembers] instead.
     */
    suspend fun getMembers(limit: Int = 5): Result<List<RoomMember>>

    /**
     * Will return an updated member or an error.
     */
    suspend fun getUpdatedMember(userId: UserId): Result<RoomMember>

    suspend fun updateRoomNotificationSettings(): Result<Unit>

    val syncUpdateFlow: StateFlow<Long>

    /**
     * The live timeline of the room. Must be used to send Event to a room.
     */
    val liveTimeline: Timeline

    /**
     * Create a new timeline, focused on the provided Event.
     * Should not be used directly, see `TimelineController` to manage the various timelines.
     */
    suspend fun timelineFocusedOnEvent(eventId: EventId): Result<Timeline>

    /**
     * Create a new timeline for the pinned events of the room.
     */
    suspend fun pinnedEventsTimeline(): Result<Timeline>

    fun destroy()

    suspend fun subscribeToSync()

    suspend fun powerLevels(): Result<MatrixRoomPowerLevels>

    suspend fun updatePowerLevels(matrixRoomPowerLevels: MatrixRoomPowerLevels): Result<Unit>

    suspend fun resetPowerLevels(): Result<MatrixRoomPowerLevels>

    suspend fun userRole(userId: UserId): Result<RoomMember.Role>

    suspend fun updateUsersRoles(changes: List<UserRoleChange>): Result<Unit>

    suspend fun userDisplayName(userId: UserId): Result<String?>

    suspend fun userAvatarUrl(userId: UserId): Result<String?>

    suspend fun sendMessage(body: String, htmlBody: String?, intentionalMentions: List<IntentionalMention>): Result<Unit>

    suspend fun editMessage(eventId: EventId, body: String, htmlBody: String?, intentionalMentions: List<IntentionalMention>): Result<Unit>

    suspend fun sendImage(
        file: File,
        thumbnailFile: File?,
        imageInfo: ImageInfo,
        body: String?,
        formattedBody: String?,
        progressCallback: ProgressCallback?
    ): Result<MediaUploadHandler>

    suspend fun sendVideo(
        file: File,
        thumbnailFile: File?,
        videoInfo: VideoInfo,
        body: String?,
        formattedBody: String?,
        progressCallback: ProgressCallback?
    ): Result<MediaUploadHandler>

    suspend fun sendAudio(file: File, audioInfo: AudioInfo, progressCallback: ProgressCallback?): Result<MediaUploadHandler>

    suspend fun sendFile(file: File, fileInfo: FileInfo, progressCallback: ProgressCallback?): Result<MediaUploadHandler>

    suspend fun toggleReaction(emoji: String, eventOrTransactionId: EventOrTransactionId): Result<Unit>

    suspend fun forwardEvent(eventId: EventId, roomIds: List<RoomId>): Result<Unit>

    suspend fun retrySendMessage(transactionId: TransactionId): Result<Unit>

    suspend fun cancelSend(transactionId: TransactionId): Result<Unit>

    suspend fun leave(): Result<Unit>

    suspend fun join(): Result<Unit>

    suspend fun inviteUserById(id: UserId): Result<Unit>

    suspend fun canUserInvite(userId: UserId): Result<Boolean>

    suspend fun canUserKick(userId: UserId): Result<Boolean>

    suspend fun canUserBan(userId: UserId): Result<Boolean>

    suspend fun canUserRedactOwn(userId: UserId): Result<Boolean>

    suspend fun canUserRedactOther(userId: UserId): Result<Boolean>

    suspend fun canUserSendState(userId: UserId, type: StateEventType): Result<Boolean>

    suspend fun canUserSendMessage(userId: UserId, type: MessageEventType): Result<Boolean>

    suspend fun canUserTriggerRoomNotification(userId: UserId): Result<Boolean>

    suspend fun canUserPinUnpin(userId: UserId): Result<Boolean>

    suspend fun canUserJoinCall(userId: UserId): Result<Boolean> =
        canUserSendState(userId, StateEventType.CALL_MEMBER)

    suspend fun updateAvatar(mimeType: String, data: ByteArray): Result<Unit>

    suspend fun removeAvatar(): Result<Unit>

    suspend fun setName(name: String): Result<Unit>

    suspend fun setTopic(topic: String): Result<Unit>

    suspend fun reportContent(eventId: EventId, reason: String, blockUserId: UserId?): Result<Unit>

    suspend fun kickUser(userId: UserId, reason: String? = null): Result<Unit>

    suspend fun banUser(userId: UserId, reason: String? = null): Result<Unit>

    suspend fun unbanUser(userId: UserId, reason: String? = null): Result<Unit>

    suspend fun setIsFavorite(isFavorite: Boolean): Result<Unit>

    /**
     * Mark the room as read by trying to attach an unthreaded read receipt to the latest room event.
     * @param receiptType The type of receipt to send.
     */
    suspend fun markAsRead(receiptType: ReceiptType): Result<Unit>

    /**
     * Sets a flag on the room to indicate that the user has explicitly marked it as unread, or reverts the flag.
     * @param isUnread true to mark the room as unread, false to remove the flag.
     *
     */
    suspend fun setUnreadFlag(isUnread: Boolean): Result<Unit>

    /**
     * Share a location message in the room.
     *
     * @param body A human readable textual representation of the location.
     * @param geoUri A geo URI (RFC 5870) representing the location e.g. `geo:51.5008,0.1247;u=35`.
     *  Respectively: latitude, longitude, and (optional) uncertainty.
     * @param description Optional description of the location to display to the user.
     * @param zoomLevel Optional zoom level to display the map at.
     * @param assetType Optional type of the location asset.
     *  Set to SENDER if sharing own location. Set to PIN if sharing any location.
     */
    suspend fun sendLocation(
        body: String,
        geoUri: String,
        description: String? = null,
        zoomLevel: Int? = null,
        assetType: AssetType? = null,
    ): Result<Unit>

    /**
     * Create a poll in the room.
     *
     * @param question The question to ask.
     * @param answers The list of answers.
     * @param maxSelections The maximum number of answers that can be selected.
     * @param pollKind The kind of poll to create.
     */
    suspend fun createPoll(
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind,
    ): Result<Unit>

    /**
     * Edit a poll in the room.
     *
     * @param pollStartId The event ID of the poll start event.
     * @param question The question to ask.
     * @param answers The list of answers.
     * @param maxSelections The maximum number of answers that can be selected.
     * @param pollKind The kind of poll to create.
     */
    suspend fun editPoll(
        pollStartId: EventId,
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind,
    ): Result<Unit>

    /**
     * Send a response to a poll.
     *
     * @param pollStartId The event ID of the poll start event.
     * @param answers The list of answer ids to send.
     */
    suspend fun sendPollResponse(pollStartId: EventId, answers: List<String>): Result<Unit>

    /**
     * Ends a poll in the room.
     *
     * @param pollStartId The event ID of the poll start event.
     * @param text Fallback text of the poll end event.
     */
    suspend fun endPoll(pollStartId: EventId, text: String): Result<Unit>

    suspend fun sendVoiceMessage(
        file: File,
        audioInfo: AudioInfo,
        waveform: List<Float>,
        progressCallback: ProgressCallback?
    ): Result<MediaUploadHandler>

    /**
     * Send a typing notification.
     * @param isTyping True if the user is typing, false otherwise.
     */
    suspend fun typingNotice(isTyping: Boolean): Result<Unit>

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
     * Get the permalink for the room.
     */
    suspend fun getPermalink(): Result<String>

    /**
     * Get the permalink for the provided [eventId].
     * @param eventId The event id to get the permalink for.
     * @return The permalink, or a failure.
     */
    suspend fun getPermalinkFor(eventId: EventId): Result<String>

    /**
     * Send an Element Call started notification if needed.
     */
    suspend fun sendCallNotificationIfNeeded(): Result<Unit>

    suspend fun setSendQueueEnabled(enabled: Boolean)

    /**
     * Store the given `ComposerDraft` in the state store of this room.
     */
    suspend fun saveComposerDraft(composerDraft: ComposerDraft): Result<Unit>

    /**
     * Retrieve the `ComposerDraft` stored in the state store for this room.
     */
    suspend fun loadComposerDraft(): Result<ComposerDraft?>

    /**
     * Clear the `ComposerDraft` stored in the state store for this room.
     */
    suspend fun clearComposerDraft(): Result<Unit>

    /**
     * Ignore the local trust for the given devices and resend messages that failed to send because said devices are unverified.
     *
     * @param devices The map of users identifiers to device identifiers received in the error
     * @param transactionId The send queue transaction identifier of the local echo the send error applies to.
     *
     */
    suspend fun ignoreDeviceTrustAndResend(devices: Map<UserId, List<DeviceId>>, transactionId: TransactionId): Result<Unit>

    /**
     * Remove verification requirements for the given users and
     * resend messages that failed to send because their identities were no longer verified.
     *
     * @param userIds The list of users identifiers received in the error.
     * @param transactionId The send queue transaction identifier of the local echo the send error applies to.
     *
     */
    suspend fun withdrawVerificationAndResend(userIds: List<UserId>, transactionId: TransactionId): Result<Unit>

    override fun close() = destroy()
}
