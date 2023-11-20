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

package io.element.android.libraries.matrix.test.room

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.MediaUploadHandler
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.MatrixRoomNotificationSettingsState
import io.element.android.libraries.matrix.api.room.Mention
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import io.element.android.libraries.matrix.api.widget.MatrixWidgetDriver
import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.media.FakeMediaUploadHandler
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.timeline.FakeMatrixTimeline
import io.element.android.libraries.matrix.test.widget.FakeWidgetDriver
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class FakeMatrixRoom(
    override val sessionId: SessionId = A_SESSION_ID,
    override val roomId: RoomId = A_ROOM_ID,
    override val name: String? = null,
    override val displayName: String = "",
    override val topic: String? = null,
    override val avatarUrl: String? = null,
    override val isEncrypted: Boolean = false,
    override val alias: String? = null,
    override val alternativeAliases: List<String> = emptyList(),
    override val isPublic: Boolean = true,
    override val isDirect: Boolean = false,
    override val isOneToOne: Boolean = false,
    override val joinedMemberCount: Long = 123L,
    override val activeMemberCount: Long = 234L,
    val notificationSettingsService: NotificationSettingsService = FakeNotificationSettingsService(),
    private val matrixTimeline: MatrixTimeline = FakeMatrixTimeline(),
    canRedact: Boolean = false,
) : MatrixRoom {

    private var ignoreResult: Result<Unit> = Result.success(Unit)
    private var unignoreResult: Result<Unit> = Result.success(Unit)
    private var userDisplayNameResult = Result.success<String?>(null)
    private var userAvatarUrlResult = Result.success<String?>(null)
    private var updateMembersResult: Result<Unit> = Result.success(Unit)
    private var joinRoomResult = Result.success(Unit)
    private var inviteUserResult = Result.success(Unit)
    private var canInviteResult = Result.success(true)
    private var canRedactResult = Result.success(canRedact)
    private val canSendStateResults = mutableMapOf<StateEventType, Result<Boolean>>()
    private val canSendEventResults = mutableMapOf<MessageEventType, Result<Boolean>>()
    private var sendMediaResult = Result.success(FakeMediaUploadHandler())
    private var setNameResult = Result.success(Unit)
    private var setTopicResult = Result.success(Unit)
    private var updateAvatarResult = Result.success(Unit)
    private var removeAvatarResult = Result.success(Unit)
    private var toggleReactionResult = Result.success(Unit)
    private var retrySendMessageResult = Result.success(Unit)
    private var cancelSendResult = Result.success(Unit)
    private var forwardEventResult = Result.success(Unit)
    private var reportContentResult = Result.success(Unit)
    private var sendLocationResult = Result.success(Unit)
    private var createPollResult = Result.success(Unit)
    private var sendPollResponseResult = Result.success(Unit)
    private var endPollResult = Result.success(Unit)
    private var progressCallbackValues = emptyList<Pair<Long, Long>>()
    private var generateWidgetWebViewUrlResult = Result.success("https://call.element.io")
    private var getWidgetDriverResult: Result<MatrixWidgetDriver> = Result.success(FakeWidgetDriver())
    private var canUserTriggerRoomNotificationResult: Result<Boolean> = Result.success(true)
    var sendMessageMentions = emptyList<Mention>()
    val editMessageCalls = mutableListOf<Pair<String, String?>>()

    var sendMediaCount = 0
        private set

    private val _myReactions = mutableSetOf<String>()
    val myReactions: Set<String> = _myReactions

    var retrySendMessageCount: Int = 0
        private set

    var cancelSendCount: Int = 0
        private set

    var reportedContentCount: Int = 0
        private set

    private val _sentLocations = mutableListOf<SendLocationInvocation>()
    val sentLocations: List<SendLocationInvocation> = _sentLocations

    private val _createPollInvocations = mutableListOf<CreatePollInvocation>()
    val createPollInvocations: List<CreatePollInvocation> = _createPollInvocations

    private val _sendPollResponseInvocations = mutableListOf<SendPollResponseInvocation>()
    val sendPollResponseInvocations: List<SendPollResponseInvocation> = _sendPollResponseInvocations

    private val _endPollInvocations = mutableListOf<EndPollInvocation>()
    val endPollInvocations: List<EndPollInvocation> = _endPollInvocations

    var invitedUserId: UserId? = null
        private set

    var newTopic: String? = null
        private set

    var newName: String? = null
        private set

    var newAvatarData: ByteArray? = null
        private set

    var removedAvatar: Boolean = false
        private set

    private var leaveRoomError: Throwable? = null

    private val _roomInfoFlow: MutableSharedFlow<MatrixRoomInfo> = MutableStateFlow(aRoomInfo())
    override val roomInfoFlow: Flow<MatrixRoomInfo> = _roomInfoFlow

    override val membersStateFlow: MutableStateFlow<MatrixRoomMembersState> = MutableStateFlow(MatrixRoomMembersState.Unknown)

    override val roomNotificationSettingsStateFlow: MutableStateFlow<MatrixRoomNotificationSettingsState> =
        MutableStateFlow(MatrixRoomNotificationSettingsState.Unknown)

    override suspend fun updateMembers(): Result<Unit> = simulateLongTask {
        updateMembersResult
    }

    override suspend fun updateRoomNotificationSettings(): Result<Unit> = simulateLongTask {
        val notificationSettings = notificationSettingsService.getRoomNotificationSettings(roomId, isEncrypted, isOneToOne).getOrThrow()
        roomNotificationSettingsStateFlow.value = MatrixRoomNotificationSettingsState.Ready(notificationSettings)
        return Result.success(Unit)
    }

    override val syncUpdateFlow: StateFlow<Long> = MutableStateFlow(0L)

    override val timeline: MatrixTimeline = matrixTimeline

    override suspend fun subscribeToSync() = Unit

    override suspend fun unsubscribeFromSync() = Unit

    override fun destroy() = Unit

    override suspend fun userDisplayName(userId: UserId): Result<String?> = simulateLongTask {
        userDisplayNameResult
    }

    override suspend fun userAvatarUrl(userId: UserId): Result<String?> = simulateLongTask {
        userAvatarUrlResult
    }

    override suspend fun sendMessage(body: String, htmlBody: String?, mentions: List<Mention>) = simulateLongTask {
        sendMessageMentions = mentions
        Result.success(Unit)
    }

    override suspend fun toggleReaction(emoji: String, eventId: EventId): Result<Unit> {
        if (toggleReactionResult.isFailure) {
            // Don't do the toggle if we failed
            return toggleReactionResult
        }

        if (_myReactions.contains(emoji)) {
            _myReactions.remove(emoji)
        } else {
            _myReactions.add(emoji)
        }

        return toggleReactionResult
    }

    override suspend fun retrySendMessage(transactionId: TransactionId): Result<Unit> {
        retrySendMessageCount++
        return retrySendMessageResult
    }

    override suspend fun cancelSend(transactionId: TransactionId): Result<Unit> {
        cancelSendCount++
        return cancelSendResult
    }

    override suspend fun editMessage(
        originalEventId: EventId?,
        transactionId: TransactionId?,
        body: String,
        htmlBody: String?,
        mentions: List<Mention>
    ): Result<Unit> {
        sendMessageMentions = mentions
        editMessageCalls += body to htmlBody
        return Result.success(Unit)
    }

    var replyMessageParameter: Pair<String, String?>? = null
        private set

    override suspend fun enterSpecialMode(eventId: EventId?): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun replyMessage(eventId: EventId, body: String, htmlBody: String?, mentions: List<Mention>): Result<Unit> {
        sendMessageMentions = mentions
        replyMessageParameter = body to htmlBody
        return Result.success(Unit)
    }

    var redactEventEventIdParam: EventId? = null
        private set

    override suspend fun redactEvent(eventId: EventId, reason: String?): Result<Unit> {
        redactEventEventIdParam = eventId
        return Result.success(Unit)
    }

    override suspend fun leave(): Result<Unit> =
        leaveRoomError?.let { Result.failure(it) } ?: Result.success(Unit)

    override suspend fun join(): Result<Unit> {
        return joinRoomResult
    }

    override suspend fun inviteUserById(id: UserId): Result<Unit> = simulateLongTask {
        invitedUserId = id
        inviteUserResult
    }

    override suspend fun canUserInvite(userId: UserId): Result<Boolean> {
        return canInviteResult
    }

    override suspend fun canUserRedact(userId: UserId): Result<Boolean> {
        return canRedactResult
    }

    override suspend fun canUserSendState(userId: UserId, type: StateEventType): Result<Boolean> {
        return canSendStateResults[type] ?: Result.failure(IllegalStateException("No fake answer"))
    }

    override suspend fun canUserSendMessage(userId: UserId, type: MessageEventType): Result<Boolean> {
        return canSendEventResults[type] ?: Result.failure(IllegalStateException("No fake answer"))
    }

    override suspend fun canUserTriggerRoomNotification(userId: UserId): Result<Boolean> {
        return canUserTriggerRoomNotificationResult
    }

    override suspend fun sendImage(
        file: File,
        thumbnailFile: File,
        imageInfo: ImageInfo,
        progressCallback: ProgressCallback?
    ): Result<MediaUploadHandler> = fakeSendMedia(progressCallback)

    override suspend fun sendVideo(
        file: File,
        thumbnailFile: File,
        videoInfo: VideoInfo,
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
        forwardEventResult
    }

    private suspend fun fakeSendMedia(progressCallback: ProgressCallback?): Result<MediaUploadHandler> = simulateLongTask {
        sendMediaResult.onSuccess {
            progressCallbackValues.forEach { (current, total) ->
                progressCallback?.onProgress(current, total)
                delay(1)
            }
            sendMediaCount++
        }
    }

    override suspend fun updateAvatar(mimeType: String, data: ByteArray): Result<Unit> = simulateLongTask {
        newAvatarData = data
        updateAvatarResult
    }

    override suspend fun removeAvatar(): Result<Unit> = simulateLongTask {
        removedAvatar = true
        removeAvatarResult
    }

    override suspend fun setName(name: String): Result<Unit> = simulateLongTask {
        newName = name
        setNameResult
    }

    override suspend fun setTopic(topic: String): Result<Unit> = simulateLongTask {
        newTopic = topic
        setTopicResult
    }

    override suspend fun reportContent(
        eventId: EventId,
        reason: String,
        blockUserId: UserId?
    ): Result<Unit> = simulateLongTask {
        reportedContentCount++
        return reportContentResult
    }

    override suspend fun sendLocation(
        body: String,
        geoUri: String,
        description: String?,
        zoomLevel: Int?,
        assetType: AssetType?,
    ): Result<Unit> = simulateLongTask {
        _sentLocations.add(SendLocationInvocation(body, geoUri, description, zoomLevel, assetType))
        return sendLocationResult
    }

    override suspend fun createPoll(
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind
    ): Result<Unit> = simulateLongTask {
        _createPollInvocations.add(CreatePollInvocation(question, answers, maxSelections, pollKind))
        return createPollResult
    }

    override suspend fun sendPollResponse(
        pollStartId: EventId,
        answers: List<String>
    ): Result<Unit> = simulateLongTask {
        _sendPollResponseInvocations.add(SendPollResponseInvocation(pollStartId, answers))
        return sendPollResponseResult
    }

    override suspend fun endPoll(
        pollStartId: EventId,
        text: String
    ): Result<Unit> = simulateLongTask {
        _endPollInvocations.add(EndPollInvocation(pollStartId, text))
        return endPollResult
    }

    override suspend fun sendVoiceMessage(
        file: File,
        audioInfo: AudioInfo,
        waveform: List<Float>,
        progressCallback: ProgressCallback?
    ): Result<MediaUploadHandler> = fakeSendMedia(progressCallback)

    override suspend fun generateWidgetWebViewUrl(
        widgetSettings: MatrixWidgetSettings,
        clientId: String,
        languageTag: String?,
        theme: String?,
    ): Result<String> = generateWidgetWebViewUrlResult

    override fun getWidgetDriver(widgetSettings: MatrixWidgetSettings): Result<MatrixWidgetDriver> = getWidgetDriverResult

    fun givenLeaveRoomError(throwable: Throwable?) {
        this.leaveRoomError = throwable
    }

    fun givenRoomMembersState(state: MatrixRoomMembersState) {
        membersStateFlow.value = state
    }

    fun givenUpdateMembersResult(result: Result<Unit>) {
        updateMembersResult = result
    }

    fun givenUserDisplayNameResult(displayName: Result<String?>) {
        userDisplayNameResult = displayName
    }

    fun givenUserAvatarUrlResult(avatarUrl: Result<String?>) {
        userAvatarUrlResult = avatarUrl
    }

    fun givenJoinRoomResult(result: Result<Unit>) {
        joinRoomResult = result
    }

    fun givenInviteUserResult(result: Result<Unit>) {
        inviteUserResult = result
    }

    fun givenCanInviteResult(result: Result<Boolean>) {
        canInviteResult = result
    }

    fun givenCanSendStateResult(type: StateEventType, result: Result<Boolean>) {
        canSendStateResults[type] = result
    }

    fun givenCanSendEventResult(type: MessageEventType, result: Result<Boolean>) {
        canSendEventResults[type] = result
    }

    fun givenCanTriggerRoomNotification(result: Result<Boolean>) {
        canUserTriggerRoomNotificationResult = result
    }

    fun givenIgnoreResult(result: Result<Unit>) {
        ignoreResult = result
    }

    fun givenUnIgnoreResult(result: Result<Unit>) {
        unignoreResult = result
    }

    fun givenSendMediaResult(result: Result<FakeMediaUploadHandler>) {
        sendMediaResult = result
    }

    fun givenUpdateAvatarResult(result: Result<Unit>) {
        updateAvatarResult = result
    }

    fun givenRemoveAvatarResult(result: Result<Unit>) {
        removeAvatarResult = result
    }

    fun givenSetNameResult(result: Result<Unit>) {
        setNameResult = result
    }

    fun givenSetTopicResult(result: Result<Unit>) {
        setTopicResult = result
    }

    fun givenToggleReactionResult(result: Result<Unit>) {
        toggleReactionResult = result
    }

    fun givenRetrySendMessageResult(result: Result<Unit>) {
        retrySendMessageResult = result
    }

    fun givenCancelSendResult(result: Result<Unit>) {
        cancelSendResult = result
    }

    fun givenForwardEventResult(result: Result<Unit>) {
        forwardEventResult = result
    }

    fun givenReportContentResult(result: Result<Unit>) {
        reportContentResult = result
    }

    fun givenSendLocationResult(result: Result<Unit>) {
        sendLocationResult = result
    }

    fun givenCreatePollResult(result: Result<Unit>) {
        createPollResult = result
    }

    fun givenSendPollResponseResult(result: Result<Unit>) {
        sendPollResponseResult = result
    }

    fun givenEndPollResult(result: Result<Unit>) {
        endPollResult = result
    }

    fun givenProgressCallbackValues(values: List<Pair<Long, Long>>) {
        progressCallbackValues = values
    }

    fun givenGenerateWidgetWebViewUrlResult(result: Result<String>) {
        generateWidgetWebViewUrlResult = result
    }

    fun givenGetWidgetDriverResult(result: Result<MatrixWidgetDriver>) {
        getWidgetDriverResult = result
    }

    fun givenRoomInfo(roomInfo: MatrixRoomInfo) {
        _roomInfoFlow.tryEmit(roomInfo)
    }
}

