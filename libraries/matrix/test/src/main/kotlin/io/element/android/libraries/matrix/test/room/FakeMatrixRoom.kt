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

import io.element.android.libraries.core.coroutine.errorFlow
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.timeline.FakeMatrixTimeline
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class FakeMatrixRoom(
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
    private val members: List<RoomMember> = emptyList(),
    private val matrixTimeline: MatrixTimeline = FakeMatrixTimeline(),
) : MatrixRoom {

    private var userDisplayNameResult = Result.success<String?>(null)
    private var userAvatarUrlResult = Result.success<String?>(null)
    private var acceptInviteResult = Result.success(Unit)
    private var rejectInviteResult = Result.success(Unit)
    private var dmMember: RoomMember? = null
    private var fetchMemberResult: Result<Unit> = Result.success(Unit)
    private var ignoreResult = Result.success(Unit)
    private var unignoreResult = Result.success(Unit)

    var areMembersFetched: Boolean = false
        private set

    var isInviteAccepted: Boolean = false
        private set

    var isInviteRejected: Boolean = false
        private set

    private var leaveRoomError: Throwable? = null

    override fun syncUpdateFlow(): Flow<Long> {
        return emptyFlow()
    }

    override fun timeline(): MatrixTimeline {
        return matrixTimeline
    }

    override suspend fun fetchMembers(): Result<Unit> {
        return fetchMemberResult.also { result ->
            if (result.isSuccess) {
                areMembersFetched = true
            }
        }
    }

    override fun getDmMember(): Flow<RoomMember?> {
        return flowOf(dmMember)
    }

    override suspend fun userDisplayName(userId: UserId): Result<String?> {
        return userDisplayNameResult
    }

    override suspend fun userAvatarUrl(userId: UserId): Result<String?> {
        return userAvatarUrlResult
    }

    override fun members(): Flow<List<RoomMember>> {
        return fetchMemberResult.fold(onSuccess = {
            flowOf(members)
        }, onFailure = {
            errorFlow(it)
        })
    }

    override fun updateMembers() = Unit

    override fun getMember(userId: UserId): Flow<RoomMember?> {
        return flowOf(members.find { it.userId == userId })
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

    override suspend fun ignoreUser(userId: UserId): Result<Unit> = ignoreResult

    override suspend fun unignoreUser(userId: UserId): Result<Unit> = unignoreResult

    override suspend fun leave(): Result<Unit> = leaveRoomError?.let { Result.failure(it) } ?: Result.success(Unit)
    override suspend fun acceptInvitation(): Result<Unit> {
        isInviteAccepted = true
        return acceptInviteResult
    }

    override suspend fun rejectInvitation(): Result<Unit> {
        isInviteRejected = true
        return rejectInviteResult
    }

    override fun close() = Unit

    fun givenLeaveRoomError(throwable: Throwable?) {
        this.leaveRoomError = throwable
    }

    fun givenFetchMemberResult(result: Result<Unit>) {
        fetchMemberResult = result
    }

    fun givenDmMember(roomMember: RoomMember) {
        this.dmMember = roomMember
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


    fun givenIgnoreResult(result: Result<Unit>) {
        ignoreResult = result
    }

    fun givenUnIgnoreResult(result: Result<Unit>) {
        unignoreResult = result
    }
}
