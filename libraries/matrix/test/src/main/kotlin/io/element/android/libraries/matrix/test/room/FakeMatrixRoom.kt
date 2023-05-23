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
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.timeline.FakeMatrixTimeline
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import java.io.File

class FakeMatrixRoom(
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
    private val matrixTimeline: MatrixTimeline = FakeMatrixTimeline(),
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
    private var sendMediaResult = Result.success(Unit)
    var sendMediaCount = 0
        private set

    var isInviteAccepted: Boolean = false
        private set

    var isInviteRejected: Boolean = false
        private set

    var invitedUserId: UserId? = null
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

    override suspend fun sendMessage(message: String): Result<Unit> {
        delay(100)
        return Result.success(Unit)
    }

    var editMessageParameter: String? = null
        private set

    override suspend fun editMessage(originalEventId: EventId, message: String): Result<Unit> {
        editMessageParameter = message
        delay(100)
        return Result.success(Unit)
    }

    var replyMessageParameter: String? = null
        private set

    override suspend fun replyMessage(eventId: EventId, message: String): Result<Unit> {
        replyMessageParameter = message
        delay(100)
        return Result.success(Unit)
    }

    var redactEventEventIdParam: EventId? = null
        private set

    override suspend fun redactEvent(eventId: EventId, reason: String?): Result<Unit> {
        redactEventEventIdParam = eventId
        delay(100)
        return Result.success(Unit)
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

    override suspend fun sendImage(file: File, thumbnailFile: File, imageInfo: ImageInfo): Result<Unit> = sendMediaResult.also { sendMediaCount++ }

    override suspend fun sendVideo(file: File, thumbnailFile: File, videoInfo: VideoInfo): Result<Unit> = sendMediaResult.also { sendMediaCount++ }

    override suspend fun sendAudio(file: File, audioInfo: AudioInfo): Result<Unit> = sendMediaResult.also { sendMediaCount++ }

    override suspend fun sendFile(file: File, fileInfo: FileInfo): Result<Unit> = sendMediaResult.also { sendMediaCount++ }

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

    fun givenIgnoreResult(result: Result<Unit>) {
        ignoreResult = result
    }

    fun givenUnIgnoreResult(result: Result<Unit>) {
        unignoreResult = result
    }

    fun givenSendMediaResult(result: Result<Unit>) {
        sendMediaResult = result
    }
}
