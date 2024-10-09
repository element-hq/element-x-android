/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.room

import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.identity.IdentityStateChange
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.MediaUploadHandler
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.IntentionalMention
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.MatrixRoomNotificationSettingsState
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.draft.ComposerDraft
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.room.powerlevels.MatrixRoomPowerLevels
import io.element.android.libraries.matrix.api.room.powerlevels.UserRoleChange
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.widget.MatrixWidgetDriver
import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.media.FakeMediaUploadHandler
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class FakeMatrixRoom(
    override val sessionId: SessionId = A_SESSION_ID,
    override val roomId: RoomId = A_ROOM_ID,
    override val displayName: String = "",
    override val topic: String? = null,
    override val avatarUrl: String? = null,
    override var isEncrypted: Boolean = false,
    override val alias: RoomAlias? = null,
    override val alternativeAliases: List<RoomAlias> = emptyList(),
    override val isPublic: Boolean = true,
    override val isSpace: Boolean = false,
    override val isDirect: Boolean = false,
    override val joinedMemberCount: Long = 123L,
    override val activeMemberCount: Long = 234L,
    val notificationSettingsService: NotificationSettingsService = FakeNotificationSettingsService(),
    override val liveTimeline: Timeline = FakeTimeline(),
    private var roomPermalinkResult: () -> Result<String> = { lambdaError() },
    private var eventPermalinkResult: (EventId) -> Result<String> = { lambdaError() },
    private val sendCallNotificationIfNeededResult: () -> Result<Unit> = { lambdaError() },
    private val userDisplayNameResult: (UserId) -> Result<String?> = { lambdaError() },
    private val userAvatarUrlResult: () -> Result<String?> = { lambdaError() },
    private val userRoleResult: () -> Result<RoomMember.Role> = { lambdaError() },
    private val getUpdatedMemberResult: (UserId) -> Result<RoomMember> = { lambdaError() },
    private val joinRoomResult: () -> Result<Unit> = { lambdaError() },
    private val inviteUserResult: (UserId) -> Result<Unit> = { lambdaError() },
    private val canInviteResult: (UserId) -> Result<Boolean> = { lambdaError() },
    private val canKickResult: (UserId) -> Result<Boolean> = { lambdaError() },
    private val canBanResult: (UserId) -> Result<Boolean> = { lambdaError() },
    private val canRedactOwnResult: (UserId) -> Result<Boolean> = { lambdaError() },
    private val canRedactOtherResult: (UserId) -> Result<Boolean> = { lambdaError() },
    private val canSendStateResult: (UserId, StateEventType) -> Result<Boolean> = { _, _ -> lambdaError() },
    private val canUserSendMessageResult: (UserId, MessageEventType) -> Result<Boolean> = { _, _ -> lambdaError() },
    private val sendMediaResult: (ProgressCallback?) -> Result<FakeMediaUploadHandler> = { lambdaError() },
    private val setNameResult: (String) -> Result<Unit> = { lambdaError() },
    private val setTopicResult: (String) -> Result<Unit> = { lambdaError() },
    private val updateAvatarResult: (String, ByteArray) -> Result<Unit> = { _, _ -> lambdaError() },
    private val removeAvatarResult: () -> Result<Unit> = { lambdaError() },
    private val editMessageLambda: (EventId, String, String?, List<IntentionalMention>) -> Result<Unit> = { _, _, _, _ -> lambdaError() },
    private val sendMessageResult: (String, String?, List<IntentionalMention>) -> Result<Unit> = { _, _, _ -> lambdaError() },
    private val updateUserRoleResult: () -> Result<Unit> = { lambdaError() },
    private val toggleReactionResult: (String, UniqueId) -> Result<Unit> = { _, _ -> lambdaError() },
    private val retrySendMessageResult: (TransactionId) -> Result<Unit> = { lambdaError() },
    private val cancelSendResult: (TransactionId) -> Result<Unit> = { lambdaError() },
    private val forwardEventResult: (EventId, List<RoomId>) -> Result<Unit> = { _, _ -> lambdaError() },
    private val reportContentResult: (EventId, String, UserId?) -> Result<Unit> = { _, _, _ -> lambdaError() },
    private val kickUserResult: (UserId, String?) -> Result<Unit> = { _, _ -> lambdaError() },
    private val banUserResult: (UserId, String?) -> Result<Unit> = { _, _ -> lambdaError() },
    private val unBanUserResult: (UserId, String?) -> Result<Unit> = { _, _ -> lambdaError() },
    private val sendLocationResult: (String, String, String?, Int?, AssetType?) -> Result<Unit> = { _, _, _, _, _ -> lambdaError() },
    private val createPollResult: (String, List<String>, Int, PollKind) -> Result<Unit> = { _, _, _, _ -> lambdaError() },
    private val editPollResult: (EventId, String, List<String>, Int, PollKind) -> Result<Unit> = { _, _, _, _, _ -> lambdaError() },
    private val sendPollResponseResult: (EventId, List<String>) -> Result<Unit> = { _, _ -> lambdaError() },
    private val endPollResult: (EventId, String) -> Result<Unit> = { _, _ -> lambdaError() },
    private val progressCallbackValues: List<Pair<Long, Long>> = emptyList(),
    private val generateWidgetWebViewUrlResult: (MatrixWidgetSettings, String, String?, String?) -> Result<String> = { _, _, _, _ -> lambdaError() },
    private val getWidgetDriverResult: (MatrixWidgetSettings) -> Result<MatrixWidgetDriver> = { lambdaError() },
    private val canUserTriggerRoomNotificationResult: (UserId) -> Result<Boolean> = { lambdaError() },
    private val canUserJoinCallResult: (UserId) -> Result<Boolean> = { lambdaError() },
    private val canUserPinUnpinResult: (UserId) -> Result<Boolean> = { lambdaError() },
    private val setIsFavoriteResult: (Boolean) -> Result<Unit> = { lambdaError() },
    private val powerLevelsResult: () -> Result<MatrixRoomPowerLevels> = { lambdaError() },
    private val updatePowerLevelsResult: () -> Result<Unit> = { lambdaError() },
    private val resetPowerLevelsResult: () -> Result<MatrixRoomPowerLevels> = { lambdaError() },
    private val typingNoticeResult: (Boolean) -> Result<Unit> = { lambdaError() },
    private val leaveRoomLambda: () -> Result<Unit> = { lambdaError() },
    private val updateMembersResult: () -> Unit = { lambdaError() },
    private val getMembersResult: (Int) -> Result<List<RoomMember>> = { lambdaError() },
    private val timelineFocusedOnEventResult: (EventId) -> Result<Timeline> = { lambdaError() },
    private val pinnedEventsTimelineResult: () -> Result<Timeline> = { lambdaError() },
    private val setSendQueueEnabledLambda: (Boolean) -> Unit = { _: Boolean -> },
    private val saveComposerDraftLambda: (ComposerDraft) -> Result<Unit> = { _: ComposerDraft -> Result.success(Unit) },
    private val loadComposerDraftLambda: () -> Result<ComposerDraft?> = { Result.success<ComposerDraft?>(null) },
    private val clearComposerDraftLambda: () -> Result<Unit> = { Result.success(Unit) },
    private val subscribeToSyncLambda: () -> Unit = { lambdaError() },
    private val ignoreDeviceTrustAndResendResult: (Map<UserId, List<DeviceId>>, TransactionId) -> Result<Unit> = { _, _ -> lambdaError() },
    private val withdrawVerificationAndResendResult: (List<UserId>, TransactionId) -> Result<Unit> = { _, _ -> lambdaError() },
) : MatrixRoom {
    private val _roomInfoFlow: MutableSharedFlow<MatrixRoomInfo> = MutableSharedFlow(replay = 1)
    override val roomInfoFlow: Flow<MatrixRoomInfo> = _roomInfoFlow

    fun givenRoomInfo(roomInfo: MatrixRoomInfo) {
        _roomInfoFlow.tryEmit(roomInfo)
    }

    private val _roomTypingMembersFlow: MutableSharedFlow<List<UserId>> = MutableSharedFlow(replay = 1)
    override val roomTypingMembersFlow: Flow<List<UserId>> = _roomTypingMembersFlow

    fun givenRoomTypingMembers(typingMembers: List<UserId>) {
        _roomTypingMembersFlow.tryEmit(typingMembers)
    }

    private val _identityStateChangesFlow: MutableSharedFlow<List<IdentityStateChange>> = MutableSharedFlow(replay = 1)
    override val identityStateChangesFlow: Flow<List<IdentityStateChange>> = _identityStateChangesFlow

    fun emitIdentityStateChanges(identityStateChanges: List<IdentityStateChange>) {
        _identityStateChangesFlow.tryEmit(identityStateChanges)
    }

    override val membersStateFlow: MutableStateFlow<MatrixRoomMembersState> = MutableStateFlow(MatrixRoomMembersState.Unknown)

    override val roomNotificationSettingsStateFlow: MutableStateFlow<MatrixRoomNotificationSettingsState> =
        MutableStateFlow(MatrixRoomNotificationSettingsState.Unknown)

    override suspend fun updateMembers() = updateMembersResult()

    override suspend fun getUpdatedMember(userId: UserId): Result<RoomMember> {
        return getUpdatedMemberResult(userId)
    }

    override suspend fun getMembers(limit: Int): Result<List<RoomMember>> {
        return getMembersResult(limit)
    }

    override suspend fun updateRoomNotificationSettings(): Result<Unit> = simulateLongTask {
        val notificationSettings = notificationSettingsService.getRoomNotificationSettings(roomId, isEncrypted, isOneToOne).getOrThrow()
        roomNotificationSettingsStateFlow.value = MatrixRoomNotificationSettingsState.Ready(notificationSettings)
        return Result.success(Unit)
    }

    fun enableEncryption() {
        isEncrypted = true
        emitSyncUpdate()
    }

    private val _syncUpdateFlow = MutableStateFlow(0L)
    override val syncUpdateFlow: StateFlow<Long> = _syncUpdateFlow.asStateFlow()

    fun emitSyncUpdate() {
        _syncUpdateFlow.tryEmit(_syncUpdateFlow.value + 1)
    }

    override suspend fun timelineFocusedOnEvent(eventId: EventId): Result<Timeline> = simulateLongTask {
        timelineFocusedOnEventResult(eventId)
    }

    override suspend fun pinnedEventsTimeline(): Result<Timeline> = simulateLongTask {
        pinnedEventsTimelineResult()
    }

    override suspend fun subscribeToSync() {
        subscribeToSyncLambda()
    }

    override suspend fun powerLevels(): Result<MatrixRoomPowerLevels> {
        return powerLevelsResult()
    }

    override suspend fun updatePowerLevels(matrixRoomPowerLevels: MatrixRoomPowerLevels): Result<Unit> = simulateLongTask {
        updatePowerLevelsResult()
    }

    override suspend fun resetPowerLevels(): Result<MatrixRoomPowerLevels> = simulateLongTask {
        resetPowerLevelsResult()
    }

    override fun destroy() = Unit

    override suspend fun userDisplayName(userId: UserId): Result<String?> = simulateLongTask {
        userDisplayNameResult(userId)
    }

    override suspend fun userAvatarUrl(userId: UserId): Result<String?> = simulateLongTask {
        userAvatarUrlResult()
    }

    override suspend fun userRole(userId: UserId): Result<RoomMember.Role> {
        return userRoleResult()
    }

    override suspend fun updateUsersRoles(changes: List<UserRoleChange>): Result<Unit> {
        return updateUserRoleResult()
    }

    override suspend fun editMessage(eventId: EventId, body: String, htmlBody: String?, intentionalMentions: List<IntentionalMention>) = simulateLongTask {
        editMessageLambda(eventId, body, htmlBody, intentionalMentions)
    }

    override suspend fun sendMessage(body: String, htmlBody: String?, intentionalMentions: List<IntentionalMention>) = simulateLongTask {
        sendMessageResult(body, htmlBody, intentionalMentions)
    }

    override suspend fun toggleReaction(emoji: String, uniqueId: UniqueId): Result<Unit> {
        return toggleReactionResult(emoji, uniqueId)
    }

    override suspend fun retrySendMessage(transactionId: TransactionId): Result<Unit> = simulateLongTask {
        return retrySendMessageResult(transactionId)
    }

    override suspend fun cancelSend(transactionId: TransactionId): Result<Unit> {
        return cancelSendResult(transactionId)
    }

    override suspend fun getPermalink(): Result<String> {
        return roomPermalinkResult()
    }

    override suspend fun getPermalinkFor(eventId: EventId): Result<String> {
        return eventPermalinkResult(eventId)
    }

    override suspend fun leave(): Result<Unit> {
        return leaveRoomLambda()
    }

    override suspend fun join(): Result<Unit> {
        return joinRoomResult()
    }

    override suspend fun inviteUserById(id: UserId): Result<Unit> = simulateLongTask {
        inviteUserResult(id)
    }

    override suspend fun canUserBan(userId: UserId): Result<Boolean> {
        return canBanResult(userId)
    }

    override suspend fun canUserKick(userId: UserId): Result<Boolean> {
        return canKickResult(userId)
    }

    override suspend fun canUserInvite(userId: UserId): Result<Boolean> {
        return canInviteResult(userId)
    }

    override suspend fun canUserRedactOwn(userId: UserId): Result<Boolean> {
        return canRedactOwnResult(userId)
    }

    override suspend fun canUserRedactOther(userId: UserId): Result<Boolean> {
        return canRedactOtherResult(userId)
    }

    override suspend fun canUserSendState(userId: UserId, type: StateEventType): Result<Boolean> {
        return canSendStateResult(userId, type)
    }

    override suspend fun canUserSendMessage(userId: UserId, type: MessageEventType): Result<Boolean> {
        return canUserSendMessageResult(userId, type)
    }

    override suspend fun canUserTriggerRoomNotification(userId: UserId): Result<Boolean> {
        return canUserTriggerRoomNotificationResult(userId)
    }

    override suspend fun canUserJoinCall(userId: UserId): Result<Boolean> {
        return canUserJoinCallResult(userId)
    }

    override suspend fun canUserPinUnpin(userId: UserId): Result<Boolean> {
        return canUserPinUnpinResult(userId)
    }

    override suspend fun sendImage(
        file: File,
        thumbnailFile: File?,
        imageInfo: ImageInfo,
        body: String?,
        formattedBody: String?,
        progressCallback: ProgressCallback?
    ): Result<MediaUploadHandler> = fakeSendMedia(progressCallback)

    override suspend fun sendVideo(
        file: File,
        thumbnailFile: File?,
        videoInfo: VideoInfo,
        body: String?,
        formattedBody: String?,
        progressCallback: ProgressCallback?
    ): Result<MediaUploadHandler> = fakeSendMedia(
        progressCallback
    )

    override suspend fun sendAudio(
        file: File,
        audioInfo: AudioInfo,
        progressCallback: ProgressCallback?
    ): Result<MediaUploadHandler> = fakeSendMedia(progressCallback)

    override suspend fun sendFile(
        file: File,
        fileInfo: FileInfo,
        progressCallback: ProgressCallback?
    ): Result<MediaUploadHandler> = fakeSendMedia(progressCallback)

    override suspend fun forwardEvent(eventId: EventId, roomIds: List<RoomId>): Result<Unit> = simulateLongTask {
        forwardEventResult(eventId, roomIds)
    }

    private suspend fun fakeSendMedia(progressCallback: ProgressCallback?): Result<MediaUploadHandler> = simulateLongTask {
        progressCallbackValues.forEach { (current, total) ->
            progressCallback?.onProgress(current, total)
            delay(1)
        }
        sendMediaResult(progressCallback)
    }

    override suspend fun updateAvatar(mimeType: String, data: ByteArray): Result<Unit> = simulateLongTask {
        updateAvatarResult(mimeType, data)
    }

    override suspend fun removeAvatar(): Result<Unit> = simulateLongTask {
        removeAvatarResult()
    }

    override suspend fun setName(name: String): Result<Unit> = simulateLongTask {
        setNameResult(name)
    }

    override suspend fun setTopic(topic: String): Result<Unit> = simulateLongTask {
        setTopicResult(topic)
    }

    override suspend fun reportContent(
        eventId: EventId,
        reason: String,
        blockUserId: UserId?
    ): Result<Unit> = simulateLongTask {
        return reportContentResult(eventId, reason, blockUserId)
    }

    override suspend fun kickUser(userId: UserId, reason: String?): Result<Unit> {
        return kickUserResult(userId, reason)
    }

    override suspend fun banUser(userId: UserId, reason: String?): Result<Unit> {
        return banUserResult(userId, reason)
    }

    override suspend fun unbanUser(userId: UserId, reason: String?): Result<Unit> {
        return unBanUserResult(userId, reason)
    }

    override suspend fun setIsFavorite(isFavorite: Boolean): Result<Unit> {
        return setIsFavoriteResult(isFavorite)
    }

    val markAsReadCalls = mutableListOf<ReceiptType>()

    override suspend fun markAsRead(receiptType: ReceiptType): Result<Unit> {
        markAsReadCalls.add(receiptType)
        return Result.success(Unit)
    }

    var setUnreadFlagCalls = mutableListOf<Boolean>()
        private set

    override suspend fun setUnreadFlag(isUnread: Boolean): Result<Unit> {
        setUnreadFlagCalls.add(isUnread)
        return Result.success(Unit)
    }

    override suspend fun sendLocation(
        body: String,
        geoUri: String,
        description: String?,
        zoomLevel: Int?,
        assetType: AssetType?,
    ): Result<Unit> = simulateLongTask {
        return sendLocationResult(
            body,
            geoUri,
            description,
            zoomLevel,
            assetType,
        )
    }

    override suspend fun createPoll(
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind
    ): Result<Unit> = simulateLongTask {
        return createPollResult(
            question,
            answers,
            maxSelections,
            pollKind,
        )
    }

    override suspend fun editPoll(
        pollStartId: EventId,
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind
    ): Result<Unit> = simulateLongTask {
        return editPollResult(
            pollStartId,
            question,
            answers,
            maxSelections,
            pollKind,
        )
    }

    override suspend fun sendPollResponse(
        pollStartId: EventId,
        answers: List<String>
    ): Result<Unit> = simulateLongTask {
        return sendPollResponseResult(pollStartId, answers)
    }

    override suspend fun endPoll(
        pollStartId: EventId,
        text: String
    ): Result<Unit> = simulateLongTask {
        return endPollResult(pollStartId, text)
    }

    override suspend fun sendVoiceMessage(
        file: File,
        audioInfo: AudioInfo,
        waveform: List<Float>,
        progressCallback: ProgressCallback?
    ): Result<MediaUploadHandler> = fakeSendMedia(progressCallback)

    override suspend fun typingNotice(isTyping: Boolean): Result<Unit> {
        return typingNoticeResult(isTyping)
    }

    override suspend fun generateWidgetWebViewUrl(
        widgetSettings: MatrixWidgetSettings,
        clientId: String,
        languageTag: String?,
        theme: String?,
    ): Result<String> = generateWidgetWebViewUrlResult(
        widgetSettings,
        clientId,
        languageTag,
        theme,
    )

    override suspend fun sendCallNotificationIfNeeded(): Result<Unit> {
        return sendCallNotificationIfNeededResult()
    }

    override suspend fun setSendQueueEnabled(enabled: Boolean) = setSendQueueEnabledLambda(enabled)

    override suspend fun saveComposerDraft(composerDraft: ComposerDraft) = saveComposerDraftLambda(composerDraft)

    override suspend fun loadComposerDraft() = loadComposerDraftLambda()

    override suspend fun clearComposerDraft() = clearComposerDraftLambda()

    override fun getWidgetDriver(widgetSettings: MatrixWidgetSettings): Result<MatrixWidgetDriver> {
        return getWidgetDriverResult(widgetSettings)
    }

    override suspend fun ignoreDeviceTrustAndResend(devices: Map<UserId, List<DeviceId>>, transactionId: TransactionId): Result<Unit> = simulateLongTask {
        return ignoreDeviceTrustAndResendResult(devices, transactionId)
    }

    override suspend fun withdrawVerificationAndResend(userIds: List<UserId>, transactionId: TransactionId): Result<Unit> = simulateLongTask {
        return withdrawVerificationAndResendResult(userIds, transactionId)
    }

    fun givenRoomMembersState(state: MatrixRoomMembersState) {
        membersStateFlow.value = state
    }
}

