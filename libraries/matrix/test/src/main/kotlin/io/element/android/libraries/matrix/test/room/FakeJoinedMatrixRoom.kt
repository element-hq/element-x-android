/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.room

import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SendHandle
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.identity.IdentityStateChange
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.MediaUploadHandler
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.room.CreateTimelineParams
import io.element.android.libraries.matrix.api.room.IntentionalMention
import io.element.android.libraries.matrix.api.room.JoinedMatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.MatrixRoomNotificationSettingsState
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.room.knock.KnockRequest
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.room.message.ReplyParameters
import io.element.android.libraries.matrix.api.room.powerlevels.MatrixRoomPowerLevels
import io.element.android.libraries.matrix.api.room.powerlevels.UserRoleChange
import io.element.android.libraries.matrix.api.roomdirectory.RoomVisibility
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.widget.MatrixWidgetDriver
import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings
import io.element.android.libraries.matrix.test.media.FakeMediaUploadHandler
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestScope
import java.io.File

class FakeJoinedMatrixRoom(
    val baseRoom: FakeMatrixRoom = FakeMatrixRoom(),
    override val liveTimeline: Timeline = FakeTimeline(),
    override val roomCoroutineScope: CoroutineScope = TestScope(),
    override val syncUpdateFlow: StateFlow<Long> = MutableStateFlow(0),
    override val roomTypingMembersFlow: Flow<List<UserId>> = MutableStateFlow(emptyList()),
    override val identityStateChangesFlow: Flow<List<IdentityStateChange>> = MutableStateFlow(emptyList()),
    override val roomNotificationSettingsStateFlow: StateFlow<MatrixRoomNotificationSettingsState> =
    MutableStateFlow(MatrixRoomNotificationSettingsState.Unknown),
    override val knockRequestsFlow: Flow<List<KnockRequest>> = MutableStateFlow(emptyList()),
    private val roomNotificationSettingsService: FakeNotificationSettingsService = FakeNotificationSettingsService(),
    private var createTimelineResult: (CreateTimelineParams) -> Result<Timeline> = { lambdaError() },
    private val sendMessageResult: (String, String?, List<IntentionalMention>) -> Result<Unit> = { _, _, _ -> lambdaError() },
    private val editMessageLambda: (EventId, String, String?, List<IntentionalMention>) -> Result<Unit> = { _, _, _, _ -> lambdaError() },
    private val sendImageResult: (File, File?, ImageInfo, String?, String?, ProgressCallback?, ReplyParameters?) -> Result<FakeMediaUploadHandler> =
        { _, _, _, _, _, _, _ -> lambdaError() },
    private val sendVideoResult: (File, File?, VideoInfo, String?, String?, ProgressCallback?, ReplyParameters?) -> Result<FakeMediaUploadHandler> =
        { _, _, _, _, _, _, _ -> lambdaError() },
    private val sendFileResult: (File, FileInfo, String?, String?, ProgressCallback?, ReplyParameters?) -> Result<FakeMediaUploadHandler> =
        { _, _, _, _, _, _ -> lambdaError() },
    private val sendAudioResult: (File, AudioInfo, String?, String?, ProgressCallback?, ReplyParameters?) -> Result<FakeMediaUploadHandler> =
        { _, _, _, _, _, _ -> lambdaError() },
    private val sendVoiceMessageResult: (File, AudioInfo, List<Float>, ProgressCallback?, ReplyParameters?) -> Result<FakeMediaUploadHandler> =
        { _, _, _, _, _ -> lambdaError() },
    private val sendLocationResult: (String, String, String?, Int?, AssetType?) -> Result<Unit> = { _, _, _, _, _ -> lambdaError() },
    private val sendCallNotificationIfNeededResult: () -> Result<Unit> = { lambdaError() },
    private val progressCallbackValues: List<Pair<Long, Long>> = emptyList(),
    private val createPollResult: (String, List<String>, Int, PollKind) -> Result<Unit> = { _, _, _, _ -> lambdaError() },
    private val editPollResult: (EventId, String, List<String>, Int, PollKind) -> Result<Unit> = { _, _, _, _, _ -> lambdaError() },
    private val sendPollResponseResult: (EventId, List<String>) -> Result<Unit> = { _, _ -> lambdaError() },
    private val endPollResult: (EventId, String) -> Result<Unit> = { _, _ -> lambdaError() },
    private val generateWidgetWebViewUrlResult: (MatrixWidgetSettings, String, String?, String?) -> Result<String> = { _, _, _, _ -> lambdaError() },
    private val getWidgetDriverResult: (MatrixWidgetSettings) -> Result<MatrixWidgetDriver> = { lambdaError() },
    private val typingNoticeResult: (Boolean) -> Result<Unit> = { lambdaError() },
    private val toggleReactionResult: (String, EventOrTransactionId) -> Result<Unit> = { _, _ -> lambdaError() },
    private val forwardEventResult: (EventId, List<RoomId>) -> Result<Unit> = { _, _ -> lambdaError() },
    private val cancelSendResult: (TransactionId) -> Result<Unit> = { lambdaError() },
    private val inviteUserResult: (UserId) -> Result<Unit> = { lambdaError() },
    private val setNameResult: (String) -> Result<Unit> = { lambdaError() },
    private val setTopicResult: (String) -> Result<Unit> = { lambdaError() },
    private val updateAvatarResult: (String, ByteArray) -> Result<Unit> = { _, _ -> lambdaError() },
    private val removeAvatarResult: () -> Result<Unit> = { lambdaError() },
    private val updateUserRoleResult: (List<UserRoleChange>) -> Result<Unit> = { lambdaError() },
    private val updatePowerLevelsResult: (MatrixRoomPowerLevels) -> Result<Unit> = { lambdaError() },
    private val resetPowerLevelsResult: () -> Result<MatrixRoomPowerLevels> = { lambdaError() },
    private val reportContentResult: (EventId, String, UserId?) -> Result<Unit> = { _, _, _ -> lambdaError() },
    private val kickUserResult: (UserId, String?) -> Result<Unit> = { _, _ -> lambdaError() },
    private val banUserResult: (UserId, String?) -> Result<Unit> = { _, _ -> lambdaError() },
    private val unBanUserResult: (UserId, String?) -> Result<Unit> = { _, _ -> lambdaError() },
    private val ignoreDeviceTrustAndResendResult: (Map<UserId, List<DeviceId>>, SendHandle) -> Result<Unit> = { _, _ -> lambdaError() },
    private val withdrawVerificationAndResendResult: (List<UserId>, SendHandle) -> Result<Unit> = { _, _ -> lambdaError() },
    private val updateCanonicalAliasResult: (RoomAlias?, List<RoomAlias>) -> Result<Unit> = { _, _ -> lambdaError() },
    private val updateRoomVisibilityResult: (RoomVisibility) -> Result<Unit> = { lambdaError() },
    private val updateRoomHistoryVisibilityResult: (RoomHistoryVisibility) -> Result<Unit> = { lambdaError() },
    private val publishRoomAliasInRoomDirectoryResult: (RoomAlias) -> Result<Boolean> = { lambdaError() },
    private val removeRoomAliasFromRoomDirectoryResult: (RoomAlias) -> Result<Boolean> = { lambdaError() },
    private val enableEncryptionResult: () -> Result<Unit> = { lambdaError() },
    private val updateJoinRuleResult: (JoinRule) -> Result<Unit> = { lambdaError() },
    private val setSendQueueEnabledResult: (Boolean) -> Unit = { _: Boolean -> },
) : JoinedMatrixRoom, MatrixRoom by baseRoom {
    fun givenRoomMembersState(state: MatrixRoomMembersState) {
        baseRoom.givenRoomMembersState(state)
    }

    fun givenRoomInfo(roomInfo: MatrixRoomInfo) {
        baseRoom.givenRoomInfo(roomInfo)
    }

    override suspend fun createTimeline(createTimelineParams: CreateTimelineParams): Result<Timeline> = simulateLongTask {
        createTimelineResult(createTimelineParams)
    }

    override suspend fun sendMessage(body: String, htmlBody: String?, intentionalMentions: List<IntentionalMention>): Result<Unit> = simulateLongTask {
        sendMessageResult(body, htmlBody, intentionalMentions)
    }

    override suspend fun editMessage(
        eventId: EventId,
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>
    ): Result<Unit> = simulateLongTask {
        editMessageLambda(eventId, body, htmlBody, intentionalMentions)
    }

    override suspend fun sendImage(
        file: File,
        thumbnailFile: File?,
        imageInfo: ImageInfo,
        caption: String?,
        formattedCaption: String?,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ): Result<MediaUploadHandler> = simulateLongTask {
        simulateSendMediaProgress(progressCallback)
        sendImageResult(
            file,
            thumbnailFile,
            imageInfo,
            caption,
            formattedCaption,
            progressCallback,
            replyParameters,
        )
    }

    override suspend fun sendVideo(
        file: File,
        thumbnailFile: File?,
        videoInfo: VideoInfo,
        caption: String?,
        formattedCaption: String?,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ): Result<MediaUploadHandler> = simulateLongTask {
        simulateSendMediaProgress(progressCallback)
        sendVideoResult(
            file,
            thumbnailFile,
            videoInfo,
            caption,
            formattedCaption,
            progressCallback,
            replyParameters,
        )
    }

    override suspend fun sendAudio(
        file: File,
        audioInfo: AudioInfo,
        caption: String?,
        formattedCaption: String?,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ): Result<MediaUploadHandler> = simulateLongTask {
        simulateSendMediaProgress(progressCallback)
        sendAudioResult(
            file,
            audioInfo,
            caption,
            formattedCaption,
            progressCallback,
            replyParameters,
        )
    }

    override suspend fun sendFile(
        file: File,
        fileInfo: FileInfo,
        caption: String?,
        formattedCaption: String?,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ): Result<MediaUploadHandler> = simulateLongTask {
        simulateSendMediaProgress(progressCallback)
        sendFileResult(
            file,
            fileInfo,
            caption,
            formattedCaption,
            progressCallback,
            replyParameters,
        )
    }

    override suspend fun sendVoiceMessage(
        file: File,
        audioInfo: AudioInfo,
        waveform: List<Float>,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ): Result<MediaUploadHandler> = simulateLongTask {
        simulateSendMediaProgress(progressCallback)
        sendVoiceMessageResult(
            file,
            audioInfo,
            waveform,
            progressCallback,
            replyParameters,
        )
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

    override suspend fun createPoll(question: String, answers: List<String>, maxSelections: Int, pollKind: PollKind): Result<Unit> = simulateLongTask {
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

    override suspend fun sendPollResponse(pollStartId: EventId, answers: List<String>): Result<Unit> = simulateLongTask {
        return sendPollResponseResult(
            pollStartId,
            answers,
        )
    }

    override suspend fun endPoll(pollStartId: EventId, text: String): Result<Unit> = simulateLongTask {
        endPollResult(
            pollStartId,
            text,
        )
    }

    override suspend fun typingNotice(isTyping: Boolean): Result<Unit> = simulateLongTask {
        typingNoticeResult(isTyping)
    }

    override suspend fun toggleReaction(emoji: String, eventOrTransactionId: EventOrTransactionId): Result<Unit> = simulateLongTask {
        toggleReactionResult(emoji, eventOrTransactionId)
    }

    override suspend fun forwardEvent(eventId: EventId, roomIds: List<RoomId>): Result<Unit> = simulateLongTask {
        forwardEventResult(eventId, roomIds)
    }

    override suspend fun cancelSend(transactionId: TransactionId): Result<Unit> = simulateLongTask {
        cancelSendResult(transactionId)
    }

    override suspend fun inviteUserById(id: UserId): Result<Unit> = simulateLongTask {
        inviteUserResult(id)
    }

    override suspend fun updateAvatar(mimeType: String, data: ByteArray): Result<Unit> = simulateLongTask {
        simulateSendMediaProgress(null)
        updateAvatarResult(mimeType, data)
    }

    override suspend fun removeAvatar(): Result<Unit> = simulateLongTask {
        removeAvatarResult()
    }

    override suspend fun updateRoomNotificationSettings(): Result<Unit> = simulateLongTask {
        val notificationSettings = roomNotificationSettingsService.getRoomNotificationSettings(roomId, info().isEncrypted.orFalse(), isOneToOne).getOrThrow()
        (roomNotificationSettingsStateFlow as MutableStateFlow).value = MatrixRoomNotificationSettingsState.Ready(notificationSettings)
        return Result.success(Unit)
    }

    override suspend fun updateCanonicalAlias(canonicalAlias: RoomAlias?, alternativeAliases: List<RoomAlias>): Result<Unit> = simulateLongTask {
        updateCanonicalAliasResult(canonicalAlias, alternativeAliases)
    }

    override suspend fun updateRoomVisibility(roomVisibility: RoomVisibility): Result<Unit> = simulateLongTask {
        updateRoomVisibilityResult(roomVisibility)
    }

    override suspend fun updateHistoryVisibility(historyVisibility: RoomHistoryVisibility): Result<Unit> = simulateLongTask {
        updateRoomHistoryVisibilityResult(historyVisibility)
    }

    override suspend fun publishRoomAliasInRoomDirectory(roomAlias: RoomAlias): Result<Boolean> = simulateLongTask {
        publishRoomAliasInRoomDirectoryResult(roomAlias)
    }

    override suspend fun removeRoomAliasFromRoomDirectory(roomAlias: RoomAlias): Result<Boolean> = simulateLongTask {
        removeRoomAliasFromRoomDirectoryResult(roomAlias)
    }

    override suspend fun enableEncryption(): Result<Unit> = simulateLongTask {
        enableEncryptionResult().onSuccess {
            baseRoom.givenRoomInfo(info().copy(isEncrypted = true))
            emitSyncUpdate()
        }
    }

    override suspend fun updateJoinRule(joinRule: JoinRule): Result<Unit> = simulateLongTask {
        updateJoinRuleResult(joinRule)
    }

    override suspend fun updateUsersRoles(changes: List<UserRoleChange>): Result<Unit> = simulateLongTask {
        updateUserRoleResult(changes)
    }

    override suspend fun updatePowerLevels(matrixRoomPowerLevels: MatrixRoomPowerLevels): Result<Unit> = simulateLongTask {
        updatePowerLevelsResult(matrixRoomPowerLevels)
    }

    override suspend fun resetPowerLevels(): Result<MatrixRoomPowerLevels> = simulateLongTask {
        resetPowerLevelsResult()
    }

    override suspend fun setName(name: String): Result<Unit> = simulateLongTask {
        setNameResult(name)
    }

    override suspend fun setTopic(topic: String): Result<Unit> = simulateLongTask {
        setTopicResult(topic)
    }

    override suspend fun reportContent(eventId: EventId, reason: String, blockUserId: UserId?): Result<Unit> = simulateLongTask {
        reportContentResult(eventId, reason, blockUserId)
    }

    override suspend fun kickUser(userId: UserId, reason: String?): Result<Unit> = simulateLongTask {
        kickUserResult(userId, reason)
    }

    override suspend fun banUser(userId: UserId, reason: String?): Result<Unit> = simulateLongTask {
        banUserResult(userId, reason)
    }

    override suspend fun unbanUser(userId: UserId, reason: String?): Result<Unit> = simulateLongTask {
        unBanUserResult(userId, reason)
    }

    override suspend fun generateWidgetWebViewUrl(
        widgetSettings: MatrixWidgetSettings,
        clientId: String,
        languageTag: String?,
        theme: String?
    ): Result<String> = simulateLongTask {
        generateWidgetWebViewUrlResult(widgetSettings, clientId, languageTag, theme)
    }

    override fun getWidgetDriver(widgetSettings: MatrixWidgetSettings): Result<MatrixWidgetDriver> {
        return getWidgetDriverResult(widgetSettings)
    }

    override suspend fun sendCallNotificationIfNeeded(): Result<Unit> = simulateLongTask {
        sendCallNotificationIfNeededResult()
    }

    override suspend fun setSendQueueEnabled(enabled: Boolean) = simulateLongTask {
        setSendQueueEnabledResult(enabled)
    }

    override suspend fun ignoreDeviceTrustAndResend(devices: Map<UserId, List<DeviceId>>, sendHandle: SendHandle): Result<Unit> = simulateLongTask {
        ignoreDeviceTrustAndResendResult(devices, sendHandle)
    }

    override suspend fun withdrawVerificationAndResend(userIds: List<UserId>, sendHandle: SendHandle): Result<Unit> = simulateLongTask {
        withdrawVerificationAndResendResult(userIds, sendHandle)
    }

    private suspend fun simulateSendMediaProgress(progressCallback: ProgressCallback?) {
        progressCallbackValues.forEach { (current, total) ->
            progressCallback?.onProgress(current, total)
            delay(1)
        }
    }

    fun emitSyncUpdate() {
        (syncUpdateFlow as MutableStateFlow).value = syncUpdateFlow.value + 1
    }
}
