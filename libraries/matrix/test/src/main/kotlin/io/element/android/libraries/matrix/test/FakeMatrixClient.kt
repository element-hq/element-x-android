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

package io.element.android.libraries.matrix.test

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.createroom.CreateRoomParameters
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.notification.NotificationService
import io.element.android.libraries.matrix.api.pusher.PushersService
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.RoomSummaryDataSource
import io.element.android.libraries.matrix.api.user.MatrixSearchUserResults
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.test.media.FakeMediaLoader
import io.element.android.libraries.matrix.test.notification.FakeNotificationService
import io.element.android.libraries.matrix.test.pushers.FakePushersService
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.FakeRoomSummaryDataSource
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import kotlinx.coroutines.delay

fun aFakeMatrixClient(
    sessionId: SessionId = A_SESSION_ID,
    userDisplayName: Result<String> = Result.success(A_USER_NAME),
    userAvatarURLString: Result<String> = Result.success(AN_AVATAR_URL),
    roomSummaryDataSource: RoomSummaryDataSource = FakeRoomSummaryDataSource(),
    invitesDataSource: RoomSummaryDataSource = FakeRoomSummaryDataSource(),
    mediaLoader: MatrixMediaLoader = FakeMediaLoader(),
    sessionVerificationService: FakeSessionVerificationService = FakeSessionVerificationService(),
    pushersService: FakePushersService = FakePushersService(),
    notificationService: FakeNotificationService = FakeNotificationService(),
): FakeMatrixClient {
    return FakeMatrixClient(
        sessionId = sessionId,
        userDisplayName = userDisplayName,
        userAvatarURLString = userAvatarURLString,
        roomSummaryDataSource = roomSummaryDataSource,
        invitesDataSource = invitesDataSource,
        mediaLoader = mediaLoader,
        sessionVerificationService = sessionVerificationService,
        pushersService = pushersService,
        notificationService = notificationService,
    )
}

class FakeMatrixClient(
    override val sessionId: SessionId = A_SESSION_ID,
    private val userDisplayName: Result<String> = Result.success(A_USER_NAME),
    private val userAvatarURLString: Result<String> = Result.success(AN_AVATAR_URL),
    override val roomSummaryDataSource: RoomSummaryDataSource = FakeRoomSummaryDataSource(),
    override val invitesDataSource: RoomSummaryDataSource = FakeRoomSummaryDataSource(),
    override val mediaLoader: MatrixMediaLoader = FakeMediaLoader(),
    private val sessionVerificationService: FakeSessionVerificationService = FakeSessionVerificationService(),
    private val pushersService: FakePushersService = FakePushersService(),
    private val notificationService: FakeNotificationService = FakeNotificationService(),
) : MatrixClient {

    private var ignoreUserResult: Result<Unit> = Result.success(Unit)
    private var unignoreUserResult: Result<Unit> = Result.success(Unit)
    private var createRoomResult: Result<RoomId> = Result.success(A_ROOM_ID)
    private var createDmResult: Result<RoomId> = Result.success(A_ROOM_ID)
    private var createDmFailure: Throwable? = null
    private var findDmResult: MatrixRoom? = FakeMatrixRoom()
    private var logoutFailure: Throwable? = null
    private val getRoomResults = mutableMapOf<RoomId, MatrixRoom>()
    private val searchUserResults = mutableMapOf<String, Result<MatrixSearchUserResults>>()
    private val getProfileResults = mutableMapOf<UserId, Result<MatrixUser>>()
    private var uploadMediaResult: Result<String> = Result.success(AN_AVATAR_URL)

    override fun getRoom(roomId: RoomId): MatrixRoom? {
        return getRoomResults[roomId]
    }

    override fun findDM(userId: UserId): MatrixRoom? {
        return findDmResult
    }

    override suspend fun ignoreUser(userId: UserId): Result<Unit> {
        return ignoreUserResult
    }

    override suspend fun unignoreUser(userId: UserId): Result<Unit> {
        return unignoreUserResult
    }

    override suspend fun createRoom(createRoomParams: CreateRoomParameters): Result<RoomId> {
        delay(100)
        return createRoomResult
    }

    override suspend fun createDM(userId: UserId): Result<RoomId> {
        delay(100)
        createDmFailure?.let { throw it }
        return createDmResult
    }

    override suspend fun getProfile(userId: UserId): Result<MatrixUser> {
        return getProfileResults[userId] ?: Result.failure(IllegalStateException("No profile found for $userId"))
    }

    override suspend fun searchUsers(searchTerm: String, limit: Long): Result<MatrixSearchUserResults> {
        return searchUserResults[searchTerm] ?: Result.failure(IllegalStateException("No response defined for $searchTerm"))
    }

    override fun startSync() = Unit

    override fun stopSync() = Unit

    override suspend fun logout() {
        delay(100)
        logoutFailure?.let { throw it }
    }

    override fun close() = Unit

    override suspend fun loadUserDisplayName(): Result<String> {
        return userDisplayName
    }

    override suspend fun loadUserAvatarURLString(): Result<String?> {
        return userAvatarURLString
    }

    override suspend fun uploadMedia(mimeType: String, data: ByteArray): Result<String> {
        return uploadMediaResult
    }

    override fun sessionVerificationService(): SessionVerificationService = sessionVerificationService

    override fun pushersService(): PushersService = pushersService

    override fun notificationService(): NotificationService = notificationService

    override fun onSlidingSyncUpdate() {}

    override fun roomMembershipObserver(): RoomMembershipObserver {
        return RoomMembershipObserver()
    }

    // Mocks

    fun givenLogoutError(failure: Throwable?) {
        logoutFailure = failure
    }

    fun givenCreateRoomResult(result: Result<RoomId>) {
        createRoomResult = result
    }

    fun givenCreateDmResult(result: Result<RoomId>) {
        createDmResult = result
    }

    fun givenIgnoreUserResult(result: Result<Unit>) {
        ignoreUserResult = result
    }

    fun givenUnignoreUserResult(result: Result<Unit>) {
        unignoreUserResult = result
    }

    fun givenCreateDmError(failure: Throwable?) {
        createDmFailure = failure
    }

    fun givenFindDmResult(result: MatrixRoom?) {
        findDmResult = result
    }

    fun givenGetRoomResult(roomId: RoomId, result: MatrixRoom) {
        getRoomResults[roomId] = result
    }

    fun givenSearchUsersResult(searchTerm: String, result: Result<MatrixSearchUserResults>) {
        searchUserResults[searchTerm] = result
    }

    fun givenGetProfileResult(userId: UserId, result: Result<MatrixUser>) {
        getProfileResults[userId] = result
    }

    fun givenUploadMediaResult(result: Result<String>) {
        uploadMediaResult = result
    }
}
