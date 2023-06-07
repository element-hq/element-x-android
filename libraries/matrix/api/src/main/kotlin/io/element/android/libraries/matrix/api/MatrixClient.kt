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

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.createroom.CreateRoomParameters
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.notification.NotificationService
import io.element.android.libraries.matrix.api.pusher.PushersService
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.RoomNotificationSettings
import io.element.android.libraries.matrix.api.room.RoomSummaryDataSource
import io.element.android.libraries.matrix.api.user.MatrixSearchUserResults
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import java.io.Closeable

interface MatrixClient : Closeable {
    val sessionId: SessionId
    val roomSummaryDataSource: RoomSummaryDataSource
    val invitesDataSource: RoomSummaryDataSource
    val mediaLoader: MatrixMediaLoader
    fun getRoom(roomId: RoomId): MatrixRoom?
    fun findDM(userId: UserId): MatrixRoom?
    suspend fun ignoreUser(userId: UserId): Result<Unit>
    suspend fun unignoreUser(userId: UserId): Result<Unit>
    suspend fun createRoom(createRoomParams: CreateRoomParameters): Result<RoomId>
    suspend fun createDM(userId: UserId): Result<RoomId>
    suspend fun getProfile(userId: UserId): Result<MatrixUser>
    suspend fun searchUsers(searchTerm: String, limit: Long): Result<MatrixSearchUserResults>
    suspend fun getRoomNotificationMode(roomId: RoomId): Result<RoomNotificationSettings>
    suspend fun muteRoom(roomId: RoomId): Result<Unit>
    suspend fun unmuteRoom(roomId: RoomId): Result<Unit>
    fun startSync()
    fun stopSync()
    fun sessionVerificationService(): SessionVerificationService
    fun pushersService(): PushersService
    fun notificationService(): NotificationService
    suspend fun logout()
    suspend fun loadUserDisplayName(): Result<String>
    suspend fun loadUserAvatarURLString(): Result<String?>
    suspend fun uploadMedia(mimeType: String, data: ByteArray): Result<String>
    fun onSlidingSyncUpdate()
    fun roomMembershipObserver(): RoomMembershipObserver
}
