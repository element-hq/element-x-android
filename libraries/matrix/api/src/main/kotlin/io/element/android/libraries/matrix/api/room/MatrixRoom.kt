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

package io.element.android.libraries.matrix.api.room

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import kotlinx.coroutines.flow.Flow
import java.io.Closeable

interface MatrixRoom : Closeable {
    val roomId: RoomId
    val name: String?
    val bestName: String
    val displayName: String
    val alias: String?
    val alternativeAliases: List<String>
    val topic: String?
    val avatarUrl: String?
    val isEncrypted: Boolean
    val isDirect: Boolean
    val isPublic: Boolean

    suspend fun members(): List<RoomMember>

    suspend fun memberCount(): Int

    fun getMember(userId: UserId): RoomMember?

    fun getDmMember(): RoomMember?

    fun syncUpdateFlow(): Flow<Long>

    fun timeline(): MatrixTimeline

    suspend fun fetchMembers(): Result<Unit>

    suspend fun userDisplayName(userId: UserId): Result<String?>

    suspend fun userAvatarUrl(userId: UserId): Result<String?>

    suspend fun sendMessage(message: String): Result<Unit>

    suspend fun editMessage(originalEventId: EventId, message: String): Result<Unit>

    suspend fun replyMessage(eventId: EventId, message: String): Result<Unit>

    suspend fun redactEvent(eventId: EventId, reason: String? = null): Result<Unit>

    suspend fun leave(): Result<Unit>

    suspend fun acceptInvitation(): Result<Unit>

    suspend fun rejectInvitation(): Result<Unit>
}
