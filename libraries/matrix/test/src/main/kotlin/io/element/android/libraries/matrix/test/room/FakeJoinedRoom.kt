/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.room

import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.SendHandle
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.identity.IdentityStateChange
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.CreateTimelineParams
import io.element.android.libraries.matrix.api.room.IntentionalMention
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.RoomMembersState
import io.element.android.libraries.matrix.api.room.RoomNotificationSettingsState
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.room.knock.KnockRequest
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevelsValues
import io.element.android.libraries.matrix.api.room.powerlevels.UserRoleChange
import io.element.android.libraries.matrix.api.roomdirectory.RoomVisibility
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.widget.MatrixWidgetDriver
import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings
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

class FakeJoinedRoom(
    val baseRoom: FakeBaseRoom = FakeBaseRoom(),
    override val liveTimeline: Timeline = FakeTimeline(),
    override val roomCoroutineScope: CoroutineScope = TestScope(),
    override val syncUpdateFlow: StateFlow<Long> = MutableStateFlow(0),
    override val roomTypingMembersFlow: Flow<List<UserId>> = MutableStateFlow(emptyList()),
    override val identityStateChangesFlow: Flow<List<IdentityStateChange>> = MutableStateFlow(emptyList()),
    override val roomNotificationSettingsStateFlow: StateFlow<RoomNotificationSettingsState> =
        MutableStateFlow(RoomNotificationSettingsState.Unknown),
    override val knockRequestsFlow: Flow<List<KnockRequest>> = MutableStateFlow(emptyList()),
    private val roomNotificationSettingsService: FakeNotificationSettingsService = FakeNotificationSettingsService(),
    private var createTimelineResult: (CreateTimelineParams) -> Result<Timeline> = { lambdaError() },
    private val editMessageLambda: (EventId, String, String?, List<IntentionalMention>) -> Result<Unit> = { _, _, _, _ -> lambdaError() },
    private val progressCallbackValues: List<Pair<Long, Long>> = emptyList(),
    private val generateWidgetWebViewUrlResult: (MatrixWidgetSettings, String, String?, String?) -> Result<String> = { _, _, _, _ -> lambdaError() },
    private val getWidgetDriverResult: (MatrixWidgetSettings) -> Result<MatrixWidgetDriver> = { lambdaError() },
    private val typingNoticeResult: (Boolean) -> Result<Unit> = { lambdaError() },
    private val inviteUserResult: (UserId) -> Result<Unit> = { lambdaError() },
    private val setNameResult: (String) -> Result<Unit> = { lambdaError() },
    private val setTopicResult: (String) -> Result<Unit> = { lambdaError() },
    private val updateAvatarResult: (String, ByteArray) -> Result<Unit> = { _, _ -> lambdaError() },
    private val removeAvatarResult: () -> Result<Unit> = { lambdaError() },
    private val updateUserRoleResult: (List<UserRoleChange>) -> Result<Unit> = { lambdaError() },
    private val updatePowerLevelsResult: (RoomPowerLevelsValues) -> Result<Unit> = { lambdaError() },
    private val resetPowerLevelsResult: () -> Result<Unit> = { lambdaError() },
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
) : JoinedRoom, BaseRoom by baseRoom {
    fun givenRoomMembersState(state: RoomMembersState) {
        baseRoom.givenRoomMembersState(state)
    }

    fun givenRoomInfo(roomInfo: RoomInfo) {
        baseRoom.givenRoomInfo(roomInfo)
    }

    override suspend fun createTimeline(createTimelineParams: CreateTimelineParams): Result<Timeline> = simulateLongTask {
        createTimelineResult(createTimelineParams)
    }

    override suspend fun editMessage(
        eventId: EventId,
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>
    ): Result<Unit> = simulateLongTask {
        editMessageLambda(eventId, body, htmlBody, intentionalMentions)
    }

    override suspend fun typingNotice(isTyping: Boolean): Result<Unit> = simulateLongTask {
        typingNoticeResult(isTyping)
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
        (roomNotificationSettingsStateFlow as MutableStateFlow).value = RoomNotificationSettingsState.Ready(notificationSettings)
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

    override suspend fun updatePowerLevels(roomPowerLevelsValues: RoomPowerLevelsValues): Result<Unit> = simulateLongTask {
        updatePowerLevelsResult(roomPowerLevelsValues)
    }

    override suspend fun resetPowerLevels(): Result<Unit> = simulateLongTask {
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
