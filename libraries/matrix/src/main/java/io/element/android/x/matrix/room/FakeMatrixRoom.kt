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
import io.element.android.x.matrix.timeline.FakeMatrixTimeline
import io.element.android.x.matrix.timeline.MatrixTimeline
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeMatrixRoom(
    override val roomId: RoomId,
    override val name: String? = null,
    override val bestName: String = "",
    override val displayName: String = "",
    override val topic: String? = null,
    override val avatarUrl: String? = null
) : MatrixRoom {

    override fun syncUpdateFlow(): Flow<Long> {
        return emptyFlow()
    }

    override fun timeline(): MatrixTimeline {
        return FakeMatrixTimeline()
    }

    override suspend fun userDisplayName(userId: String): Result<String?> {
        return Result.success("")
    }

    override suspend fun userAvatarUrl(userId: String): Result<String?> {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessage(message: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun editMessage(originalEventId: EventId, message: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun replyMessage(eventId: EventId, message: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun redactEvent(eventId: EventId, reason: String?): Result<Unit> {
        TODO("Not yet implemented")
    }
}