fun aRoomInfo(
    id: RoomId = A_ROOM_ID,
    name: String? = A_ROOM_NAME,
    rawName: String? = name,
    topic: String? = "A topic",
    avatarUrl: String? = AN_AVATAR_URL,
    isDirect: Boolean = false,
    isPublic: Boolean = true,
    isSpace: Boolean = false,
    isTombstoned: Boolean = false,
    isFavorite: Boolean = false,
    canonicalAlias: RoomAlias? = null,
    alternativeAliases: List<RoomAlias> = emptyList(),
    currentUserMembership: CurrentUserMembership = CurrentUserMembership.JOINED,
    inviter: RoomMember? = null,
    activeMembersCount: Long = 1,
    invitedMembersCount: Long = 0,
    joinedMembersCount: Long = 1,
    highlightCount: Long = 0,
    notificationCount: Long = 0,
    userDefinedNotificationMode: RoomNotificationMode? = null,
    hasRoomCall: Boolean = false,
    userPowerLevels: ImmutableMap<UserId, Long> = persistentMapOf(),
    activeRoomCallParticipants: List<UserId> = emptyList(),
    heroes: List<MatrixUser> = emptyList(),
    pinnedEventIds: List<EventId> = emptyList(),
    roomCreator: UserId? = null,
) = MatrixRoomInfo(
    id = id,
    name = name,
    rawName = rawName,
    topic = topic,
    avatarUrl = avatarUrl,
    isDirect = isDirect,
    isPublic = isPublic,
    isSpace = isSpace,
    isTombstoned = isTombstoned,
    isFavorite = isFavorite,
    canonicalAlias = canonicalAlias,
    alternativeAliases = alternativeAliases.toImmutableList(),
    currentUserMembership = currentUserMembership,
    inviter = inviter,
    activeMembersCount = activeMembersCount,
    invitedMembersCount = invitedMembersCount,
    joinedMembersCount = joinedMembersCount,
    highlightCount = highlightCount,
    notificationCount = notificationCount,
    userDefinedNotificationMode = userDefinedNotificationMode,
    hasRoomCall = hasRoomCall,
    userPowerLevels = userPowerLevels,
    activeRoomCallParticipants = activeRoomCallParticipants.toImmutableList(),
    heroes = heroes.toImmutableList(),
    pinnedEventIds = pinnedEventIds.toImmutableList(),
    creator = roomCreator,
)

fun defaultRoomPowerLevels() = MatrixRoomPowerLevels(
    ban = 50,
    invite = 0,
    kick = 50,
    sendEvents = 0,
    redactEvents = 50,
    roomName = 100,
    roomAvatar = 100,
    roomTopic = 100
)
