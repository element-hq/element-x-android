/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.room

import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembersState
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.draft.ComposerDraft
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevelsValues
import io.element.android.libraries.matrix.api.room.tombstone.PredecessorRoom
import io.element.android.libraries.matrix.api.roomdirectory.RoomVisibility
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestScope

class FakeBaseRoom(
    override val sessionId: SessionId = A_SESSION_ID,
    override val roomId: RoomId = A_ROOM_ID,
    initialRoomInfo: RoomInfo = aRoomInfo(),
    override val roomCoroutineScope: CoroutineScope = TestScope(),
    private var roomPermalinkResult: () -> Result<String> = { lambdaError() },
    private var eventPermalinkResult: (EventId) -> Result<String> = { lambdaError() },
    private val userDisplayNameResult: (UserId) -> Result<String?> = { lambdaError() },
    private val userAvatarUrlResult: () -> Result<String?> = { lambdaError() },
    private val userRoleResult: () -> Result<RoomMember.Role> = { lambdaError() },
    private val getUpdatedMemberResult: (UserId) -> Result<RoomMember> = { lambdaError() },
    private val joinRoomResult: () -> Result<Unit> = { lambdaError() },
    private val canInviteResult: (UserId) -> Result<Boolean> = { lambdaError() },
    private val canKickResult: (UserId) -> Result<Boolean> = { lambdaError() },
    private val canBanResult: (UserId) -> Result<Boolean> = { lambdaError() },
    private val canRedactOwnResult: (UserId) -> Result<Boolean> = { lambdaError() },
    private val canRedactOtherResult: (UserId) -> Result<Boolean> = { lambdaError() },
    private val canSendStateResult: (UserId, StateEventType) -> Result<Boolean> = { _, _ -> lambdaError() },
    private val canUserSendMessageResult: (UserId, MessageEventType) -> Result<Boolean> = { _, _ -> lambdaError() },
    private val canUserTriggerRoomNotificationResult: (UserId) -> Result<Boolean> = { lambdaError() },
    private val canUserJoinCallResult: (UserId) -> Result<Boolean> = { lambdaError() },
    private val canUserPinUnpinResult: (UserId) -> Result<Boolean> = { lambdaError() },
    private val setIsFavoriteResult: (Boolean) -> Result<Unit> = { lambdaError() },
    private val markAsReadResult: (ReceiptType) -> Result<Unit> = { Result.success(Unit) },
    private val powerLevelsResult: () -> Result<RoomPowerLevelsValues> = { lambdaError() },
    private val leaveRoomLambda: () -> Result<Unit> = { lambdaError() },
    private var updateMembersResult: () -> Unit = { lambdaError() },
    private val getMembersResult: (Int) -> Result<List<RoomMember>> = { lambdaError() },
    private val saveComposerDraftLambda: (ComposerDraft) -> Result<Unit> = { _: ComposerDraft -> Result.success(Unit) },
    private val loadComposerDraftLambda: () -> Result<ComposerDraft?> = { Result.success<ComposerDraft?>(null) },
    private val clearComposerDraftLambda: () -> Result<Unit> = { Result.success(Unit) },
    private val subscribeToSyncLambda: () -> Unit = { lambdaError() },
    private val getRoomVisibilityResult: () -> Result<RoomVisibility> = { lambdaError() },
    private val forgetResult: () -> Result<Unit> = { lambdaError() },
    private val reportRoomResult: (String?) -> Result<Unit> = { lambdaError() },
    private val predecessorRoomResult: () -> PredecessorRoom? = { null },
    private val threadRootIdForEventResult: (EventId) -> Result<ThreadId?> = { lambdaError() },
) : BaseRoom {
    private val _roomInfoFlow: MutableStateFlow<RoomInfo> = MutableStateFlow(initialRoomInfo)
    override val roomInfoFlow: StateFlow<RoomInfo> = _roomInfoFlow

    fun givenRoomInfo(roomInfo: RoomInfo) {
        _roomInfoFlow.tryEmit(roomInfo)
    }

    private val declineCallFlowMap: MutableMap<EventId, MutableSharedFlow<UserId>> = mutableMapOf()

    suspend fun givenDecliner(userId: UserId, forNotificationEventId: EventId) {
        declineCallFlowMap[forNotificationEventId]?.emit(userId)
    }

    override val membersStateFlow: MutableStateFlow<RoomMembersState> = MutableStateFlow(RoomMembersState.Unknown)

    override suspend fun updateMembers() = updateMembersResult()

    override suspend fun getUpdatedMember(userId: UserId): Result<RoomMember> {
        return getUpdatedMemberResult(userId)
    }

    override suspend fun getMembers(limit: Int): Result<List<RoomMember>> {
        return getMembersResult(limit)
    }

    override suspend fun subscribeToSync() {
        subscribeToSyncLambda()
    }

    override suspend fun powerLevels(): Result<RoomPowerLevelsValues> {
        return powerLevelsResult()
    }

    private var isDestroyed = false

    override fun destroy() {
        isDestroyed = true
    }

    fun assertDestroyed() {
        check(isDestroyed) { "Room should be destroyed" }
    }

    override suspend fun userDisplayName(userId: UserId): Result<String?> = simulateLongTask {
        userDisplayNameResult(userId)
    }

    override suspend fun userAvatarUrl(userId: UserId): Result<String?> = simulateLongTask {
        userAvatarUrlResult()
    }

    override suspend fun userRole(userId: UserId): Result<RoomMember.Role> {
        return userRoleResult()
    }

    override suspend fun getPermalink(): Result<String> {
        return roomPermalinkResult()
    }

    override suspend fun getPermalinkFor(eventId: EventId): Result<String> {
        return eventPermalinkResult(eventId)
    }

    override suspend fun getRoomVisibility(): Result<RoomVisibility> = simulateLongTask {
        getRoomVisibilityResult()
    }

    override suspend fun leave(): Result<Unit> = simulateLongTask {
        return leaveRoomLambda()
    }

    override suspend fun join(): Result<Unit> {
        return joinRoomResult()
    }

    override suspend fun forget(): Result<Unit> {
        return forgetResult()
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

    override suspend fun setIsFavorite(isFavorite: Boolean): Result<Unit> {
        return setIsFavoriteResult(isFavorite)
    }

    override suspend fun markAsRead(receiptType: ReceiptType): Result<Unit> {
        return markAsReadResult(receiptType)
    }

    var setUnreadFlagCalls = mutableListOf<Boolean>()
        private set

    override suspend fun setUnreadFlag(isUnread: Boolean): Result<Unit> {
        setUnreadFlagCalls.add(isUnread)
        return Result.success(Unit)
    }

    override suspend fun saveComposerDraft(
        composerDraft: ComposerDraft,
        threadRoot: ThreadId?
    ) = saveComposerDraftLambda(composerDraft)

    override suspend fun loadComposerDraft(threadRoot: ThreadId?) = loadComposerDraftLambda()

    override suspend fun clearComposerDraft(threadRoot: ThreadId?) = clearComposerDraftLambda()

    override suspend fun getUpdatedIsEncrypted(): Result<Boolean> = simulateLongTask {
        Result.success(info().isEncrypted.orFalse())
    }

    fun givenRoomMembersState(state: RoomMembersState) {
        membersStateFlow.value = state
    }

    override suspend fun clearEventCacheStorage(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun reportRoom(reason: String?) = reportRoomResult(reason)

    override suspend fun declineCall(notificationEventId: EventId): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun subscribeToCallDecline(notificationEventId: EventId): Flow<UserId> {
        val flow = declineCallFlowMap.getOrPut(notificationEventId, { MutableSharedFlow() })
        return flow
    }

    override fun predecessorRoom(): PredecessorRoom? = predecessorRoomResult()

    fun givenUpdateMembersResult(result: () -> Unit) {
        updateMembersResult = result
    }

    override suspend fun threadRootIdForEvent(eventId: EventId): Result<ThreadId?> {
        return threadRootIdForEventResult(eventId)
    }
}

fun defaultRoomPowerLevelValues() = RoomPowerLevelsValues(
    ban = 50,
    invite = 0,
    kick = 50,
    sendEvents = 0,
    redactEvents = 50,
    roomName = 100,
    roomAvatar = 100,
    roomTopic = 100,
    spaceChild = 100,
)
