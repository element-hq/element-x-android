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

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.timeline.FakeMatrixTimeline
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.withContext
import java.io.File

fun TestScope.aFakeMatrixRoom(
    sessionId: SessionId = A_SESSION_ID,
    roomId: RoomId = A_ROOM_ID,
    name: String? = null,
    bestName: String = "",
    displayName: String = "",
    topic: String? = null,
    avatarUrl: String? = null,
    isEncrypted: Boolean = false,
    alias: String? = null,
    alternativeAliases: List<String> = emptyList(),
    isPublic: Boolean = true,
    isDirect: Boolean = false,
    joinedMemberCount: Long = 123L,
    matrixTimeline: MatrixTimeline = FakeMatrixTimeline(),
    coroutineDispatchers: CoroutineDispatchers = testCoroutineDispatchers()
): FakeMatrixRoom {
    return FakeMatrixRoom(
        sessionId = sessionId,
        roomId = roomId,
        name = name,
        bestName = bestName,
        displayName = displayName,
        topic = topic,
        avatarUrl = avatarUrl,
        isEncrypted = isEncrypted,
        alias = alias,
        alternativeAliases = alternativeAliases,
        isPublic = isPublic,
        isDirect = isDirect,
        joinedMemberCount = joinedMemberCount,
        matrixTimeline = matrixTimeline,
        coroutineDispatchers = coroutineDispatchers,
    )
}

class FakeMatrixRoom constructor(
    override val sessionId: SessionId = A_SESSION_ID,
    override val roomId: RoomId = A_ROOM_ID,
    override val name: String? = null,
    override val bestName: String = "",
    override val displayName: String = "",
    override val topic: String? = null,
    override val avatarUrl: String? = null,
    override val isEncrypted: Boolean = false,
    override val alias: String? = null,
    override val alternativeAliases: List<String> = emptyList(),
    override val isPublic: Boolean = true,
    override val isDirect: Boolean = false,
    override val joinedMemberCount: Long = 123L,
    private val matrixTimeline: MatrixTimeline = FakeMatrixTimeline(),
    private val coroutineDispatchers: CoroutineDispatchers,
) : MatrixRoom {

    private var ignoreResult: Result<Unit> = Result.success(Unit)
    private var unignoreResult: Result<Unit> = Result.success(Unit)
    private var userDisplayNameResult = Result.success<String?>(null)
    private var userAvatarUrlResult = Result.success<String?>(null)
    private var updateMembersResult: Result<Unit> = Result.success(Unit)
    private var acceptInviteResult = Result.success(Unit)
    private var rejectInviteResult = Result.success(Unit)
    private var inviteUserResult = Result.success(Unit)
    private var canInviteResult = Result.success(true)
    private val canSendStateResults = mutableMapOf<StateEventType, Result<Boolean>>()
    private var sendMediaResult = Result.success(Unit)
    private var setNameResult = Result.success(Unit)
    private var setTopicResult = Result.success(Unit)
    private var updateAvatarResult = Result.success(Unit)
    private var removeAvatarResult = Result.success(Unit)
    private var sendReactionResult = Result.success(Unit)

    var sendMediaCount = 0
        private set

    var sendReactionCount = 0
        private set

    var isInviteAccepted: Boolean = false
        private set

    var isInviteRejected: Boolean = false
        private set

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

    override val membersStateFlow: MutableStateFlow<MatrixRoomMembersState> = MutableStateFlow(MatrixRoomMembersState.Unknown)

    override suspend fun updateMembers(): Result<Unit> {
        return updateMembersResult
    }

    override fun syncUpdateFlow(): Flow<Long> {
        return emptyFlow()
    }

    override fun timeline(): MatrixTimeline {
        return matrixTimeline
    }

    override suspend fun userDisplayName(userId: UserId): Result<String?> {
        return userDisplayNameResult
    }

    override suspend fun userAvatarUrl(userId: UserId): Result<String?> {
        return userAvatarUrlResult
    }

    override suspend fun sendMessage(message: String): Result<Unit> = withContext(coroutineDispatchers.io) {
        Result.success(Unit)
    }

    override suspend fun sendReaction(emoji: String, eventId: EventId): Result<Unit> {
        sendReactionCount++
        return sendReactionResult
    }

    var editMessageParameter: String? = null
        private set

    override suspend fun editMessage(originalEventId: EventId, message: String): Result<Unit> = withContext(coroutineDispatchers.io) {
        editMessageParameter = message
        Result.success(Unit)
    }

    var replyMessageParameter: String? = null
        private set

    override suspend fun replyMessage(eventId: EventId, message: String): Result<Unit> = withContext(coroutineDispatchers.io) {
        replyMessageParameter = message
        Result.success(Unit)
    }

    var redactEventEventIdParam: EventId? = null
        private set

    override suspend fun redactEvent(eventId: EventId, reason: String?): Result<Unit> = withContext(coroutineDispatchers.io) {
        redactEventEventIdParam = eventId
        Result.success(Unit)
    }

    override suspend fun leave(): Result<Unit> = leaveRoomError?.let { Result.failure(it) } ?: Result.success(Unit)
    override suspend fun acceptInvitation(): Result<Unit> {
        isInviteAccepted = true
        return acceptInviteResult
    }

    override suspend fun rejectInvitation(): Result<Unit> {
        isInviteRejected = true
        return rejectInviteResult
    }

    override suspend fun inviteUserById(id: UserId): Result<Unit> {
        invitedUserId = id
        return inviteUserResult
    }

    override suspend fun canInvite(): Result<Boolean> {
        return canInviteResult
    }

    override suspend fun canSendStateEvent(type: StateEventType): Result<Boolean> {
        return canSendStateResults[type] ?: Result.failure(IllegalStateException("No fake answer"))
    }

    override suspend fun sendImage(file: File, thumbnailFile: File, imageInfo: ImageInfo): Result<Unit> = fakeSendMedia()

    override suspend fun sendVideo(file: File, thumbnailFile: File, videoInfo: VideoInfo): Result<Unit> = fakeSendMedia()

    override suspend fun sendAudio(file: File, audioInfo: AudioInfo): Result<Unit> = fakeSendMedia()

    override suspend fun sendFile(file: File, fileInfo: FileInfo): Result<Unit> = fakeSendMedia()

    private suspend fun fakeSendMedia(): Result<Unit> = withContext(coroutineDispatchers.io) {
        sendMediaResult.onSuccess {
            sendMediaCount++
        }
    }

    override suspend fun updateAvatar(mimeType: String, data: ByteArray): Result<Unit> {
        newAvatarData = data
        return updateAvatarResult
    }

    override suspend fun removeAvatar(): Result<Unit> {
        removedAvatar = true
        return removeAvatarResult
    }

    override suspend fun setName(name: String): Result<Unit> {
        newName = name
        return setNameResult
    }

    override suspend fun setTopic(topic: String): Result<Unit> {
        newTopic = topic
        return setTopicResult
    }

    override fun close() = Unit

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

    fun givenAcceptInviteResult(result: Result<Unit>) {
        acceptInviteResult = result
    }

    fun givenRejectInviteResult(result: Result<Unit>) {
        rejectInviteResult = result
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

    fun givenIgnoreResult(result: Result<Unit>) {
        ignoreResult = result
    }

    fun givenUnIgnoreResult(result: Result<Unit>) {
        unignoreResult = result
    }

    fun givenSendMediaResult(result: Result<Unit>) {
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

    fun givenSendReactionResult(result: Result<Unit>) {
        sendReactionResult = result
    }
}
