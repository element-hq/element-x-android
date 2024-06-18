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
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.createroom.CreateRoomParameters
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.notification.NotificationService
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.oidc.AccountManagementAction
import io.element.android.libraries.matrix.api.pusher.PushersService
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias
import io.element.android.libraries.matrix.api.room.preview.RoomPreview
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryService
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.user.MatrixSearchUserResults
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.media.FakeMatrixMediaLoader
import io.element.android.libraries.matrix.test.notification.FakeNotificationService
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.pushers.FakePushersService
import io.element.android.libraries.matrix.test.roomdirectory.FakeRoomDirectoryService
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.libraries.matrix.test.sync.FakeSyncService
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import java.util.Optional

class FakeMatrixClient(
    override val sessionId: SessionId = A_SESSION_ID,
    override val deviceId: String = "A_DEVICE_ID",
    override val sessionCoroutineScope: CoroutineScope = TestScope(),
    private val userDisplayName: String? = A_USER_NAME,
    private val userAvatarUrl: String? = AN_AVATAR_URL,
    override val roomListService: RoomListService = FakeRoomListService(),
    override val mediaLoader: MatrixMediaLoader = FakeMatrixMediaLoader(),
    private val sessionVerificationService: FakeSessionVerificationService = FakeSessionVerificationService(),
    private val pushersService: FakePushersService = FakePushersService(),
    private val notificationService: FakeNotificationService = FakeNotificationService(),
    private val notificationSettingsService: FakeNotificationSettingsService = FakeNotificationSettingsService(),
    private val syncService: FakeSyncService = FakeSyncService(),
    private val encryptionService: FakeEncryptionService = FakeEncryptionService(),
    private val roomDirectoryService: RoomDirectoryService = FakeRoomDirectoryService(),
    private val accountManagementUrlString: Result<String?> = Result.success(null),
    private val resolveRoomAliasResult: (RoomAlias) -> Result<ResolvedRoomAlias> = { Result.success(ResolvedRoomAlias(A_ROOM_ID, emptyList())) },
    private val getRoomPreviewFromRoomIdResult: (RoomId, List<String>) -> Result<RoomPreview> = { _, _ -> Result.failure(AN_EXCEPTION) },
    private val clearCacheLambda: () -> Unit = { lambdaError() },
) : MatrixClient {
    var setDisplayNameCalled: Boolean = false
        private set
    var uploadAvatarCalled: Boolean = false
        private set
    var removeAvatarCalled: Boolean = false
        private set

    private val _userProfile: MutableStateFlow<MatrixUser> = MutableStateFlow(MatrixUser(sessionId, userDisplayName, userAvatarUrl))
    override val userProfile: StateFlow<MatrixUser> = _userProfile
    override val ignoredUsersFlow: MutableStateFlow<ImmutableList<UserId>> = MutableStateFlow(persistentListOf())

    private var ignoreUserResult: Result<Unit> = Result.success(Unit)
    private var unignoreUserResult: Result<Unit> = Result.success(Unit)
    private var createRoomResult: Result<RoomId> = Result.success(A_ROOM_ID)
    private var createDmResult: Result<RoomId> = Result.success(A_ROOM_ID)
    private var findDmResult: RoomId? = A_ROOM_ID
    private val getRoomResults = mutableMapOf<RoomId, MatrixRoom>()
    private val searchUserResults = mutableMapOf<String, Result<MatrixSearchUserResults>>()
    private val getProfileResults = mutableMapOf<UserId, Result<MatrixUser>>()
    private var uploadMediaResult: Result<String> = Result.success(AN_AVATAR_URL)
    private var setDisplayNameResult: Result<Unit> = Result.success(Unit)
    private var uploadAvatarResult: Result<Unit> = Result.success(Unit)
    private var removeAvatarResult: Result<Unit> = Result.success(Unit)
    var joinRoomLambda: (RoomId) -> Result<Unit> = {
        Result.success(Unit)
    }
    var joinRoomByIdOrAliasLambda: (RoomId, List<String>) -> Result<Unit> = { _, _ ->
        Result.success(Unit)
    }
    var knockRoomLambda: (RoomId) -> Result<Unit> = {
        Result.success(Unit)
    }
    var getRoomInfoFlowLambda = { _: RoomId ->
        flowOf<Optional<MatrixRoomInfo>>(Optional.empty())
    }
    var logoutLambda: (Boolean) -> String? = {
        null
    }

    override suspend fun getRoom(roomId: RoomId): MatrixRoom? {
        return getRoomResults[roomId]
    }

    override suspend fun findDM(userId: UserId): RoomId? {
        return findDmResult
    }

    override suspend fun ignoreUser(userId: UserId): Result<Unit> = simulateLongTask {
        return ignoreUserResult
    }

    override suspend fun unignoreUser(userId: UserId): Result<Unit> = simulateLongTask {
        return unignoreUserResult
    }

    override suspend fun createRoom(createRoomParams: CreateRoomParameters): Result<RoomId> = simulateLongTask {
        return createRoomResult
    }

    override suspend fun createDM(userId: UserId): Result<RoomId> = simulateLongTask {
        return createDmResult
    }

    override suspend fun getProfile(userId: UserId): Result<MatrixUser> {
        return getProfileResults[userId] ?: Result.failure(IllegalStateException("No profile found for $userId"))
    }

    override suspend fun searchUsers(searchTerm: String, limit: Long): Result<MatrixSearchUserResults> {
        return searchUserResults[searchTerm] ?: Result.failure(IllegalStateException("No response defined for $searchTerm"))
    }

    override fun syncService() = syncService

    override fun roomDirectoryService() = roomDirectoryService

    override suspend fun getCacheSize(): Long {
        return 0
    }

    override suspend fun clearCache() {
        clearCacheLambda()
    }

    override suspend fun logout(ignoreSdkError: Boolean): String? = simulateLongTask {
        return logoutLambda(ignoreSdkError)
    }

    override fun close() = Unit

    override suspend fun getUserProfile(): Result<MatrixUser> = simulateLongTask {
        val result = getProfileResults[sessionId]?.getOrNull() ?: MatrixUser(sessionId, userDisplayName, userAvatarUrl)
        _userProfile.tryEmit(result)
        return Result.success(result)
    }

    override suspend fun getAccountManagementUrl(action: AccountManagementAction?): Result<String?> {
        return accountManagementUrlString
    }

    override suspend fun uploadMedia(
        mimeType: String,
        data: ByteArray,
        progressCallback: ProgressCallback?
    ): Result<String> {
        return uploadMediaResult
    }

    override suspend fun setDisplayName(displayName: String): Result<Unit> = simulateLongTask {
        setDisplayNameCalled = true
        return setDisplayNameResult
    }

    override suspend fun uploadAvatar(mimeType: String, data: ByteArray): Result<Unit> = simulateLongTask {
        uploadAvatarCalled = true
        return uploadAvatarResult
    }

    override suspend fun removeAvatar(): Result<Unit> = simulateLongTask {
        removeAvatarCalled = true
        return removeAvatarResult
    }

    override suspend fun joinRoom(roomId: RoomId): Result<Unit> = joinRoomLambda(roomId)

    override suspend fun joinRoomByIdOrAlias(roomId: RoomId, serverNames: List<String>): Result<Unit> {
        return joinRoomByIdOrAliasLambda(roomId, serverNames)
    }

    override suspend fun knockRoom(roomId: RoomId): Result<Unit> = knockRoomLambda(roomId)

    override fun sessionVerificationService(): SessionVerificationService = sessionVerificationService

    override fun pushersService(): PushersService = pushersService

    override fun notificationService(): NotificationService = notificationService
    override fun notificationSettingsService(): NotificationSettingsService = notificationSettingsService
    override fun encryptionService(): EncryptionService = encryptionService

    override fun roomMembershipObserver(): RoomMembershipObserver {
        return RoomMembershipObserver()
    }

    suspend fun emitIgnoreUserList(users: List<UserId>) {
        ignoredUsersFlow.emit(users.toImmutableList())
    }

    // Mocks

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

    fun givenFindDmResult(result: RoomId?) {
        findDmResult = result
    }

    fun givenGetRoomResult(roomId: RoomId, result: MatrixRoom?) {
        if (result == null) {
            getRoomResults.remove(roomId)
        } else {
            getRoomResults[roomId] = result
        }
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

    fun givenSetDisplayNameResult(result: Result<Unit>) {
        setDisplayNameResult = result
    }

    fun givenUploadAvatarResult(result: Result<Unit>) {
        uploadAvatarResult = result
    }

    fun givenRemoveAvatarResult(result: Result<Unit>) {
        removeAvatarResult = result
    }

    private val visitedRoomsId: MutableList<RoomId> = mutableListOf()

    override suspend fun trackRecentlyVisitedRoom(roomId: RoomId): Result<Unit> {
        visitedRoomsId.removeAll { it == roomId }
        visitedRoomsId.add(0, roomId)
        return Result.success(Unit)
    }

    override suspend fun resolveRoomAlias(roomAlias: RoomAlias): Result<ResolvedRoomAlias> = simulateLongTask {
        resolveRoomAliasResult(roomAlias)
    }

    override suspend fun getRoomPreviewFromRoomId(roomId: RoomId, serverNames: List<String>) = simulateLongTask {
        getRoomPreviewFromRoomIdResult(roomId, serverNames)
    }

    override suspend fun getRecentlyVisitedRooms(): Result<List<RoomId>> {
        return Result.success(visitedRoomsId)
    }

    override fun getRoomInfoFlow(roomId: RoomId) = getRoomInfoFlowLambda(roomId)

    var setAllSendQueuesEnabledLambda = lambdaRecorder(ensureNeverCalled = true) { _: Boolean ->
        // no-op
    }

    override suspend fun setAllSendQueuesEnabled(enabled: Boolean) = setAllSendQueuesEnabledLambda(enabled)

    var sendQueueDisabledFlow = emptyFlow<RoomId>()
    override fun sendQueueDisabledFlow(): Flow<RoomId> = sendQueueDisabledFlow
}
