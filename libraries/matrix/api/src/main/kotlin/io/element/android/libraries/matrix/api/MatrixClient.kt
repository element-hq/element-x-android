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

package io.element.android.libraries.matrix.api

import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
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
import io.element.android.libraries.matrix.api.room.preview.RoomPreview
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryService
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.user.MatrixSearchUserResults
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.io.Closeable
import java.util.Optional

interface MatrixClient : Closeable {
    val sessionId: SessionId
    val deviceId: String
    val userProfile: StateFlow<MatrixUser>
    val roomListService: RoomListService
    val mediaLoader: MatrixMediaLoader
    val sessionCoroutineScope: CoroutineScope
    val ignoredUsersFlow: StateFlow<ImmutableList<UserId>>
    suspend fun getRoom(roomId: RoomId): MatrixRoom?
    suspend fun findDM(userId: UserId): RoomId?
    suspend fun ignoreUser(userId: UserId): Result<Unit>
    suspend fun unignoreUser(userId: UserId): Result<Unit>
    suspend fun createRoom(createRoomParams: CreateRoomParameters): Result<RoomId>
    suspend fun createDM(userId: UserId): Result<RoomId>
    suspend fun getProfile(userId: UserId): Result<MatrixUser>
    suspend fun searchUsers(searchTerm: String, limit: Long): Result<MatrixSearchUserResults>
    suspend fun setDisplayName(displayName: String): Result<Unit>
    suspend fun uploadAvatar(mimeType: String, data: ByteArray): Result<Unit>
    suspend fun removeAvatar(): Result<Unit>
    suspend fun joinRoom(roomId: RoomId): Result<Unit>
    fun syncService(): SyncService
    fun sessionVerificationService(): SessionVerificationService
    fun pushersService(): PushersService
    fun notificationService(): NotificationService
    fun notificationSettingsService(): NotificationSettingsService
    fun encryptionService(): EncryptionService
    fun roomDirectoryService(): RoomDirectoryService
    suspend fun getCacheSize(): Long

    /**
     * Will close the client and delete the cache data.
     */
    suspend fun clearCache()

    /**
     * Logout the user.
     * Returns an optional URL. When the URL is there, it should be presented to the user after logout for
     * Relying Party (RP) initiated logout on their account page.
     * @param ignoreSdkError if true, the SDK will ignore any error and delete the session data anyway.
     */
    suspend fun logout(ignoreSdkError: Boolean): String?

    /**
     * Retrieve the user profile, will also eventually emit a new value to [userProfile].
     */
    suspend fun getUserProfile(): Result<MatrixUser>
    suspend fun getAccountManagementUrl(action: AccountManagementAction?): Result<String?>
    suspend fun uploadMedia(mimeType: String, data: ByteArray, progressCallback: ProgressCallback?): Result<String>
    fun roomMembershipObserver(): RoomMembershipObserver
    fun getRoomInfoFlow(roomId: RoomId): Flow<Optional<MatrixRoomInfo>>

    fun isMe(userId: UserId?) = userId == sessionId

    suspend fun trackRecentlyVisitedRoom(roomId: RoomId): Result<Unit>
    suspend fun getRecentlyVisitedRooms(): Result<List<RoomId>>
    suspend fun resolveRoomAlias(roomAlias: RoomAlias): Result<RoomId>
    suspend fun getRoomPreview(roomIdOrAlias: RoomIdOrAlias): Result<RoomPreview>
}