data class SendLocationInvocation(
    val body: String,
    val geoUri: String,
    val description: String?,
    val zoomLevel: Int?,
    val assetType: AssetType?,
)

data class CreatePollInvocation(
    val question: String,
    val answers: List<String>,
    val maxSelections: Int,
    val pollKind: PollKind,
)

data class SendPollResponseInvocation(
    val pollStartId: EventId,
    val answers: List<String>,
)

data class EndPollInvocation(
    val pollStartId: EventId,
    val text: String,
)

fun aRoomInfo(
    id: String = A_ROOM_ID.value,
    name: String? = A_ROOM_NAME,
    topic: String? = "A topic",
    avatarUrl: String? = AN_AVATAR_URL,
    isDirect: Boolean = false,
    isPublic: Boolean = true,
    isSpace: Boolean = false,
    isTombstoned: Boolean = false,
    canonicalAlias: String? = null,
    alternativeAliases: List<String> = emptyList(),
    currentUserMembership: CurrentUserMembership = CurrentUserMembership.JOINED,
    latestEvent: EventTimelineItem? = null,
    inviter: RoomMember? = null,
    activeMembersCount: Long = 1,
    invitedMembersCount: Long = 0,
    joinedMembersCount: Long = 1,
    highlightCount: Long = 0,
    notificationCount: Long = 0,
    userDefinedNotificationMode: RoomNotificationMode? = null,
    hasRoomCall: Boolean = false,
    activeRoomCallParticipants: List<String> = emptyList()
) = MatrixRoomInfo(
    id = id,
    name = name,
    topic = topic,
    avatarUrl = avatarUrl,
    isDirect = isDirect,
    isPublic = isPublic,
    isSpace = isSpace,
    isTombstoned = isTombstoned,
    canonicalAlias = canonicalAlias,
    alternativeAliases = alternativeAliases,
    currentUserMembership = currentUserMembership,
    latestEvent = latestEvent,
    inviter = inviter,
    activeMembersCount = activeMembersCount,
    invitedMembersCount = invitedMembersCount,
    joinedMembersCount = joinedMembersCount,
    highlightCount = highlightCount,
    notificationCount = notificationCount,
    userDefinedNotificationMode = userDefinedNotificationMode,
    hasRoomCall = hasRoomCall,
    activeRoomCallParticipants = activeRoomCallParticipants
)
