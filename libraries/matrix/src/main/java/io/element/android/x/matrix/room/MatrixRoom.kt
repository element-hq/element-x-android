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

package io.element.android.x.matrix.room

import io.element.android.x.matrix.core.EventId
import io.element.android.x.matrix.core.RoomId
import io.element.android.x.matrix.timeline.MatrixTimeline
import kotlinx.coroutines.flow.Flow

interface MatrixRoom {
    val roomId: RoomId
    val name: String?
    val bestName: String
    val displayName: String
    val topic: String?
    val avatarUrl: String?

    fun syncUpdateFlow(): Flow<Long>

    fun timeline(): MatrixTimeline

    suspend fun userDisplayName(userId: String): Result<String?>

    suspend fun userAvatarUrl(userId: String): Result<String?>

    suspend fun sendMessage(message: String): Result<Unit>

    suspend fun editMessage(originalEventId: EventId, message: String): Result<Unit>

    suspend fun replyMessage(eventId: EventId, message: String): Result<Unit>

    suspend fun redactEvent(eventId: EventId, reason: String? = null): Result<Unit>
}
